package com.kairu.core.Bootstrap;

import com.kairu.core.bus.EventBus;
import com.kairu.core.session.SessionManager;
import com.kairu.core.time.Clock;
import com.kairu.core.time.TimerFactory;

public class CoreModule{


  public  static SessionManager createCore(EventBus bus, Clock clock){
    TimerFactory factory = new TimerFactory(clock, bus);
    SessionManager manager = new SessionManager(bus, clock, factory);
    return manager;
  }
}
