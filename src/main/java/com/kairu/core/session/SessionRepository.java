package com.kairu.core.session;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
   
  public void save(Session session);
  public Optional<Session> findById(long sessionId);
  public List<Session> findAll();

}
