package com.kairu.core.Bootstrap;

import java.util.Scanner;

import com.kairu.core.commands.CommandCatalog;
import com.kairu.core.commands.Dispatcher;
import com.kairu.core.commands.builtin.*;

public class CommandBootstrap {

    public static CommandCatalog createCatalog(ApplicationContext context, Scanner scanner) {
        CommandCatalog catalog = new CommandCatalog();

        catalog.register(new HelpCommand());
        catalog.register(new ExitCommand());
        catalog.register(new StartCommand(context, scanner));
        catalog.register(new PauseCommand(context));
        catalog.register(new ResumeCommand(context));
        catalog.register(new StopCommand(context));
        catalog.register(new CreateTagCommand(context, scanner));
        catalog.register(new ListTagCommand(context));
        catalog.register(new ListCommand(context));
        catalog.register(new AnalyticsCommand(context, scanner));

        return catalog;
    }

    public static Dispatcher createDispatcher(ApplicationContext context, Scanner scanner) {
        CommandCatalog catalog = createCatalog(context, scanner);
        return new Dispatcher(catalog);
    }
}
