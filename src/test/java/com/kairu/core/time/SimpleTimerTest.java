package com.kairu.core.time;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kairu.core.bus.SimpleEventBus;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTimerTest {
  ManualClock clock;
  SimpleEventBus bus;
  UUID id;
  sessionTimer timer;

  @BeforeEach
  public void setup(){
    this.clock = new ManualClock(Instant.now());
    this.bus = new SimpleEventBus();
    this.id = UUID.randomUUID();
    this.timer = new sessionTimer(clock, bus,id);
  }


  @Test
  public void shouldCalculateTimeBetweenStartAndPause() {
    timer.start();
    clock.advanceSeconds(10);
    timer.pause();

    assertEquals((long) 10, timer.getElapsedTime());
  }

  @Test
  public void ThrowErrorWhenStartTwice(){
    timer.start();

    assertThrows(IllegalStateException.class, ()-> {
      timer.start();
    });
  }

  @Test
  public void ThrowErrorWhenPauseTwice(){
    timer.start();
    timer.pause();
    assertThrows(IllegalStateException.class, ()-> {
      timer.pause();
    });
  }

  @Test
  public void shouldCalculateTimeBetweenStartAndPauseTwice(){
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
    timer.start();
    clock.advanceSeconds(5);
    timer.stop();

    assertEquals(5, timer.getElapsedTime());
  }
  
  @Test
  public void timerCanStopWhenPaused(){
    timer.start();
    clock.advanceSeconds(5);
    timer.pause();
    timer.stop();

      assertEquals(5, timer.getElapsedTime());
  }

  @Test
  public void advanceTimeOnPauseCantCountToElapsedTime(){
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
    timer.start();
    clock.advanceSeconds(5);

    assertEquals(5, timer.getElapsedTime());
  }
}
