package com.kairu.core.time;

import java.time.Instant;

public class ManualClock implements Clock {

    private Instant current;

    public ManualClock(Instant start) {
        this.current = start;
    }

    @Override
    public Instant now() {
    /**
 * Returns the current simulated time.
 * 
 * This clock does NOT track real-world time.
 * Time only changes when manually advanced.
 */
        return current;
    }

    public void advanceSeconds(long seconds) {
        if (seconds < 0) {
          throw new IllegalArgumentException("the argument does't be negative or zero");
        }
        current = current.plusSeconds(seconds);
    }

    public void set(Instant instant) {
        current = instant;
    }
}

