package com.kairu.core.time;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import com.kairu.core.bus.SimpleEventBus;
import static org.junit.jupiter.api.Assertions.*;
import com.kairu.core.session.sessionTimer;

public class SimpleTimerTest {

  @Test
  public void shouldCalculateTimeBetweenStartAndPause() {
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);

    timer.start();
    clock.advanceSeconds(10);
    timer.pause();

    assertEquals((long) 10, timer.getElapsedTime());
  }

  @Test
  public void ThrowErrorWhenStartTwice(){
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);

    timer.start();

    assertThrows(IllegalStateException.class, ()-> {
      timer.start();
    });
  }

  @Test
  public void ThrowErrorWhenPauseTwice(){
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);

    timer.start();
    timer.pause();
    assertThrows(IllegalStateException.class, ()-> {
      timer.pause();
    });
  }

  @Test
  public void shouldCalculateTimeBetweenStartAndPauseTwice(){
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);
    timer.start();
    clock.advanceSeconds(5);
    timer.pause();

    timer.resume();
    clock.advanceSeconds(5);
    timer.pause();

    assertEquals(10, timer.getElapsedTime());
  }

  @Test
  public void timerStopMantainElapsedTime(){  
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);

    timer.start();
    clock.advanceSeconds(5);
    timer.stop();

    assertEquals(5, timer.getElapsedTime());
  }
  
  @Test
  public void timerCanStopWhenPaused(){
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);

    timer.start();
    clock.advanceSeconds(5);
    timer.pause();
    timer.stop();

      assertEquals(5, timer.getElapsedTime());
  }

  @Test
  public void advanceTimeOnPauseCantCountToElapsedTime(){
  ManualClock clock = new ManualClock(Instant.now());
  SimpleEventBus bus = new SimpleEventBus();
  sessionTimer timer = new sessionTimer(clock, bus);
  
  timer.start();
  clock.advanceSeconds(5);
  timer.pause();

  clock.advanceSeconds(100); // não deve contar

  timer.resume();
  clock.advanceSeconds(5);
  timer.stop();

  assertEquals(10, timer.getElapsedTime());
  }

  @Test
  public void shouldReturnElapsedTimeWhileRunning() {
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);

    timer.start();
    clock.advanceSeconds(5);

    assertEquals(5, timer.getElapsedTime());
  }
}
