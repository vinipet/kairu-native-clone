package com.kairu.core.time;

import com.kairu.core.bus.EventBus;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;

import java.time.Instant;
import java.time.Duration;

public final class sessionTimer implements Timer{
  private Instant startedAt;
  private long elapsedTime = 0;
  private State state = State.IDLE;
  private Clock clock;
  private EventBus bus;
  private enum State {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED
  }
  
  public sessionTimer(Clock clock, EventBus bus){
    this.clock = clock;
    this.bus = bus;
  }

  public void start(){
    if(State.IDLE == state){
      startedAt = clock.now();
      elapsedTime = 0;
      state = State.RUNNING;
      bus.publishEvent(new TimerStartedEvent(startedAt));

    }else{
      throw new IllegalStateException("nao pode começar uma sessão antes de terminar outra");

    }
  } 

  public void pause(){
    if(state != State.RUNNING){
      throw new IllegalStateException("a sessão deve estar rodando pra ser pausada");
    }

    elapsedTime += Duration.between(startedAt, clock.now()).toSeconds();
    state = State.PAUSED;
    bus.publishEvent(new TimerPausedEvent(clock.now()));
  }

  public long getElapsedTime(){
    if (state == State.RUNNING) {
      return elapsedTime + Duration.between(startedAt, clock.now()).toSeconds();
    }
    return elapsedTime;
  } 

  public void stop() {
    switch (state) {
      case PAUSED:
        state = State.STOPPED;
        bus.publishEvent(new TimerStoppedEvent(clock.now()));
      break;

      case STOPPED:
        throw new IllegalStateException("nao pode finalizar sessão ja finalizada");

      case IDLE:
        throw new IllegalStateException("nao pode finalizar uma sessão nao iniciada");

      case RUNNING:
        elapsedTime += Duration.between(startedAt, clock.now()).toSeconds();
        state = State.STOPPED;
        bus.publishEvent(new TimerStoppedEvent(clock.now()));
      break;
    }
  }  

  public void resume(){
    if(state != State.PAUSED){
      throw new IllegalStateException("não pode resumir uma sessão nao pausada");
    }

    startedAt = clock.now();
    state = State.RUNNING;
      bus.publishEvent(new TimerResumeEvent(clock.now()));
  }
}
