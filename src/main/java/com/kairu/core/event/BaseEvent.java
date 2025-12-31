package com.kairu.core.event;
import java.time.Instant;

public abstract class BaseEvent implements Event {
    private final Instant timestamp;

    public BaseEvent(Instant timestamp) {
        this.timestamp = timestamp; 
    }
    
    @Override
    public Instant getOccurredAt() {
      return timestamp;
    }

}
