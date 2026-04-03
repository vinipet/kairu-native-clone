package com.kairu.core.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SimpleManualClockTest {
    
  @Test
  public void ClockCanSkipTime() {
    ManualClock clock = new ManualClock(Instant.now()); 
    
    Instant Start = clock.now();
    clock.advanceSeconds(10);
    
    assertEquals(10, Duration.between(Start, clock.now()).toSeconds());
  }

  @Test
  public void ClockCanSkipTimeMultpliTimes() {
    ManualClock clock = new ManualClock(Instant.now());

    Instant StartAt = clock.now();
    clock.advanceSeconds(5);
    Instant t1 = clock.now();
    clock.advanceSeconds(5);
    Instant t2 = clock.now();
    

    assertEquals(5, Duration.between(StartAt, t1).toSeconds());
    assertEquals(5, Duration.between(t1, t2).toSeconds());
    assertEquals(10, Duration.between(StartAt, clock.now()).toSeconds());

  }

  @Test
  public void ClockCantSkipWithNegativeNumbers(){
    ManualClock clock = new ManualClock(Instant.now());
    
    assertThrows(IllegalArgumentException.class, () -> {
    clock.advanceSeconds(-5);
    });
  }

  @Test
  public void SugestedName(){
    ManualClock clock = new ManualClock(Instant.now());

    Instant t1 = clock.now();
    Instant t2 = clock.now();

    assertEquals(t1, t2);

  }

}
