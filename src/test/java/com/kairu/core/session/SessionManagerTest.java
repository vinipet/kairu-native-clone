package com.kairu.core.session;

import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.TimerStartedEvent;
import com.kairu.core.event.TimerStoppedEvent;
import com.kairu.core.time.ManualClock;
import com.kairu.core.time.StopResult;
import com.kairu.core.time.TimerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    private SimpleEventBus bus;
    private ManualClock clock;
    private TimerFactory factory;
    private SessionManager manager;

    @BeforeEach
    void setup() {
        bus = new SimpleEventBus();
        clock = new ManualClock(Instant.now());
        factory = new TimerFactory(clock, bus);
        manager = new SessionManager(bus, clock, factory);
    }

    @Test
    void shouldCleanUpBusAfterStoppingSession() {
        manager.startSession();
        clock.advanceSeconds(1000);
        
        assertEquals(StopResult.SUCCESS,manager.stopSession()) ;

        assertDoesNotThrow(() -> manager.startSession(), "Deveria ser possível iniciar nova sessão após o stop");
    }

    @Test
    void shouldThrowExceptionWhenStartingTwoSessions() {
        manager.startSession();
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            manager.startSession();
        });

        assertEquals("there is already an active session", exception.getMessage());
    }

    @Test
    void shouldControlTimerCorrectly() {
        AtomicReference<TimerStartedEvent> startEventRef = new AtomicReference<>();
        AtomicReference<TimerStoppedEvent> stopEventRef = new AtomicReference<>();

        bus.subscribeListener(TimerStartedEvent.class, startEventRef::set);
        bus.subscribeListener(TimerStoppedEvent.class, stopEventRef::set);

        manager.startSession();
        assertNotNull(startEventRef.get(), "O evento de TimerStarted deveria ter sido publicado");

        clock.advanceSeconds(1000);
        
        manager.pauseSession();
        
        manager.stopSession();
        assertNotNull(stopEventRef.get(), "O evento de TimerStopped deveria ter sido publicado");
    }
}
