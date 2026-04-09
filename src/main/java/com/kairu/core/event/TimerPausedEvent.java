package com.kairu.core.event;
import java.time.Instant;

public class TimerPausedEvent extends BaseEvent {
    public TimerPausedEvent(Instant timestamp) {
        super(timestamp);
    }
}
