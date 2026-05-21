package com.kairu.core.session;

import java.time.Duration;
import java.util.UUID;

import com.kairu.core.bus.EventBus;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStoppedEvent;
import com.kairu.core.time.Clock;
import com.kairu.core.time.StopResult;
import com.kairu.core.time.Timer;
import com.kairu.core.time.TimerFactory;


public class SessionManager{
  
  private final  EventBus bus;
  private final Clock clock;
  private final TimerFactory factory;
  private SessionRuntime currentRuntime;
  private UUID currentSessionId;
  private Tag currentTag;
  private Timer timer;

  public SessionManager(EventBus bus, Clock clock, TimerFactory factory){
    
    this.bus = bus;
    this.clock = clock;
    this.factory = factory;

  }

  public void startSession(Tag tag){
    if(currentRuntime != null){
      throw new IllegalStateException("there is already an active session");
    }
    
    currentTag = tag;
    currentSessionId = UUID.randomUUID();
    currentRuntime = new SessionRuntime(currentSessionId, bus, clock, currentTag);
    bus.subscribeListener(TimerStartedEvent.class, currentRuntime);
    bus.subscribeListener(TimerPausedEvent.class, currentRuntime);
    bus.subscribeListener(TimerResumeEvent.class, currentRuntime);
    bus.subscribeListener(TimerStoppedEvent.class, currentRuntime);
    timer = factory.createDefaultTimer(currentSessionId);
    timer.start();

  }

  public void resumeSession(){
    if(currentRuntime == null){
      throw new IllegalStateException("need a active, and paused session to resume");
    }
    timer.resume();
  }

  public void pauseSession(){
    if(currentRuntime == null){
      throw new IllegalStateException("need a active, and running session to pause");
    }
    timer.pause();
  }

  public StopResult stopSession(){
    if(currentRuntime == null){
      throw new IllegalStateException("need a active session to stop");
    } 
    
    Duration duration = currentRuntime.getDuration();

    if(duration.toMinutes() <= 5){
      return StopResult.TOO_SHORT;
    } else {
      timer.stop();

      bus.unsubscribeForAll(currentRuntime);

      currentRuntime = null;
      currentSessionId = null;
      timer = null;
      return StopResult.SUCCESS;
    }
  }

  public void cancelSession(){
    timer.cancel();
    bus.unsubscribeForAll(currentRuntime);

    currentRuntime = null;
    currentSessionId = null;
    timer = null;
  }
  
  public SessionRuntime getCurrentRuntime() {
    return currentRuntime; 
  }

  public Timer getTimer(){
    return timer;
  }
}
