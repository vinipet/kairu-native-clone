package com.kairu.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import com.kairu.core.event.*;
import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.EventListener;
import com.kairu.core.time.Clock;
import com.kairu.core.time.Interval;

public class SessionRuntime implements EventListener<Event>{
  
  private final Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();
  private UUID sessionId;
  private Instant currentStart;
  private List<Interval> intervals = new ArrayList<>();
  private State state = State.IDLE;
  private EventBus bus;
  private Clock clock;
  private enum State{
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED
  };


  public SessionRuntime(UUID sessionIdParam, EventBus bus, Clock clock){
    this.sessionId = sessionIdParam;
    this.bus = bus;
    this.clock = clock;

    handlers.put(TimerStartedEvent.class, e -> this.handleStart((TimerStartedEvent) e));
    handlers.put(TimerStoppedEvent.class, e -> this.handleStop((TimerStoppedEvent) e));
    handlers.put(TimerPausedEvent.class, e -> this.handlePaused((TimerPausedEvent) e));
    handlers.put(TimerResumeEvent.class, e -> this.handleResume((TimerResumeEvent) e));
    
  }


  @Override
  public void onEvent(Event event) {
    Consumer<Event> handler = handlers.get(event.getClass());

    if (handler != null){
      handler.accept(event);
    }

  }
  
  private void handleStart(TimerStartedEvent e){
    if (state != State.IDLE){
      throw new IllegalStateException("cant start a session who is started");
    } 
    currentStart = e.getOccurredAt();
    state = State.RUNNING;
  }

  private void handleStop(TimerStoppedEvent e){
    if (state != State.RUNNING && state != State.PAUSED){
      throw new IllegalStateException("cant stop a stopped session");
    }
    
    if(state == State.RUNNING){
      Interval currentInterval = new Interval(currentStart, e.getOccurredAt());
      if (currentInterval.getDuration().isNegative() || currentInterval.getDuration().isZero()){
        throw new IllegalArgumentException("the interval must be positive ");
      }
      intervals.add(currentInterval);
    }

    currentStart = null;
    state = State.STOPPED;
    Session session = finishSession();
    bus.publishEvent(new SessionCompletedEvent(clock.now(),session));

  }

  private void handlePaused(TimerPausedEvent e){
    if(state != State.RUNNING){
      throw new IllegalStateException("cant pause a session who arent running.");
    }
    
    intervals.add(new Interval(currentStart, e.getOccurredAt()));
    currentStart = null;
    state = State.PAUSED;

  }

  private void handleResume(TimerResumeEvent e){
    if(state != State.PAUSED){
      throw new IllegalStateException("cant resume a session who arent paused.");
    }
    currentStart = e.getOccurredAt();
    state = State.RUNNING;

  }

  public Session finishSession(){
    Duration duration = intervals.stream().map(Interval::getDuration).reduce(Duration.ZERO, Duration::plus);   
    if(duration.toMinutes() <= 5){
      throw new IllegalStateException("the duration of session must be gratter than 5 minutes");
    } 
    Session session = new Session(sessionId, intervals);
    return session;
  }
}
