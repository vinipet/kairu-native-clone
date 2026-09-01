package com.kairu.core.session;

import java.time.Duration;
import java.util.UUID;

import com.kairu.core.bus.EventBus;
import com.kairu.core.event.TimerCancelEvent;
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

  public SessionManager(EventBus bus, Clock clock, TimerFactory factory){
    
    this.bus = bus;
    this.clock = clock;
    this.factory = factory;

  }

  public void startSession(Tag tag){
    if(currentRuntime != null){
      throw new IllegalStateException("there is already an active session");
    }
    UUID id = UUID.randomUUID();
    Timer timer = factory.createDefaultTimer(id);

    currentRuntime = new SessionRuntime(id, bus, clock, tag, timer);
    bus.subscribeListener(TimerStartedEvent.class, currentRuntime);
    bus.subscribeListener(TimerPausedEvent.class, currentRuntime);
    bus.subscribeListener(TimerResumeEvent.class, currentRuntime);
    bus.subscribeListener(TimerStoppedEvent.class, currentRuntime);
    bus.subscribeListener(TimerCancelEvent.class, currentRuntime);

    currentRuntime.getTimer().start();
  }

  public void resumeSession(){
    if(currentRuntime == null){
      throw new IllegalStateException("need a active, and paused session to resume");
    }
    currentRuntime.getTimer().resume();
  }

  public void pauseSession(){
    if(currentRuntime == null){
      throw new IllegalStateException("need a active, and running session to pause");
    }
    currentRuntime.getTimer().pause();
  }

  public StopResult stopSession(){
    if(currentRuntime == null){
      throw new IllegalStateException("need a active session to stop");
    } 
    
    Duration duration = currentRuntime.getDuration();

    if(duration.toMinutes() <= 5){
      return StopResult.TOO_SHORT;
    } else {
      currentRuntime.getTimer().stop();

      bus.unsubscribeForAll(currentRuntime);

      currentRuntime = null;
      return StopResult.SUCCESS;
    }
  }

  public void cancelSession(){
    currentRuntime.getTimer().cancel();
    bus.unsubscribeForAll(currentRuntime);

    currentRuntime = null;
  }
  
  public SessionRuntime getCurrentRuntime() {
    return currentRuntime; 
  }

  public Timer getTimer(){
    if(currentRuntime == null) { return null;}
    else{ return currentRuntime.getTimer();}
  }

}
