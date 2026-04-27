package com.kairu.core.bus;

import com.kairu.core.event.BaseEvent;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class SimpleEventBusTest {
  
  public class ExeptionTest extends RuntimeException {
        public ExeptionTest(String message) {
          super(message);
        }
  }

  static class TestEvent extends BaseEvent {

      public TestEvent(Instant occurredAt) {
            super(occurredAt);
      }
  }

  @Test
  void listenerReceivesPublishedEvent() {
        SimpleEventBus bus = new SimpleEventBus();
        AtomicBoolean received = new AtomicBoolean(false);
        bus.subscribeListener(TestEvent.class, event -> {
            received.set(true);
        });
        bus.publishEvent(new TestEvent(Instant.now()));
        assertTrue(received.get());
  }

  @Test
  void publishingEventWithNoListenersDoesNotCrash() {
      SimpleEventBus bus = new SimpleEventBus();
      TestEvent event = new TestEvent(Instant.now());
      assertDoesNotThrow(() -> bus.publishEvent(event));
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
      bus.subscribeListener(TestEvent.class, event -> {
         received.set(true);
      });
      bus.publishEvent(new AnotherEvent(Instant.now()));
      assertFalse(received.get());
  }

  @Test 
  void twoListenerReceiveSameEvent() {
        SimpleEventBus bus = new SimpleEventBus();

        AtomicBoolean firstListenerExecuted = new AtomicBoolean(false);
        AtomicBoolean secondListenerExecuted = new AtomicBoolean(false);

        bus.subscribeListener(TestEvent.class, event -> {
          firstListenerExecuted.set(true);
        });

        bus.subscribeListener(TestEvent.class, event -> {
          secondListenerExecuted.set(true);
        });

        bus.publishEvent(new TestEvent(Instant.now()));

        assertTrue(firstListenerExecuted.get());
        assertTrue(secondListenerExecuted.get());
  }
    
  @Test
  @Disabled("pausado prq por hr, nao capturamos erros")
  void shouldContinueProcessingWhenListenerThrowsException() {
      SimpleEventBus bus = new SimpleEventBus();
      AtomicBoolean firstreceived = new AtomicBoolean(false);
      AtomicBoolean secondreceived = new AtomicBoolean(false);
      AtomicBoolean thirdreceived = new AtomicBoolean(false);

      bus.subscribeListener(TestEvent.class, event -> {
        firstreceived.set(true);
      });
        
      bus.subscribeListener(TestEvent.class, event -> {
        secondreceived.set(true);
        throw new ExeptionTest("disparou o erro em fio");  
      });
      
      bus.subscribeListener(TestEvent.class, event -> {
        thirdreceived.set(true); 
      });

      assertDoesNotThrow(()-> {
        bus.publishEvent(new TestEvent(Instant.now()));
      });
      assertTrue(firstreceived.get());
      assertTrue(secondreceived.get());
      assertTrue(thirdreceived.get());
  }
}
