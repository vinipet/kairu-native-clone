package com.kairu;

import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.SessionCompletedPersistenceListener;
import com.kairu.core.session.SessionManager;
import com.kairu.core.time.TimerFactory;
import com.kairu.core.time.ManualClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    private ManualClock clock;
    private SessionManager manager;
    private InMemorySessionRepository repository;

    @BeforeEach
    void setup() throws Exception {
        clock = new ManualClock(Instant.now());
        EventBus bus = new SimpleEventBus();
        TimerFactory factory = new TimerFactory(clock, bus);
        
        manager = new SessionManager(bus, clock, factory);
        repository = new InMemorySessionRepository();
        bus.subscribeListener(SessionCompletedEvent.class, new SessionCompletedPersistenceListener(repository));

        setStaticField("manager", manager);
        setStaticField("repository", repository);
        setStaticField("statusMessage", null);
    }

    @Test
    void testFluxoCompleto_Start_Pause_Resume_Stop() throws Exception {
        invokePrivateMethod("startSession");
        assertNotNull(manager.getCurrentRuntime(), "Sessão deveria ter iniciado");
        
        clock.advanceSeconds(600); 

        invokePrivateMethod("pauseSession");
        assertEquals(10, manager.getCurrentRuntime().getDuration().toMinutes(), "Deveria ter 10 min antes do pause");

        clock.advanceSeconds(300);
        assertEquals(10, manager.getCurrentRuntime().getDuration().toMinutes(), "Tempo não deve correr no pause");

        invokePrivateMethod("resumeSession");
        clock.advanceSeconds(300); // Mais 5 minutos

        invokePrivateMethod("stopSession");
        assertEquals(1, repository.findAll().size(), "Deveria ter salvo uma sessão");
        assertEquals(15, repository.findAll().get(0).getTotalDuration().toMinutes());
    }

    @Test
    void testRegraDeSessaoCurta_NaoDeveSalvarSeMenorQue5Min() throws Exception {
        invokePrivateMethod("startSession");
        
        clock.advanceSeconds(60); // Apenas 1 minuto
        
        invokePrivateMethod("stopSession");

        // Verifica se a mensagem de erro apareceu na UI
        String msg = (String) getStaticField("statusMessage");
        assertTrue(msg != null && msg.contains("curta"), "Deveria mostrar aviso de sessão curta");
        assertEquals(0, repository.findAll().size(), "Não deveria ter salvo sessão curta no repositório");
    }

    @Test
    void testComandoHelp_NaoDeveQuebrar() {
        assertDoesNotThrow(() -> invokePrivateMethod("showHelp"));
    }

    @Test
    void testNaoDeveReiniciarSessaoSeJaEstiverRodando() throws Exception {
      invokePrivateMethod("startSession");
      clock.advanceSeconds(600); // 10 min
      
      invokePrivateMethod("startSession");

      assertEquals(10, manager.getCurrentRuntime().getDuration().toMinutes(), 
        "O tempo não deveria ter sido resetado por um novo start");
    }

    @Test
    void testDeveAcumularVariasSessoesNoHistorico() throws Exception {
      // Sessão 1 (10 min)
      invokePrivateMethod("startSession");
      clock.advanceSeconds(600);
      invokePrivateMethod("stopSession");

      // Sessão 2 (20 min)
      invokePrivateMethod("startSession");
      clock.advanceSeconds(1200);
      invokePrivateMethod("stopSession");

      assertEquals(2, repository.findAll().size(), "Deveria ter 2 sessões no histórico");
    }

    @Test
    void testComandosNaoDevemExplodirSemSessaoAtiva() {
    assertDoesNotThrow(() -> {
        invokePrivateMethod("pauseSession");
        invokePrivateMethod("resumeSession");
        invokePrivateMethod("stopSession");
    }, "Comandos disparados sem sessão ativa não devem lançar exceções");
    }   

    // --- UTILITÁRIOS DE REFLECTION PARA ACESSAR A CLI ---

    private void setStaticField(String fieldName, Object value) throws Exception {
        Field field = Application.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private Object getStaticField(String fieldName) throws Exception {
        Field field = Application.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private void invokePrivateMethod(String methodName) throws Exception {
        java.lang.reflect.Method method = Application.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(null);
    }
}
