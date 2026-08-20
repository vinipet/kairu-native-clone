package com.kairu.core.commands;

import java.util.Optional;

public final class Dispatcher {

    private final CommandCatalog catalog;

    public Dispatcher(CommandCatalog catalog) {
        this.catalog = catalog;
    }

    public <T> CommandResult<T> dispatch(String name, String[] args) {
        Optional<Command<?>> lookup = catalog.find(name);

        if (lookup.isEmpty()) {
            @SuppressWarnings("unchecked")
            CommandResult<T> notFound = (CommandResult<T>) CommandResult.failure(
                    new IllegalArgumentException("unknown command: " + name));
            return notFound;
        }

        Command<?> command = lookup.get();

        try {
            @SuppressWarnings("unchecked")
            CommandResult<T> result = (CommandResult<T>) command.execute(args);
            return result;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            CommandResult<T> error = (CommandResult<T>) CommandResult.failure(e);
            return error;
        }
    }
}
