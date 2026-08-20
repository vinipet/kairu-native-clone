package com.kairu.core.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandResultTest {

    @Test
    void successShouldHaveDataAndNoError() {
        CommandResult<String> result = CommandResult.success("ok");

        assertTrue(result.isSuccess());
        assertEquals("ok", result.getData().orElse(null));
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void failureShouldHaveErrorAndNoData() {
        CommandResult<String> result = CommandResult.failure(new RuntimeException("fail"));

        assertFalse(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        assertEquals("fail", result.getError().orElse(null).getMessage());
    }

    @Test
    void failureWithNullMessage() {
        CommandResult<String> result = CommandResult.failure(new NullPointerException());

        assertFalse(result.isSuccess());
        assertTrue(result.getError().isPresent());
    }

    @Test
    void successWithDifferentTypes() {
        CommandResult<Integer> intResult = CommandResult.success(42);
        assertEquals(42, intResult.getData().orElse(null));

        CommandResult<Double> doubleResult = CommandResult.success(3.14);
        assertEquals(3.14, doubleResult.getData().orElse(null));
    }

    @Test
    void failureShouldPreserveExceptionType() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        CommandResult<String> result = CommandResult.failure(ex);

        assertInstanceOf(IllegalArgumentException.class, result.getError().orElse(null));
        assertEquals("bad arg", result.getError().orElse(null).getMessage());
    }

    @Test
    void successAndFailureShouldBeDistinctObjects() {
        CommandResult<String> ok = CommandResult.success("data");
        CommandResult<String> fail = CommandResult.failure(new Exception("err"));

        assertNotEquals(ok.isSuccess(), fail.isSuccess());
    }
}
