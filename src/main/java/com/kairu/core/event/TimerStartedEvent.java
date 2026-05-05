package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerStartedEvent extends SessionEvent {
    public TimerStartedEvent(Instant timestamp, UUID sessionId) {
        super(timestamp,sessionId);
    }
}
