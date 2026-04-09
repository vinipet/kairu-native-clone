package com.kairu.core.bus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.time.ManualClock;
import com.kairu.core.time.sessionTimer;

public class SimpleListenerTest {
  @Test
  void shouldReceiveOnlySubscribedEventType() {
    ManualClock clock = new ManualClock(Instant.now());
    SimpleEventBus bus = new SimpleEventBus();
    sessionTimer timer = new sessionTimer(clock, bus);
    SimpleListener<TimerStartedEvent> listener = new SimpleListener<TimerStartedEvent>(); 

    bus.subscribeListener(TimerStartedEvent.class, listener);
    timer.start();
    timer.pause();
    timer.resume();
    timer.stop();
    assertEquals(1, listener.getEvents().size());
    assertInstanceOf(TimerStartedEvent.class, listener.getEvents().get(0));
  }

}
