package com.kairu.core.commands.builtin;

import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;

public class PauseCommand extends BuiltinCommand {

    private final ApplicationContext context;

    public PauseCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "pause";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Pausa a sessao atual";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        context.getManager().pauseSession();
        return CommandResult.success("Sessao pausada.");
    }
}
