package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public abstract class SessionEvent implements Event {
    private final Instant timestamp;
    private final UUID sessionId;

    public SessionEvent(Instant timestamp, UUID id) {
        this.timestamp = timestamp;
        this.sessionId = id;
    }
    
    @Override
    public Instant getOccurredAt() {
      return timestamp;
    }

    public UUID getSessionId(){
      return sessionId;
    }

}
