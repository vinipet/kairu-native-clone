package com.kairu.core.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;


import com.kairu.core.bus.EventListener;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;
import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.Session;
import com.kairu.core.session.SessionCompletedPersistenceListener;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.session.SessionRuntime;
import com.kairu.core.time.ManualClock;
import com.kairu.core.time.Timer;
import com.kairu.core.time.sessionTimer;

public class SessionFlowTest{


    private SimpleEventBus bus;
    private ManualClock clock;
    private SessionRuntime runtime;

  @BeforeEach
  void setup() {
    bus = new SimpleEventBus();
    clock = new ManualClock(Instant.now());
    runtime = new SessionRuntime(1, bus, clock);
  } 

  private void startSession() {
    runtime.onEvent(new TimerStartedEvent(clock.now()));
    clock.advanceSeconds(10);
  }

  @Test
  public void basicSessionflow(){
    
    SessionRepository repository = new InMemorySessionRepository();
    EventListener<SessionCompletedEvent> listener = new SessionCompletedPersistenceListener(repository); 
    sessionTimer timer = new sessionTimer(clock, bus);

    bus.subscribeListener(SessionCompletedEvent.class, listener);
    bus.subscribeListener(TimerStartedEvent.class, runtime );
    bus.subscribeListener(TimerStoppedEvent.class, runtime );
    bus.subscribeListener(TimerResumeEvent.class, runtime );
    bus.subscribeListener(TimerPausedEvent.class, runtime );
    
    timer.start();
    clock.advanceSeconds(600);
    timer.pause();
    clock.advanceSeconds(300);
    timer.resume();
    clock.advanceSeconds(600);
    timer.stop();

    assertEquals(1, repository.findAll().size());
    assertEquals(20, repository.findById(1).get().getTotalDuration().toMinutes());
    assertEquals(2, repository.findById(1).get().getIntervals().size());


  }

  @Test
  public void sessionUnderFiveMinutesThrowError(){
    assertThrows(IllegalStateException.class , ()->{
      
      Timer timer = new sessionTimer(clock, bus);
      SessionRepository repository = new InMemorySessionRepository();
      EventListener<SessionCompletedEvent> listener = new SessionCompletedPersistenceListener(repository); 

      SessionRuntime sessionRuntime = new SessionRuntime(1, bus, clock);

      bus.subscribeListener(SessionCompletedEvent.class, listener);
      bus.subscribeListener(TimerStartedEvent.class, sessionRuntime );
      bus.subscribeListener(TimerResumeEvent.class, sessionRuntime );
      bus.subscribeListener(TimerPausedEvent.class, sessionRuntime );
      bus.subscribeListener(TimerStoppedEvent.class, sessionRuntime );
      timer.start();
      clock.advanceSeconds(50);
      timer.stop();
    });

  }

  @Test
  public void StopAfterPauseGiveOneInterval(){

    Timer timer = new sessionTimer(clock, bus);
    SessionRepository repository = new InMemorySessionRepository();
    EventListener<SessionCompletedEvent> listener = new SessionCompletedPersistenceListener(repository); 

    SessionRuntime sessionRuntime = new SessionRuntime(1, bus, clock);

    bus.subscribeListener(SessionCompletedEvent.class, listener);
    bus.subscribeListener(TimerStartedEvent.class, sessionRuntime );
    bus.subscribeListener(TimerStoppedEvent.class, sessionRuntime );
    bus.subscribeListener(TimerResumeEvent.class, sessionRuntime );
    bus.subscribeListener(TimerPausedEvent.class, sessionRuntime );
    
    timer.start();
    clock.advanceSeconds(1000);
    timer.pause();
    timer.stop();
    
    assertEquals(1, repository.findAll().getFirst().getIntervals().size());

    
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
    runtime.onEvent(new TimerStoppedEvent(clock.now())); // Finalizando
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerStartedEvent(clock.now())));
    assertThrows(IllegalStateException.class, () -> runtime.onEvent(new TimerResumeEvent(clock.now())));
        
    Session session = runtime.finishSession();
    assertNotNull(session);
  }
}
