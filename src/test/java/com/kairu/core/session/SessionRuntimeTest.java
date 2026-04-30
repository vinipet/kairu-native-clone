package com.kairu.core.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kairu.core.bus.*;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;
import com.kairu.core.time.*;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;

public class SessionRuntimeTest{

  private SimpleEventBus bus;
  private ManualClock clock;
  private SessionRuntime runtime;
  
  @BeforeEach
  void setup(){
    bus = new SimpleEventBus();
    clock = new ManualClock(Instant.now());
    UUID id1 = UUID.randomUUID();
    runtime = new SessionRuntime(id1, bus, clock);
  }
  private void startSession() {
    runtime.onEvent(new TimerStartedEvent(clock.now()));
    clock.advanceSeconds(10);
  }


  @Test
  public void SessionRuntimeCanDefineCurrentStart(){
   
    runtime.onEvent(new TimerStartedEvent(clock.now()));
    Instant start = clock.now();  
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now()));
      
    Session session = runtime.finishSession();
    assertEquals(start, session.getStartedAt());
  }

  @Test
  public void PauseReallyCreateAIntervals(){
 
    runtime.onEvent(new TimerStartedEvent(clock.now()));
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerPausedEvent(clock.now()));
    clock.advanceSeconds(30);
    runtime.onEvent(new TimerResumeEvent(clock.now()));
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now()));
    
    Session session = runtime.finishSession();
    assertEquals(2, session.getIntervals().size());

  }

  @Test
  public void ThrowExeptionOnDuplicatedEvent(){

    runtime.onEvent(new TimerStartedEvent(clock.now()));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerStartedEvent(clock.now()));
    });
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerPausedEvent(clock.now()));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerPausedEvent(clock.now()));
    });
    clock.advanceSeconds(10);
    runtime.onEvent(new TimerResumeEvent(clock.now()));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerResumeEvent(clock.now()));
    });
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now()));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerStoppedEvent(clock.now()));
    });
  }

  @Test
  void idleState_ShouldOnlyAllowStart() {
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStoppedEvent(clock.now())));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerPausedEvent(clock.now())));
        
    assertDoesNotThrow(() -> runtime.onEvent(new TimerStartedEvent(clock.now())));
  }

  @Test
  void runningState_ShouldNotAllowDuplicateStart() {
    startSession(); // Helper para ir ao estado Running

    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now())));
        
    assertDoesNotThrow(() -> runtime.onEvent(new TimerPausedEvent(clock.now())));
  }

  @Test
  void pausedState_ShouldOnlyAllowResume() {
    startSession();
    runtime.onEvent(new TimerPausedEvent(clock.now())); // Indo para Paused

    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now())));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerPausedEvent(clock.now())));
        
    assertDoesNotThrow(() -> runtime.onEvent(new TimerResumeEvent(clock.now())));
  }

  @Test
  void stoppedState_ShouldLockRuntime() {
    startSession();
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now())); 

    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now())));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerResumeEvent(clock.now())));
        
    Session session = runtime.finishSession();
    assertNotNull(session);
  }

}
