package com.kairu.core.time;


public interface Timer {

  public void start();

  public void pause();
  
  public long getElapsedTime();

  public void stop();

  public void resume();
}
