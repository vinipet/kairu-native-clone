package com.kairu.core.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public interface Clock {
    Instant now();
    public ZoneId getZoneId();

    default public LocalDate today(){
        return now().atZone(getZoneId()).toLocalDate();
    }

}

