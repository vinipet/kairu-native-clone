package com.kairu.core.session;

import com.kairu.core.bus.EventListener;
import com.kairu.core.event.SessionCompletedEvent;

public class SessionCompletedPersistenceListener implements EventListener<SessionCompletedEvent>{
  final SessionRepository repository;

  @Override
  public void onEvent(SessionCompletedEvent event) {
    repository.save(event.getSession());
        
  }

  public SessionCompletedPersistenceListener(SessionRepository sessionRepository){
    this.repository = sessionRepository;
  } 
}
