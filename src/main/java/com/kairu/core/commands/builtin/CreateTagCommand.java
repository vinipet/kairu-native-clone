package com.kairu.core.commands.builtin;

import java.util.Scanner;
import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.session.Tag;

public class CreateTagCommand extends BuiltinCommand {

    private final ApplicationContext context;
    private final Scanner scanner;

    public CreateTagCommand(ApplicationContext context, Scanner scanner) {
        this.context = context;
        this.scanner = scanner;
    }

    @Override
    public String name() {
        return "create tag";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Cria uma nova tag";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        if (context.getManager().getCurrentRuntime() != null) {
            return CommandResult.failure(new IllegalStateException("Vai estudar, FOCO!!!"));
        }

        System.out.print("\033[1m\033[31mEscolha o nome da Tag: \033[0m");
        String choice = scanner.nextLine();

        if (choice == null || choice.isBlank()) {
            return CommandResult.failure(new IllegalArgumentException("O nome da tag nao pode ser vazio!"));
        }

        Tag tag = new Tag(choice);
        context.getTagRepository().save(tag);

        return CommandResult.success("Tag '" + choice + "' criada com sucesso!");
    }
}
