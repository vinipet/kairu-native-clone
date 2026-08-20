package com.kairu.core.commands.builtin;

import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.time.StopResult;

public class StopCommand extends BuiltinCommand {

    private final ApplicationContext context;

    public StopCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "stop";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Finaliza e salva a sessao";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        if (context.getManager().getCurrentRuntime() == null) {
            return CommandResult.failure(new IllegalStateException("Nenhuma sessao ativa."));
        }

        StopResult result = context.getManager().stopSession();

        return CommandResult.success(result.name());
    }
}
