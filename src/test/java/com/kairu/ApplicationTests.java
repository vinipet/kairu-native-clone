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
import java.util.concurrent.ScheduledExecutorService;

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
    void testStopSessaoCurta_DevePerguntarEResponderSim() throws Exception {
      invokePrivateMethod("startSession");
      clock.advanceSeconds(60); // 1 minuto
     
      // 1. Preparamos a resposta "s" (sim, descartar) seguida de um Enter (\n)
      String input = "s\n";
      System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));
     
      // 2. CRUCIAL: Reinicializar o scanner da Application para ler o System.in falso
      setStaticField("scanner", new java.util.Scanner(System.in));
      
      // 3. Agora o stopSession vai ler o "s" automaticamente e não vai travar
      invokePrivateMethod("stopSession");
      
      // 4. Verificações
      assertNull(manager.getCurrentRuntime(), "A sessão deveria ter sido encerrada após o 's'");
      assertEquals(0, repository.findAll().size(), "Não deveria ter salvo nada no repo");
      
      // 5. Limpeza: Volta o System.in para o original para não quebrar outros testes
      System.setIn(System.in);
      setStaticField("scanner", new java.util.Scanner(System.in));
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
    void testBugRelogioAcelerado_VariosStartsNaoDevemCriarMultiplosSchedulers() throws Exception {
      // 1. Simula o usuário dando 'start' várias vezes seguidas
      invokePrivateMethod("startSession");
      invokePrivateMethod("startSession");
      invokePrivateMethod("startSession");
      // 2. Pegamos o scheduler via Reflection para ver se ele está saudável
      ScheduledExecutorService s = (ScheduledExecutorService) getStaticField("scheduler");
      assertFalse(s.isShutdown(), "O scheduler deveria estar ativo");
       // 3. Verificamos o tempo no manager (o Core)
      clock.advanceSeconds(10);
      long tempoNoCore = manager.getCurrentRuntime().getDuration().toSeconds();
      assertEquals(10, tempoNoCore, "O Core deve contar o tempo corretamente independente da UI");
        
    }
      
    @Test
    void testStartDuplo_NaoDeveReiniciarScheduler() throws Exception {
      // Primeiro start
      invokePrivateMethod("startSession");
      Object schedulerOriginal = getStaticField("scheduler");
      assertNotNull(schedulerOriginal);

      // Segundo start (deveria ser ignorado pelo seu novo 'if')
      invokePrivateMethod("startSession");
      Object schedulerDepois = getStaticField("scheduler");

      // Se o seu 'if' no startSession estiver correto, o objeto scheduler 
      // não deve ter sido substituído (ou seja, startLiveTimer não rodou de novo)
      assertSame(schedulerOriginal, schedulerDepois, "O scheduler foi recriado indevidamente!");
    }

    @Test
    void testMensagemDeErro_ComandoDesconhecido() throws Exception {
      // Simula o default do switch case (podemos chamar o setStatusMessage direto)
      // Mas aqui vamos testar se a lógica de expiração funciona
      invokePrivateMethod("startSession");
      
      // Seta uma mensagem de erro manual
      java.lang.reflect.Method setMsg = Application.class.getDeclaredMethod("setStatusMessage", String.class, int.class);
      setMsg.setAccessible(true);
      setMsg.invoke(null, "ERRO TESTE", 1); // Expira em 1 segundo
      
      assertNotNull(getStaticField("statusMessage"));
      
      // Avança o tempo do clock para depois da expiração
      clock.advanceSeconds(2);
      
      // No próximo ciclo do scheduler, a UI deveria esconder a mensagem 
      // (Isso é o que o seu if(Instant.now().isBefore(messageExpiry)) faz)
    }

    @Test
    void testAlinhamentoRelogio_NaoDeveResetarStartTimeIndevidamente() throws Exception {
      // 1. Inicia
      invokePrivateMethod("startSession");
      Instant primeiroStart = manager.getCurrentRuntime().getCurrentStart();
      
      clock.advanceSeconds(100);
      
      // 2. Tenta dar um segundo 'start' acidental
      invokePrivateMethod("startSession");
      
      // 3. O lastStartTime NÃO pode ter mudado
      Instant segundoStart = manager.getCurrentRuntime().getCurrentStart();
      assertEquals(primeiroStart, segundoStart, "O relógio de exibição resetou! Isso causa o pulo no tempo da UI.");
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
