package com.kairu.core.bus;

import com.kairu.core.event.BaseEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SimpleEventBusTest {

    static class TestEvent extends BaseEvent {

        public TestEvent(Instant occurredAt) {
            super(occurredAt);
        }
    }

    @Test
    void listenerReceivesPublishedEvent() {
        SimpleEventBus bus = new SimpleEventBus();
        AtomicBoolean received = new AtomicBoolean(false);
        bus.subscribe(TestEvent.class, event -> {
            received.set(true);
        });
        bus.publish(new TestEvent(Instant.now()));
        assertTrue(received.get());
    }

    @Test
    void publishingEventWithNoListenersDoesNotCrash() {
      SimpleEventBus bus = new SimpleEventBus();
      TestEvent event = new TestEvent(Instant.now());
      assertDoesNotThrow(() -> bus.publish(event));
    }
    static class AnotherEvent extends BaseEvent {

      public AnotherEvent(Instant occurredAt) {
        super(occurredAt);
      }
    }

  @Test
  void listenerOnlyReceivesSubscribedEventType() {
    SimpleEventBus bus = new SimpleEventBus();
    AtomicBoolean received = new AtomicBoolean(false);
    bus.subscribe(TestEvent.class, event -> {
        received.set(true);
    });
    bus.publish(new AnotherEvent(Instant.now()));
    assertFalse(received.get());
  }

}
