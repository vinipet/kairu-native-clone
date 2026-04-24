package com.kairu.core.event;
import java.time.Instant;

import com.kairu.core.session.Session;

public class SessionCompletedEvent extends BaseEvent {
    private final Session session;
    public SessionCompletedEvent(Instant timestamp, Session session) {
        super(timestamp);
        this.session = session;
    }
    
    public Session getSession(){
    return session;
  }
}
