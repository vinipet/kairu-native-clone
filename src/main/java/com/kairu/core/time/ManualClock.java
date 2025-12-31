package com.kairu.core.time;

import java.time.Instant;

public class ManualClock implements Clock {

    private Instant current;

    public ManualClock(Instant start) {
        this.current = start;
    }

    @Override
    public Instant now() {
        return current;
    }

    public void advanceSeconds(long seconds) {
        current = current.plusSeconds(seconds);
    }

    public void set(Instant instant) {
        current = instant;
    }
}

