package com.kairu.core.commands.builtin;

import java.util.List;
import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.session.Session;

public class ListCommand extends BuiltinCommand {

    private final ApplicationContext context;

    public ListCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("ls");
    }

    @Override
    public String description() {
        return "Exibe historico de sessoes";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        List<Session> sessions = context.getSessionRepository().findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("\n\033[1m--- HISTORICO ---\033[0m\n");
        if (sessions.isEmpty()) {
            sb.append("Vazio.\n");
        } else {
            sessions.forEach(s -> sb.append(String.format(
                    "[%s] | %s | %d min | %d intervalos%n",
                    s.getTag() != null ? s.getTag().getName() : "Sem Tag",
                    s.getStartedAt(),
                    s.getTotalDuration().toMinutes(),
                    s.getIntervals().size())));
        }
        sb.append("\n");

        return CommandResult.success(sb.toString());
    }
}
