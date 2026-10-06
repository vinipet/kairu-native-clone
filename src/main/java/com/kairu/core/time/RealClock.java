package com.kairu.core.time;

import java.time.Instant;
import java.time.ZoneId;

public final class RealClock implements Clock {

    private ZoneId zoneId;

    @Override
    public Instant now() {
        return Instant.now();
    }

    public RealClock(ZoneId zoneId){
            if( zoneId == null ){ throw new IllegalStateException("zone id cant be null");}
        this.zoneId = zoneId;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}

