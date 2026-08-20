package com.kairu.core.commands;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BuiltinCommandTest {

    @Test
    void shouldReturnEmptyAliasesByDefault() {
        BuiltinCommand cmd = new BuiltinCommand() {
            @Override public String name() { return "test"; }
            @Override public String description() { return "test cmd"; }
            @Override protected CommandResult<String> run(String[] args) {
                return CommandResult.success("ok");
            }
        };

        assertTrue(cmd.aliases().isEmpty());
    }

    @Test
    void shouldDelegateExecuteToRun() {
        BuiltinCommand cmd = new BuiltinCommand() {
            @Override public String name() { return "test"; }
            @Override public String description() { return "test cmd"; }
            @Override protected CommandResult<String> run(String[] args) {
                return CommandResult.success("from-run");
            }
        };

        CommandResult<String> result = cmd.execute(new String[]{});
        assertTrue(result.isSuccess());
        assertEquals("from-run", result.getData().orElse(null));
    }

    @Test
    void shouldCatchExceptionFromRun() {
        BuiltinCommand cmd = new BuiltinCommand() {
            @Override public String name() { return "bad"; }
            @Override public String description() { return "bad cmd"; }
            @Override protected CommandResult<String> run(String[] args) {
                throw new RuntimeException("run failed");
            }
        };

        CommandResult<String> result = cmd.execute(new String[]{});
        assertFalse(result.isSuccess());
        assertEquals("run failed", result.getError().orElse(null).getMessage());
    }

    @Test
    void shouldCatchUnsupportedOperationFromRun() {
        BuiltinCommand cmd = new BuiltinCommand() {
            @Override public String name() { return "checked"; }
            @Override public String description() { return "checked cmd"; }
            @Override protected CommandResult<String> run(String[] args) {
                throw new UnsupportedOperationException("not supported");
            }
        };

        CommandResult<String> result = cmd.execute(new String[]{});
        assertFalse(result.isSuccess());
        assertInstanceOf(UnsupportedOperationException.class, result.getError().orElse(null));
    }

    @Test
    void shouldPassArgsThroughToRun() {
        BuiltinCommand cmd = new BuiltinCommand() {
            @Override public String name() { return "args"; }
            @Override public String description() { return "args cmd"; }
            @Override protected CommandResult<String> run(String[] args) {
                return CommandResult.success(String.join(",", args));
            }
        };

        CommandResult<String> result = cmd.execute(new String[]{"x", "y"});
        assertEquals("x,y", result.getData().orElse(null));
    }

    @Test
    void implementsCommandInterface() {
        BuiltinCommand cmd = new BuiltinCommand() {
            @Override public String name() { return "iface"; }
            @Override public String description() { return "interface"; }
            @Override protected CommandResult<String> run(String[] args) {
                return CommandResult.success("ok");
            }
        };

        assertInstanceOf(Command.class, cmd);
    }
}
