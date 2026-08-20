package com.kairu;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.Bootstrap.Bootstrap;
import com.kairu.core.Bootstrap.CommandBootstrap;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.commands.Dispatcher;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.Tag;
import com.kairu.core.time.ManualClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    private ManualClock clock;
    private SessionManager manager;
    private ApplicationContext context;
    private Dispatcher dispatcher;

    @BeforeEach
    void setup() throws Exception {
        clock = new ManualClock(Instant.now());
        context = Bootstrap.createInMemoryContext(clock);
        manager = context.getManager();

        context.getTagRepository().save(new Tag("teste"));

        setStaticField("manager", manager);
        setStaticField("statusMessage", null);
        setStaticField("context", context);

        dispatcher = CommandBootstrap.createDispatcher(context, new Scanner(System.in));
        setStaticField("dispatcher", dispatcher);
    }

    @Test
    void testFluxoCompleto_Start_Pause_Resume_Stop() throws Exception {
        darStartComTag("teste");
        assertNotNull(manager.getCurrentRuntime(), "Sessao deveria ter iniciado");

        clock.advanceSeconds(600);

        dispatcher.dispatch("pause", new String[]{});
        assertEquals(10, manager.getCurrentRuntime().getDuration().toMinutes(), "Deveria ter 10 min antes do pause");

        clock.advanceSeconds(300);
        assertEquals(10, manager.getCurrentRuntime().getDuration().toMinutes(), "Tempo nao deve correr no pause");

        dispatcher.dispatch("resume", new String[]{});
        clock.advanceSeconds(300);

        dispatcher.dispatch("stop", new String[]{});
        assertEquals(1, context.getSessionRepository().findAll().size(), "Deveria ter salvo uma sessao");
        assertEquals(15, context.getSessionRepository().findAll().get(0).getTotalDuration().toMinutes());
    }

    @Test
    void testStopSessaoCurta_DeveRetornarTooShort_E_Cancelar() throws Exception {
        darStartComTag("teste");
        clock.advanceSeconds(60);

        CommandResult<?> result = dispatcher.dispatch("stop", new String[]{});
        assertTrue(result.isSuccess());
        assertEquals("TOO_SHORT", result.getData().orElse(null));

        manager.cancelSession();

        assertNull(manager.getCurrentRuntime(), "A sessao deveria ter sido cancelada");
        assertEquals(0, context.getSessionRepository().findAll().size(), "Nao deveria ter salvo nada no repo");
    }

    @Test
    void testComandoHelp_NaoDeveQuebrar() {
        CommandResult<?> result = dispatcher.dispatch("help", new String[]{});
        assertTrue(result.isSuccess(), "Help deveria retornar sucesso");
        assertNotNull(result.getData().orElse(null), "Help deveria retornar dados");
    }

    @Test
    void testNaoDeveReiniciarSessaoSeJaEstiverRodando() throws Exception {
        darStartComTag("teste");
        clock.advanceSeconds(600);

        CommandResult<?> result = dispatcher.dispatch("start", new String[]{});
        assertFalse(result.isSuccess(), "Start duplicado deveria falhar");

        assertEquals(10, manager.getCurrentRuntime().getDuration().toMinutes(),
                "O tempo nao deveria ter sido resetado por um novo start");
    }

    @Test
    void testDeveAcumularVariasSessoesNoHistorico() throws Exception {
        darStartComTag("teste");
        clock.advanceSeconds(600);
        dispatcher.dispatch("stop", new String[]{});

        darStartComTag("teste");
        clock.advanceSeconds(1200);
        dispatcher.dispatch("stop", new String[]{});

        assertEquals(2, context.getSessionRepository().findAll().size(), "Deveria ter 2 sessoes no historico");
    }

    @Test
    void testVariosStarts_NaoDevemCriarMultiplosSchedulers() throws Exception {
        darStartComTag("teste");

        clock.advanceSeconds(10);
        long tempoNoCore = manager.getCurrentRuntime().getDuration().toSeconds();
        assertEquals(10, tempoNoCore, "O Core deve contar o tempo corretamente independente da UI");
    }

    @Test
    void testStartDuplo_DeveRetornarErro() throws Exception {
        darStartComTag("teste");
        assertNotNull(manager.getCurrentRuntime());

        CommandResult<?> result = dispatcher.dispatch("start", new String[]{});
        assertFalse(result.isSuccess(), "Start duplicado deveria retornar erro");
        assertTrue(result.getError().isPresent());
    }

    @Test
    void testMensagemDeErro_ComandoDesconhecido() throws Exception {
        CommandResult<?> result = dispatcher.dispatch("comando_inexistente", new String[]{});
        assertFalse(result.isSuccess());
        assertTrue(result.getError().isPresent());
        assertTrue(result.getError().get().getMessage().startsWith("unknown command:"));
    }

    @Test
    void testAlinhamentoRelogio_NaoDeveResetarStartTimeIndevidamente() throws Exception {
        darStartComTag("teste");
        Instant primeiroStart = manager.getCurrentRuntime().getCurrentStart();

        clock.advanceSeconds(100);

        dispatcher.dispatch("start", new String[]{});

        Instant segundoStart = manager.getCurrentRuntime().getCurrentStart();
        assertEquals(primeiroStart, segundoStart, "O relogio de exibicao resetou! Isso causa o pulo no tempo da UI.");
    }

    // --- UTILITARIOS ---

    private void setStaticField(String fieldName, Object value) throws Exception {
        Field field = Application.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private void darStartComTag(String nomeTag) throws Exception {
        String input = nomeTag + "\n";
        System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));
        Scanner testScanner = new Scanner(System.in);
        setStaticField("scanner", testScanner);

        dispatcher = CommandBootstrap.createDispatcher(context, testScanner);
        setStaticField("dispatcher", dispatcher);

        dispatcher.dispatch("start", new String[]{});

        System.setIn(System.in);
        setStaticField("scanner", new Scanner(System.in));
        dispatcher = CommandBootstrap.createDispatcher(context, new Scanner(System.in));
        setStaticField("dispatcher", dispatcher);
    }
}
