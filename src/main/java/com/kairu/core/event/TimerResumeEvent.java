package com.kairu.core.event;
import java.time.Instant;
import java.util.UUID;

public class TimerResumeEvent extends SessionEvent {
    public TimerResumeEvent(Instant timestamp, UUID sessionId) {
        super(timestamp,sessionId);
    }
}
