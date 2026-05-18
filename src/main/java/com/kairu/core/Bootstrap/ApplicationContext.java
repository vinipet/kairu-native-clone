package com.kairu.core.Bootstrap;

import com.kairu.core.bus.EventBus;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.SessionRepository;


public class ApplicationContext{
  private EventBus bus;
  private SessionRepository sessionRepository;
  private SessionManager manager;
  

  public ApplicationContext(EventBus bus, SessionRepository repository, SessionManager manager){
    this.manager = manager;
    this.bus = bus;
    this.sessionRepository = repository;
  }

  public EventBus getBus() {
      return bus;
  }

  public SessionManager getManager() {
      return manager;
  }

  public SessionRepository getSessionRepository() {
      return sessionRepository;
  }
}
