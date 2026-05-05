package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerResumeEvent extends BaseEvent {
    private final UUID sessionId;
    public TimerResumeEvent(Instant timestamp, UUID sessionId) {
        super(timestamp);
        this.sessionId = sessionId;
    }
    public UUID getId(){
      return sessionId;
    }
}
