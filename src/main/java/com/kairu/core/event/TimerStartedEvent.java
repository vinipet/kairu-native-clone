package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerStartedEvent extends BaseEvent {
    private final UUID sessionId;
    public TimerStartedEvent(Instant timestamp, UUID sessionId) {
        super(timestamp);
        this.sessionId = sessionId;
    }
    public UUID getId(){
      return sessionId;
    }
}
