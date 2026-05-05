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
  private UUID id1;

  @BeforeEach
  void setup(){
    this.bus = new SimpleEventBus();
    this.clock = new ManualClock(Instant.now());
    this.id1 = UUID.randomUUID();
    this.runtime = new SessionRuntime(id1, bus, clock);
  }
  private void startSession(UUID id) {
    runtime.onEvent(new TimerStartedEvent(clock.now(),id));
    clock.advanceSeconds(10);
  }


  @Test
  public void SessionRuntimeCanDefineCurrentStart(){
   
    runtime.onEvent(new TimerStartedEvent(clock.now(),id1));
    Instant start = clock.now();  
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now(),id1));
      
    Session session = runtime.finishSession();
    assertEquals(start, session.getStartedAt());
  }

  @Test
  public void PauseReallyCreateAIntervals(){
 
    runtime.onEvent(new TimerStartedEvent(clock.now(),id1));
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerPausedEvent(clock.now(),id1));
    clock.advanceSeconds(30);
    runtime.onEvent(new TimerResumeEvent(clock.now(),id1));
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now(),id1));
    
    Session session = runtime.finishSession();
    assertEquals(2, session.getIntervals().size());

  }

  @Test
  public void ThrowExeptionOnDuplicatedEvent(){

    runtime.onEvent(new TimerStartedEvent(clock.now(),id1));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerStartedEvent(clock.now(),id1));
    });
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerPausedEvent(clock.now(),id1));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerPausedEvent(clock.now(),id1));
    });
    clock.advanceSeconds(10);
    runtime.onEvent(new TimerResumeEvent(clock.now(),id1));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerResumeEvent(clock.now(),id1));
    });
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now(),id1));
    assertThrows(IllegalStateException.class, ()->{
      runtime.onEvent(new TimerStoppedEvent(clock.now(),id1));
    });
  }

  @Test
  void idleState_ShouldOnlyAllowStart() {
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStoppedEvent(clock.now(),id1)));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerPausedEvent(clock.now(),id1)));
        
    assertDoesNotThrow(() -> runtime.onEvent(new TimerStartedEvent(clock.now(),id1)));
  }

  @Test
  void runningState_ShouldNotAllowDuplicateStart() {
    startSession(id1);

    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now(),id1)));
        
    assertDoesNotThrow(() -> runtime.onEvent(new TimerPausedEvent(clock.now(),id1)));
  }

  @Test
  void pausedState_ShouldOnlyAllowResume() {
    startSession(id1);
    runtime.onEvent(new TimerPausedEvent(clock.now(),id1)); // Indo para Paused

    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now(),id1)));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerPausedEvent(clock.now(),id1)));
        
    assertDoesNotThrow(() -> runtime.onEvent(new TimerResumeEvent(clock.now(),id1)));
  }

  @Test
  void stoppedState_ShouldLockRuntime() {
    startSession(id1);
    clock.advanceSeconds(1000);
    runtime.onEvent(new TimerStoppedEvent(clock.now(),id1)); 

    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now(),id1)));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerResumeEvent(clock.now(),id1)));
        
    Session session = runtime.finishSession();
    assertNotNull(session);
  }

}
