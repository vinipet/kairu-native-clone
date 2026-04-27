package com.kairu.core.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;


import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.EventListener;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.event.TimerPausedEvent;
import com.kairu.core.event.TimerResumeEvent;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;
import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.SessionCompletedPersistenceListener;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.session.SessionRuntime;
import com.kairu.core.time.ManualClock;
import com.kairu.core.time.Timer;
import com.kairu.core.time.sessionTimer;

public class SessionFlowTest{

  @Test
  public void basicSessionflow(){
    
    ManualClock clock = new ManualClock(Instant.now());
    EventBus bus = new SimpleEventBus();
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
      
      ManualClock clock = new ManualClock(Instant.now());
      EventBus bus = new SimpleEventBus();
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

    ManualClock clock = new ManualClock(Instant.now());
    EventBus bus = new SimpleEventBus();
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


}
