package com.kairu.core.event;
import java.time.Instant;

public class TimerResumeEvent extends BaseEvent {
    public TimerResumeEvent(Instant timestamp) {
        super(timestamp);
    }
}
