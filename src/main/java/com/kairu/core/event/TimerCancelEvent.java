package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerCancelEvent extends SessionEvent {
    public TimerCancelEvent(Instant timestamp, UUID sessionId) {
        super(timestamp, sessionId);
    }
}
