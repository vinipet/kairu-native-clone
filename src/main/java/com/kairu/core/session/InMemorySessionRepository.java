package com.kairu.core.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class InMemorySessionRepository implements SessionRepository{
  
  private List<Session> sessionRepository = new ArrayList<>();


  @Override
  public void save(Session session) {
    if(session == null){
      throw new IllegalArgumentException("the argument dont can be a null type");
    } 
    sessionRepository.add(session); 
  }

  @Override
  public Optional<Session> findById(long sessionId) {
    for (Session session : sessionRepository) {
      if(session.sessionId == sessionId){
        return Optional.of(session);
      }
    }
    return Optional.empty();
  }

  @Override
  public List<Session> findAll(){
    final List<Session> sessionList = List.copyOf(sessionRepository);
    return sessionList;  
  }
 
}
