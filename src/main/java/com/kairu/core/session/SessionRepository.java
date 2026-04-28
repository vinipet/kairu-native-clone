package com.kairu.core.session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
   
  public void save(Session session);
  public Optional<Session> findById(UUID sessionId);
  public List<Session> findAll();

}
