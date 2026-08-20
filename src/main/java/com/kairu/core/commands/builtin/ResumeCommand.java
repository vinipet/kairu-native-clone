package com.kairu.core.commands.builtin;

import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;

public class ResumeCommand extends BuiltinCommand {

    private final ApplicationContext context;

    public ResumeCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "resume";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Retoma uma sessao pausada";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        context.getManager().resumeSession();
        return CommandResult.success("Sessao retomada.");
    }
}
