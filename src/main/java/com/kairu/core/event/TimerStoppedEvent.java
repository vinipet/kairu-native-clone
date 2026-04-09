package com.kairu.core.event;
import java.time.Instant;

public class TimerStoppedEvent extends BaseEvent {
    public TimerStoppedEvent(Instant timestamp) {
        super(timestamp);
    }
}
