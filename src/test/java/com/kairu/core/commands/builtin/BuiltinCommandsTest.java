package com.kairu.core.commands.builtin;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.Bootstrap.Bootstrap;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.session.Tag;
import com.kairu.core.time.ManualClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Scanner;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BuiltinCommandsTest {

    private ApplicationContext context;
    private ManualClock clock;

    @BeforeEach
    void setup() {
        clock = new ManualClock(Instant.now());
        context = Bootstrap.createInMemoryContext(clock);
        context.getTagRepository().save(new Tag("teste"));
        context.getTagRepository().save(new Tag("rust"));
    }

    // --- HelpCommand ---

    @Test
    void helpShouldReturnSuccessWithAllCommands() {
        var result = new HelpCommand().execute(new String[]{});

        assertTrue(result.isSuccess());
        String data = result.getData().orElse("");
        assertTrue(data.contains("start"));
        assertTrue(data.contains("pause"));
        assertTrue(data.contains("resume"));
        assertTrue(data.contains("stop"));
        assertTrue(data.contains("list"));
        assertTrue(data.contains("exit"));
        assertTrue(data.contains("create tag"));
        assertTrue(data.contains("list tag"));
        assertTrue(data.contains("analytics"));
    }

    @Test
    void helpNameAndAliases() {
        HelpCommand cmd = new HelpCommand();
        assertEquals("help", cmd.name());
        assertEquals(Set.of("h", "?"), cmd.aliases());
        assertNotNull(cmd.description());
    }

    // --- ExitCommand ---

    @Test
    void exitShouldReturnExitString() {
        var result = new ExitCommand().execute(new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("exit", result.getData().orElse(null));
    }

    @Test
    void exitNameAndAliases() {
        ExitCommand cmd = new ExitCommand();
        assertEquals("exit", cmd.name());
        assertEquals(Set.of("q", "quit"), cmd.aliases());
    }

    // --- StartCommand ---

    @Test
    void startShouldStartSessionWithValidTag() {
        Scanner scanner = scanner("teste");
        var cmd = new StartCommand(context, scanner);

        var result = cmd.execute(new String[]{});

        assertTrue(result.isSuccess());
        assertNotNull(context.getManager().getCurrentRuntime());
    }

    @Test
    void startShouldFailWithInvalidTag() {
        Scanner scanner = scanner("python");
        var cmd = new StartCommand(context, scanner);

        var result = cmd.execute(new String[]{});

        assertFalse(result.isSuccess());
        assertTrue(result.getError().get().getMessage().contains("Tag invalida"));
    }

    @Test
    void startShouldFailWhenSessionAlreadyRunning() {
        Scanner s1 = scanner("teste");
        new StartCommand(context, s1).execute(new String[]{});

        Scanner s2 = scanner("teste");
        var result = new StartCommand(context, s2).execute(new String[]{});

        assertFalse(result.isSuccess());
        assertTrue(result.getError().get().getMessage().contains("Sessao ja iniciada"));
    }

    @Test
    void startTagName() {
        assertEquals("start", new StartCommand(context, new Scanner(System.in)).name());
    }

    // --- PauseCommand ---

    @Test
    void pauseShouldWorkWhenRunning() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(60);
        var result = new PauseCommand(context).execute(new String[]{});

        assertTrue(result.isSuccess());
    }

    @Test
    void pauseShouldFailWhenNoSession() {
        var result = new PauseCommand(context).execute(new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void pauseShouldFailWhenAlreadyPaused() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(60);
        new PauseCommand(context).execute(new String[]{});

        var result = new PauseCommand(context).execute(new String[]{});
        assertFalse(result.isSuccess());
    }

    @Test
    void pauseName() {
        assertEquals("pause", new PauseCommand(context).name());
    }

    // --- ResumeCommand ---

    @Test
    void resumeShouldWorkWhenPaused() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(60);
        new PauseCommand(context).execute(new String[]{});

        var result = new ResumeCommand(context).execute(new String[]{});
        assertTrue(result.isSuccess());
    }

    @Test
    void resumeShouldFailWhenNotPaused() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(60);

        var result = new ResumeCommand(context).execute(new String[]{});
        assertFalse(result.isSuccess());
    }

    @Test
    void resumeShouldFailWhenNoSession() {
        var result = new ResumeCommand(context).execute(new String[]{});
        assertFalse(result.isSuccess());
    }

    @Test
    void resumeName() {
        assertEquals("resume", new ResumeCommand(context).name());
    }

    // --- StopCommand ---

    @Test
    void stopShouldReturnSuccessForLongSession() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(600);

        var result = new StopCommand(context).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getData().orElse(null));
    }

    @Test
    void stopShouldReturnTooShortForShortSession() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(60);

        var result = new StopCommand(context).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("TOO_SHORT", result.getData().orElse(null));
        assertNotNull(context.getManager().getCurrentRuntime(), "Sessao nao deveria ter sido encerrada");
    }

    @Test
    void stopShouldFailWhenNoSession() {
        var result = new StopCommand(context).execute(new String[]{});

        assertFalse(result.isSuccess());
        assertTrue(result.getError().get().getMessage().contains("Nenhuma sessao ativa"));
    }

    @Test
    void stopName() {
        assertEquals("stop", new StopCommand(context).name());
    }

    // --- CreateTagCommand ---

    @Test
    void createTagShouldCreateTag() {
        Scanner scanner = scanner("python");
        var result = new CreateTagCommand(context, scanner).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertTrue(context.getTagRepository().findByName("python").isPresent());
    }

    @Test
    void createTagShouldFailWithBlankName() {
        Scanner scanner = scanner("   ");
        var result = new CreateTagCommand(context, scanner).execute(new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void createTagShouldFailWhenSessionActive() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        Scanner scanner = scanner("newtag");
        var result = new CreateTagCommand(context, scanner).execute(new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void createTagName() {
        assertEquals("create tag", new CreateTagCommand(context, new Scanner(System.in)).name());
    }

    // --- ListTagCommand ---

    @Test
    void listTagShouldShowExistingTags() {
        var result = new ListTagCommand(context).execute(new String[]{});

        assertTrue(result.isSuccess());
        String data = result.getData().orElse("");
        assertTrue(data.contains("teste"));
        assertTrue(data.contains("rust"));
    }

    @Test
    void listTagShouldShowEmptyWhenNoTags() {
        var emptyContext = Bootstrap.createInMemoryContext(clock);
        var result = new ListTagCommand(emptyContext).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertTrue(result.getData().orElse("").contains("Vazio"));
    }

    @Test
    void listTagName() {
        assertEquals("list tag", new ListTagCommand(context).name());
    }

    // --- ListCommand ---

    @Test
    void listShouldShowEmptyWhenNoSessions() {
        var result = new ListCommand(context).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertTrue(result.getData().orElse("").contains("Vazio"));
    }

    @Test
    void listShouldShowSessions() {
        context.getManager().startSession(context.getTagRepository().findByName("teste").orElseThrow());
        clock.advanceSeconds(600);
        new StopCommand(context).execute(new String[]{});

        var result = new ListCommand(context).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertTrue(result.getData().orElse("").contains("teste"));
    }

    @Test
    void listNameAndAliases() {
        ListCommand cmd = new ListCommand(context);
        assertEquals("list", cmd.name());
        assertEquals(Set.of("ls"), cmd.aliases());
    }

    // --- AnalyticsCommand ---

    @Test
    void analyticsOption7ShouldReturnEmpty() {
        Scanner scanner = scanner("7");
        var result = new AnalyticsCommand(context, scanner).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("", result.getData().orElse(null));
    }

    @Test
    void analyticsOption6ShouldReturnListSessions() {
        Scanner scanner = scanner("6");
        var result = new AnalyticsCommand(context, scanner).execute(new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("list-sessions", result.getData().orElse(null));
    }

    @Test
    void analyticsUnknownOptionShouldFail() {
        Scanner scanner = scanner("99");
        var result = new AnalyticsCommand(context, scanner).execute(new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void analyticsOption1ShouldReturnSummary() {
        Scanner scanner = scanner("1");
        var result = new AnalyticsCommand(context, scanner).execute(new String[]{});

        assertTrue(result.isSuccess());
    }

    @Test
    void analyticsOption3WithInvalidDateShouldFail() {
        Scanner scanner = scanner("3\nnot-a-date");
        var result = new AnalyticsCommand(context, scanner).execute(new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void analyticsOption4WithInvalidNumberShouldFail() {
        Scanner scanner = scanner("4\nabc");
        var result = new AnalyticsCommand(context, scanner).execute(new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void analyticsName() {
        assertEquals("analytics", new AnalyticsCommand(context, new Scanner(System.in)).name());
    }

    // --- CommandBootstrap integration ---

    @Test
    void allCommandsShouldBeRegisteredInCatalog() {
        var catalog = com.kairu.core.Bootstrap.CommandBootstrap.createCatalog(context, new Scanner(System.in));

        assertNotNull(catalog.find("help"));
        assertNotNull(catalog.find("exit"));
        assertNotNull(catalog.find("start"));
        assertNotNull(catalog.find("pause"));
        assertNotNull(catalog.find("resume"));
        assertNotNull(catalog.find("stop"));
        assertNotNull(catalog.find("create tag"));
        assertNotNull(catalog.find("list tag"));
        assertNotNull(catalog.find("list"));
        assertNotNull(catalog.find("analytics"));
    }

    @Test
    void helpAliasesShouldBeRegistered() {
        var catalog = com.kairu.core.Bootstrap.CommandBootstrap.createCatalog(context, new Scanner(System.in));

        assertSame(catalog.find("help").get(), catalog.find("h").get());
        assertSame(catalog.find("help").get(), catalog.find("?").get());
    }

    @Test
    void exitAliasesShouldBeRegistered() {
        var catalog = com.kairu.core.Bootstrap.CommandBootstrap.createCatalog(context, new Scanner(System.in));

        assertSame(catalog.find("exit").get(), catalog.find("q").get());
        assertSame(catalog.find("exit").get(), catalog.find("quit").get());
    }

    @Test
    void listAliasesShouldBeRegistered() {
        var catalog = com.kairu.core.Bootstrap.CommandBootstrap.createCatalog(context, new Scanner(System.in));

        assertSame(catalog.find("list").get(), catalog.find("ls").get());
    }

    // --- helpers ---

    private void startSessionWithTag(String tagName) {
        Scanner scanner = scanner(tagName);
        new StartCommand(context, scanner).execute(new String[]{});
    }

    private Scanner scanner(String input) {
        return new Scanner(new ByteArrayInputStream((input + "\n").getBytes()));
    }
}
