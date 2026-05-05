package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerPausedEvent extends SessionEvent {
    public TimerPausedEvent(Instant timestamp, UUID sessionId) {
        super(timestamp,sessionId);
    }
}
