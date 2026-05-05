package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerStoppedEvent extends SessionEvent {
    public TimerStoppedEvent(Instant timestamp, UUID sessionId) {
        super(timestamp, sessionId);
    }
}
