package com.kairu;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.Bootstrap.Bootstrap;
import com.kairu.core.Bootstrap.CommandBootstrap;
import com.kairu.core.commands.CommandResult;
import com.kairu.core.commands.Dispatcher;
import com.kairu.core.persistence.PersistencePaths;
import com.kairu.core.session.SessionManager;
import com.kairu.core.time.Timer;
import com.kairu.core.time.RealClock;

public class Application {

    private static final String SAVE_CURSOR = "\033[s";
    private static final String RESTORE_CURSOR = "\033[u";
    private static final String MOVE_UP = "\033[1A";
    private static final String ERASE_LINE = "\033[2K";
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String RED = "\u001B[31m";
    private static final String BOLD = "\u001B[1m";

    private static SessionManager manager;
    private static ApplicationContext context;
    private static Dispatcher dispatcher;
    private static ScheduledExecutorService scheduler;
    private static Scanner scanner = new Scanner(System.in);

    private static String statusMessage = null;
    private static Instant messageExpiry = null;

    public static void main(String[] args) {
        context = Bootstrap.createFileContext(new RealClock(ZoneId.systemDefault()));
        manager = context.getManager();
        dispatcher = CommandBootstrap.createDispatcher(context, scanner);

        showBanner();
        System.out.println("\n\n");

        while (true) {
            System.out.print(BOLD + CYAN + "kairu " + PersistencePaths.env + getPromptTagSuffix() + "> " + RESET);
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().toLowerCase().trim();

            if (input.isEmpty()) continue;

            if (input.equals("exit")) {
                if (confirmExit()) break;
                else continue;
            }

            stopLiveTimer();

            CommandResult<?> result = dispatcher.dispatch(input, new String[]{});

            if (result.isSuccess()) {
                handleSuccessResult(input, result);
            } else {
                handleFailureResult(result);
            }

            if (manager.getCurrentRuntime() != null) {
                startLiveTimer();
            }
        }

        stopLiveTimer();
        System.out.println(BLUE + "Ate logo!" + RESET);
        System.exit(0);
    }

    private static void handleSuccessResult(String input, CommandResult<?> result) {
        Object data = result.getData().orElse(null);

        if (data instanceof String text && !text.isEmpty()) {
            if (text.equals("list-sessions")) {
                printListSessions();
            } else if (text.equals("TOO_SHORT")) {
                handleTooShortStop();
            } else if (text.equals("SUCCESS")) {
                System.out.println(BLUE + "\r✔ Sessao salva!" + RESET + "\n\n");
            } else {
                System.out.println(text);
            }
        }
    }

    private static void handleTooShortStop() {
        while (true) {
            System.out.print(RED + BOLD + "\r* Sessao muito curta (" + manager.getCurrentRuntime().getDuration().toMinutes() + "min). " +
                    "Deseja descarta-la e encerrar? [s/n]: " + RESET);
            String choice = scanner.nextLine().toLowerCase().trim();

            if (choice.equals("s")) {
                manager.cancelSession();
                System.out.println(YELLOW + "\r" + ERASE_LINE + "Sessao descartada." + RESET + "\n");
                break;
            } else if (choice.equals("n")) {
                System.out.print(MOVE_UP + "\r" + ERASE_LINE);
                startLiveTimer();
                break;
            } else {
                System.out.print(YELLOW + "\r* Opcao invalida! Digite 's' ou 'n'." + RESET);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                System.out.print("\r" + ERASE_LINE);
            }
        }
    }

    private static void handleFailureResult(CommandResult<?> result) {
        Throwable error = result.getError().orElse(null);
        if (error == null) return;

        if (error instanceof IllegalArgumentException && error.getMessage() != null
                && error.getMessage().startsWith("unknown command:")) {
            setStatusMessage(YELLOW + "Comando desconhecido." + RESET, 3);
        } else {
            setStatusMessage(RED + "Erro: " + error.getMessage() + RESET, 5);
        }
    }

    private static boolean confirmExit() {
        if (manager.getCurrentRuntime() == null) {
            return true;
        }

        stopLiveTimer();
        System.out.print(RED + BOLD + "\r* Sessao em progresso! Deseja cancelar e sair? [s/n]: " + RESET);
        String choice = scanner.nextLine().toLowerCase().trim();

        if (choice.equals("s")) {
            return true;
        } else {
            System.out.print(MOVE_UP + "\r" + ERASE_LINE);
            startLiveTimer();
            return false;
        }
    }

    private static void printListSessions() {
        var sessions = context.getSessionRepository().findAll();
        System.out.println(BOLD + "\n--- HISTORICO ---" + RESET);
        if (sessions.isEmpty()) {
            System.out.println("Vazio.");
        } else {
            sessions.forEach(s -> System.out.printf(
                    "[%s] | %s | %d min | %d intervalos%n",
                    s.getTag() != null ? s.getTag().getName() : "Sem Tag",
                    s.getStartedAt(),
                    s.getTotalDuration().toMinutes(),
                    s.getIntervals().size()));
        }
        System.out.println();
    }

    private static void setStatusMessage(String message, int seconds) {
        if (manager.getCurrentRuntime() != null) {
            statusMessage = message;
            messageExpiry = Instant.now().plusSeconds(seconds);
        } else {
            System.out.println(message);
        }
    }

    private static void startLiveTimer() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        String[] spinner = {"⏳", "⌛"};

        scheduler.scheduleAtFixedRate(() -> {
            System.out.print(SAVE_CURSOR);
            System.out.print(MOVE_UP + "\r" + ERASE_LINE);

            if (statusMessage != null && Instant.now().isBefore(messageExpiry)) {
                System.out.print(statusMessage);
            } else if (manager.getCurrentRuntime() != null) {
                Duration totalDisplay = manager.getCurrentRuntime().getDuration();

                String time = String.format("%02d:%02d:%02d",
                        totalDisplay.toHours(), totalDisplay.toMinutesPart(), totalDisplay.toSecondsPart());

                if (!(Timer.State.PAUSED == manager.getTimer().getState())) {
                    String icon = spinner[(int) (System.currentTimeMillis() / 500 % spinner.length)];
                    System.out.print(YELLOW + icon + " Foco: " + BOLD + time + RESET + " | 'stop' para encerrar");
                } else {
                    System.out.print(RED + BOLD + "|| PAUSADO (" + time + ")" + RESET);
                }
            }

            System.out.print(RESTORE_CURSOR);
            System.out.flush();
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    private static void stopLiveTimer() {
        if (scheduler != null) scheduler.shutdownNow();
        System.out.print(SAVE_CURSOR + MOVE_UP + "\r" + ERASE_LINE + RESTORE_CURSOR);
        System.out.flush();
    }

    private static void showBanner() {
        System.out.println(BOLD + BLUE + "=== KAIRU PRODUCTIVITY TIMER ===" + RESET);
    }

    private static String getPromptTagSuffix() {
        var runtime = manager.getCurrentRuntime();

        if (runtime == null) {
            return "";
        }
        return " " + YELLOW + "[" + manager.getCurrentRuntime().getTag().getName() + "]" + CYAN;
    }
}
