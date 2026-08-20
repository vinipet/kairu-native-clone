package com.kairu.core.commands.builtin;

import java.util.Set;

import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;

public class HelpCommand extends BuiltinCommand {

    @Override
    public String name() {
        return "help";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("h", "?");
    }

    @Override
    public String description() {
        return "Exibe comandos disponiveis";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nComandos disponiveis:\n\n");

        String format = "  %-12s ------> %s%n";
        sb.append(String.format(format, "start", "Inicia uma nova sessao de foco"));
        sb.append(String.format(format, "pause", "Pausa a sessao atual"));
        sb.append(String.format(format, "resume", "Retoma uma sessao pausada"));
        sb.append(String.format(format, "stop", "Finaliza e salva a sessao"));
        sb.append(String.format(format, "list", "Exibe seu historico de estudos"));
        sb.append(String.format(format, "create tag", "Cria uma nova tag"));
        sb.append(String.format(format, "list tag", "Exibe todas as tags"));
        sb.append(String.format(format, "analytics", "Abre o menu de analytics"));
        sb.append(String.format(format, "exit", "Sai do KAIRu"));
        sb.append("\n");

        return CommandResult.success(sb.toString());
    }
}
