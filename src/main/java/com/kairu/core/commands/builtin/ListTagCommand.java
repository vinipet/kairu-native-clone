package com.kairu.core.commands.builtin;

import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;

public class ListTagCommand extends BuiltinCommand {

    private final ApplicationContext context;

    public ListTagCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "list tag";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Exibe todas as tags";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        Set<com.kairu.core.session.Tag> tags = context.getTagRepository().findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("\n\033[1m--- TAGS ---\033[0m\n");
        if (tags.isEmpty()) {
            sb.append("Vazio");
        } else {
            tags.forEach(t -> sb.append(String.format("- %s%n", t.getName())));
        }
        sb.append("\n");

        return CommandResult.success(sb.toString());
    }
}
