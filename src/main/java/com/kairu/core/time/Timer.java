package com.kairu.core.time;


public interface Timer {

  enum State {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED
  }

  public void start();

  public void pause();
  
  public long getElapsedTime();

  public void stop();

  public void resume();

  public State getState();
}
