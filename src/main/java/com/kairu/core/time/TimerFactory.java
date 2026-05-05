package com.kairu.core.time;

import java.util.UUID;

import com.kairu.core.bus.EventBus;

public class TimerFactory{
  
  private EventBus bus;
  private Clock clock;

  public TimerFactory(Clock clock, EventBus bus){
    this.bus = bus;
    this.clock = clock;
  }

  public Timer createDefaultTimer(UUID id){
    return new sessionTimer(this.clock, this.bus, id);
  }

}
