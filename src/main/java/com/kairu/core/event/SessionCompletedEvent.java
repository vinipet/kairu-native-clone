package com.kairu.core.event;
import java.time.Instant;

public class SessionCompletedEvent extends BaseEvent {
    private final long sessionId;
    public SessionCompletedEvent(Instant timestamp, long sessionId) {
        super(timestamp);
        this.sessionId = sessionId;
    }
    
    public long getSessionId(){
    return sessionId;
  }
}
