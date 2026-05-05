package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

import com.kairu.core.session.Session;

public class SessionCompletedEvent extends SessionEvent {
    private final Session session;
    public SessionCompletedEvent(Instant timestamp, UUID id, Session session) {
        super(timestamp, id);
        this.session = session;
    }
    
    public Session getSession(){
    return session;
  }
}
