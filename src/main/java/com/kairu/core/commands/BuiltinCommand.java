package com.kairu.core.commands;

import java.util.Set;

public abstract class BuiltinCommand implements Command<String> {

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public CommandResult<String> execute(String[] args) {
        try {
            return run(args);
        } catch (Exception e) {
            return CommandResult.failure(e);
        }
    }

    protected abstract CommandResult<String> run(String[] args);
}
