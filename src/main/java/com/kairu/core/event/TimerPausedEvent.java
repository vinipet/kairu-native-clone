package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerPausedEvent extends BaseEvent {
    private final UUID sessionId;
    public TimerPausedEvent(Instant timestamp, UUID sessionId) {
        super(timestamp);
        this.sessionId = sessionId;
    }
    public UUID getId(){
      return sessionId;
    }
}
