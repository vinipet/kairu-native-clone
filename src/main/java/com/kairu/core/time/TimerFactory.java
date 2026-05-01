package com.kairu.core.time;

import com.kairu.core.bus.EventBus;

public class TimerFactory{
  
  private EventBus bus;
  private Clock clock;

  public TimerFactory(Clock clock, EventBus bus){
    this.bus = bus;
    this.clock = clock;
  }

  public Timer createDefaultTimer(){
    return new sessionTimer(this.clock, this.bus);
  }

}
