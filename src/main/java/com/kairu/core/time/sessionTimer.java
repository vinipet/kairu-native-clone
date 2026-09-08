package com.kairu.core.time;

import com.kairu.core.bus.EventBus;
import com.kairu.core.event.TimerCancelEvent;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;

import java.util.UUID;

public final class sessionTimer implements Timer{
  private State state = Timer.State.IDLE;
  private Clock clock;
  private EventBus bus;
  private UUID id;

  public State getState(){
    return state;
  }
  
  public sessionTimer(Clock clock, EventBus bus, UUID id){
    this.clock = clock;
    this.bus = bus;
    this.id = id;
  }

  public void start(){
    if(Timer.State.IDLE == state){
      state = Timer.State.RUNNING;
      bus.publishEvent(new TimerStartedEvent(clock.now(),id));

    }else{
      throw new IllegalStateException("nao pode começar uma sessão antes de terminar outra");

    }
  } 

  public void pause(){
    if(state != State.RUNNING){
      throw new IllegalStateException("a sessão deve estar rodando pra ser pausada");
    }

    state = State.PAUSED;
    bus.publishEvent(new TimerPausedEvent(clock.now(),id));
  }

  public void stop() {
    switch (state) {
      case PAUSED:
        state = State.STOPPED;
        bus.publishEvent(new TimerStoppedEvent(clock.now(),id));
      break;

      case STOPPED:
        throw new IllegalStateException("nao pode finalizar sessão ja finalizada");
      case IDLE:
        throw new IllegalStateException("nao pode finalizar uma sessão nao iniciada");
      case RUNNING:
        state = State.STOPPED;
        bus.publishEvent(new TimerStoppedEvent(clock.now(),id));
      break;
    }
  }  

  public void resume(){
    if(state != State.PAUSED){
      throw new IllegalStateException("não pode resumir uma sessão nao pausada");
    }

    state = State.RUNNING;
    bus.publishEvent(new TimerResumeEvent(clock.now(),id));
  }

  public void cancel(){
    state = State.IDLE;
    bus.publishEvent(new TimerCancelEvent(clock.now(),id));
  }
}
