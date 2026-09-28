package com.kairu.core.time;

import java.time.Instant;
import java.time.LocalDate;

public interface Clock {
    Instant now();
    LocalDate today();
}

