package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerStoppedEvent extends BaseEvent {
    private final UUID sessionId;
    public TimerStoppedEvent(Instant timestamp, UUID sessionId) {
        super(timestamp);
        this.sessionId = sessionId;
    }
    public UUID getId(){
      return sessionId;
    }
}
