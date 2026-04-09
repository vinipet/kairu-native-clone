package com.kairu.core.event;
import java.time.Instant;

public class TimerStartedEvent extends BaseEvent {
    public TimerStartedEvent(Instant timestamp) {
        super(timestamp);
    }
}
