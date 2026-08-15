package com.kairu.core.commands;

import java.util.Optional;

public final class CommandResult<T> {

    private final Optional<T> data;
    private final Optional<Throwable> error;

    private CommandResult(Optional<T> data, Optional<Throwable> error) {
        this.data = data;
        this.error = error;
    }

    public static <T> CommandResult<T> success(T data) {
        return new CommandResult<>(Optional.of(data), Optional.empty());
    }

    public static <T> CommandResult<T> failure(Throwable error) {
        return new CommandResult<>(Optional.empty(), Optional.of(error));
    }

    public Optional<T> getData(){
      return data;
    }
    
    public Optional<Throwable> getError(){
      return error;
    }
    
    public boolean isSuccess() {
      return data.isPresent();
    }
}
