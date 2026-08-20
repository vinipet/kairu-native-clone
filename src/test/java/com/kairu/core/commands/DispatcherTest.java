package com.kairu.core.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

    private CommandCatalog catalog;
    private Dispatcher dispatcher;

    @BeforeEach
    void setup() {
        catalog = new CommandCatalog();
        dispatcher = new Dispatcher(catalog);
    }

    @Test
    void shouldReturnFailureForUnknownCommand() {
        CommandResult<?> result = dispatcher.dispatch("nope", new String[]{});

        assertFalse(result.isSuccess());
        assertTrue(result.getError().isPresent());
        assertTrue(result.getError().get().getMessage().startsWith("unknown command: "));
    }

    @Test
    void shouldReturnFailureForNullCommand() {
        CommandResult<?> result = dispatcher.dispatch(null, new String[]{});

        assertFalse(result.isSuccess());
        assertTrue(result.getError().get().getMessage().startsWith("unknown command: null"));
    }

    @Test
    void shouldReturnFailureForEmptyString() {
        CommandResult<?> result = dispatcher.dispatch("", new String[]{});

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldExecuteRegisteredCommand() {
        catalog.register(new StubCommand("hello", "Hello!"));
        CommandResult<?> result = dispatcher.dispatch("hello", new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("Hello!", result.getData().orElse(null));
    }

    @Test
    void shouldExecuteCommandViaAlias() {
        Command<String> cmd = new Command<>() {
            @Override public String name() { return "hello"; }
            @Override public Set<String> aliases() { return Set.of("hi"); }
            @Override public String description() { return "greet"; }
            @Override
            public CommandResult<String> execute(String[] args) {
                return CommandResult.success("Hello!");
            }
        };
        catalog.register(cmd);

        CommandResult<?> result = dispatcher.dispatch("hi", new String[]{});

        assertTrue(result.isSuccess());
        assertEquals("Hello!", result.getData().orElse(null));
    }

    @Test
    void shouldPassArgsToCommand() {
        Command<String> cmd = new Command<>() {
            @Override public String name() { return "echo"; }
            @Override public Set<String> aliases() { return Set.of(); }
            @Override public String description() { return "echo"; }
            @Override
            public CommandResult<String> execute(String[] args) {
                return CommandResult.success(String.join(" ", args));
            }
        };
        catalog.register(cmd);

        CommandResult<?> result = dispatcher.dispatch("echo", new String[]{"a", "b", "c"});
        assertEquals("a b c", result.getData().orElse(null));
    }

    @Test
    void shouldCatchExceptionThrownByCommand() {
        Command<String> exploding = new Command<>() {
            @Override public String name() { return "explode"; }
            @Override public Set<String> aliases() { return Set.of(); }
            @Override public String description() { return "boom"; }
            @Override
            public CommandResult<String> execute(String[] args) {
                throw new RuntimeException("kaboom");
            }
        };
        catalog.register(exploding);

        CommandResult<?> result = dispatcher.dispatch("explode", new String[]{});

        assertFalse(result.isSuccess());
        assertEquals("kaboom", result.getError().orElse(null).getMessage());
    }

    @Test
    void shouldCatchRuntimeExceptionFromCommand() {
        Command<String> cmd = new Command<>() {
            @Override public String name() { return "bad"; }
            @Override public Set<String> aliases() { return Set.of(); }
            @Override public String description() { return "bad"; }
            @Override
            public CommandResult<String> execute(String[] args) {
                throw new IllegalArgumentException("invalid state");
            }
        };
        catalog.register(cmd);

        CommandResult<?> result = dispatcher.dispatch("bad", new String[]{});

        assertFalse(result.isSuccess());
        assertInstanceOf(IllegalArgumentException.class, result.getError().orElse(null));
    }

    @Test
    void shouldNotMutateCatalogOnDispatch() {
        catalog.register(new StubCommand("a", "data-a"));

        dispatcher.dispatch("a", new String[]{});
        dispatcher.dispatch("unknown", new String[]{});

        assertEquals(1, catalog.findAll().size());
    }

    @Test
    void shouldFindCommandCaseInsensitively() {
        catalog.register(new StubCommand("Start", "started"));

        CommandResult<?> result = dispatcher.dispatch("START", new String[]{});
        assertTrue(result.isSuccess());
    }

    // --- Stub ---

    private static class StubCommand implements Command<String> {
        private final String name;
        private final String responseData;

        StubCommand(String name, String responseData) {
            this.name = name;
            this.responseData = responseData;
        }

        @Override public String name() { return name; }
        @Override public Set<String> aliases() { return Set.of(); }
        @Override public String description() { return "stub"; }

        @Override
        public CommandResult<String> execute(String[] args) {
            return CommandResult.success(responseData);
        }
    }
}
