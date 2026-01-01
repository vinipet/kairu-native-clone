package com.kairu.core.time;

import java.time.Instant;

public final class RealClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}

