package com.kairu.core.commands.builtin;

import java.util.Set;

import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;

public class ExitCommand extends BuiltinCommand {

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("q", "quit");
    }

    @Override
    public String description() {
        return "Sai do KAIRu";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        return CommandResult.success("exit");
    }
}
