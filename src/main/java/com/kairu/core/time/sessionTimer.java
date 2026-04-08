package com.kairu.core.time;

import com.kairu.core.time.Clock;
import java.time.Instant;
import java.time.Duration;

public final class sessionTimer implements Timer{
  private Instant startedAt;
  private long elapsedTime = 0;
  private enum State {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED
  }
  private State state = State.IDLE;
  private Clock clock;
  
  public sessionTimer(Clock clock){
    this.clock = clock;
  }

  public void start(){
    if(State.IDLE == state){
      startedAt = clock.now();
      elapsedTime = 0;
      state = State.RUNNING;
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
          break;

      case STOPPED:
        throw new IllegalStateException("nao pode finalizar sessão ja finalizada");

      case IDLE:
        throw new IllegalStateException("nao pode finalizar uma sessão nao iniciada");

      case RUNNING:
        elapsedTime += Duration.between(startedAt, clock.now()).toSeconds();
        state = State.STOPPED;
        break;
    }
  }  

  public void resume(){
    if(state != State.PAUSED){
      throw new IllegalStateException("não pode resumir uma sessão nao pausada");
    }

    startedAt = clock.now();
    state = State.RUNNING;
  }
}
