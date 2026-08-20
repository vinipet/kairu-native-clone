package com.kairu.core.commands.builtin;

import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.Tag;

public class StartCommand extends BuiltinCommand {

    private final ApplicationContext context;
    private final Scanner scanner;

    public StartCommand(ApplicationContext context, Scanner scanner) {
        this.context = context;
        this.scanner = scanner;
    }

    @Override
    public String name() {
        return "start";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Inicia uma nova sessao de foco";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        SessionManager manager = context.getManager();

        if (manager.getCurrentRuntime() != null) {
            return CommandResult.failure(new IllegalStateException("Sessao ja iniciada."));
        }

        System.out.print("\033[1m\033[31mDigite a tag da sessao:\033[0m");

        Set<Tag> tags = context.getTagRepository().findAll();
        if (tags.isEmpty()) {
            System.out.println("\nVazio");
        } else {
            System.out.println();
            tags.forEach(t -> System.out.printf("- %s%n", t.getName()));
        }

        String choice = scanner.nextLine().toLowerCase().trim();
        Optional<Tag> tagEncontrada = context.getTagRepository().findByName(choice);

        if (tagEncontrada.isPresent()) {
            System.out.print("\033[1A\r\033[2K");
            manager.startSession(tagEncontrada.get());
            return CommandResult.success("Sessao iniciada com tag: " + tagEncontrada.get().getName());
        } else {
            return CommandResult.failure(new IllegalArgumentException("Tag invalida."));
        }
    }
}
