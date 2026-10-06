package com.kairu.core.time;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.bus.SimpleListener;
import com.kairu.core.event.TimerCancelEvent;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTimerTest {
  ManualClock clock;
  SimpleEventBus bus;
  UUID id;
  sessionTimer timer;

  @BeforeEach
  public void setup(){
    this.clock = new ManualClock(Instant.now(), ZoneId.systemDefault());
    this.bus = new SimpleEventBus();
    this.id = UUID.randomUUID();
    this.timer = new sessionTimer(clock, bus,id);
  }

  @Test
  public void startMovesToRunningAndPublishesStartedEvent(){
    SimpleListener<TimerStartedEvent> listener = new SimpleListener<>();
    bus.subscribeListener(TimerStartedEvent.class, listener);

    timer.start();

    assertEquals(Timer.State.RUNNING, timer.getState());
    assertEquals(1, listener.getEvents().size());
    assertEquals(id, listener.getEvents().get(0).getSessionId());
  }

  @Test
  public void pauseMovesToPausedAndPublishesPausedEvent(){
    SimpleListener<TimerPausedEvent> listener = new SimpleListener<>();
    bus.subscribeListener(TimerPausedEvent.class, listener);
    timer.start();

    timer.pause();

    assertEquals(Timer.State.PAUSED, timer.getState());
    assertEquals(1, listener.getEvents().size());
    assertInstanceOf(TimerPausedEvent.class, listener.getEvents().get(0));
  }

  @Test
  public void stopFromRunningMovesToStoppedAndPublishesStoppedEvent(){
    SimpleListener<TimerStoppedEvent> listener = new SimpleListener<>();
    bus.subscribeListener(TimerStoppedEvent.class, listener);
    timer.start();

    timer.stop();

    assertEquals(Timer.State.STOPPED, timer.getState());
    assertEquals(1, listener.getEvents().size());
    assertEquals(id, listener.getEvents().get(0).getSessionId());
  }

  @Test
  public void stopFromPausedMovesToStoppedAndPublishesStoppedEvent(){
    SimpleListener<TimerStoppedEvent> listener = new SimpleListener<>();
    bus.subscribeListener(TimerStoppedEvent.class, listener);
    timer.start();
    timer.pause();

    timer.stop();

    assertEquals(Timer.State.STOPPED, timer.getState());
    assertEquals(1, listener.getEvents().size());
    assertEquals(id, listener.getEvents().get(0).getSessionId());
  }

  @Test
  public void resumeMovesToRunningAndPublishesResumeEvent(){
    SimpleListener<TimerResumeEvent> listener = new SimpleListener<>();
    bus.subscribeListener(TimerResumeEvent.class, listener);
    timer.start();
    timer.pause();

    timer.resume();

    assertEquals(Timer.State.RUNNING, timer.getState());
    assertEquals(1, listener.getEvents().size());
    assertEquals(id, listener.getEvents().get(0).getSessionId());
  }

  @Test
  public void cancelMovesToIdleAndPublishesCancelEvent(){
    SimpleListener<TimerCancelEvent> listener = new SimpleListener<>();
    bus.subscribeListener(TimerCancelEvent.class, listener);
    timer.start();

    timer.cancel();

    assertEquals(Timer.State.IDLE, timer.getState());
    assertEquals(1, listener.getEvents().size());
    assertEquals(id, listener.getEvents().get(0).getSessionId());
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
  public void throwErrorWhenPauseBeforeStart(){
    assertThrows(IllegalStateException.class, ()-> {
      timer.pause();
    });
  }

  @Test
  public void throwErrorWhenResumeWithoutPause(){
    timer.start();
    assertThrows(IllegalStateException.class, ()-> {
      timer.resume();
    });
  }

  @Test
  public void throwErrorWhenStopOnIdle(){
    assertThrows(IllegalStateException.class, ()-> {
      timer.stop();
    });
  }

  @Test
  public void throwErrorWhenStopTwice(){
    timer.start();
    timer.stop();
    assertThrows(IllegalStateException.class, ()-> {
      timer.stop();
    });
  }

}
