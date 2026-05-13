package com.kairu;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.session.*;
import com.kairu.core.time.*;

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
    private static SessionRepository repository;
    private static ScheduledExecutorService scheduler;
    private static Scanner scanner = new Scanner(System.in);
    
    private static Instant lastStartTime;

    // Sistema de Notificação no Timer
    private static String statusMessage = null;
    private static Instant messageExpiry = null;

    public static void main(String[] args) {
        EventBus bus = new SimpleEventBus();
        Clock clock = new RealClock();
        TimerFactory factory = new TimerFactory(clock, bus);
        manager = new SessionManager(bus, clock, factory);
        repository = new InMemorySessionRepository();
        
        bus.subscribeListener(SessionCompletedEvent.class, new SessionCompletedPersistenceListener(repository));

        showBanner();
        System.out.println("\n\n"); 

        while (true) {
            System.out.print(BOLD + CYAN + "kairu > " + RESET);
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().toLowerCase().trim();

            if (input.equals("exit")) {
                if (confirmExit()) break;
                else continue;
            }

            switch (input) {
                case "start"  -> startSession();
                case "pause"  -> pauseSession();
                case "resume" -> resumeSession();
                case "stop"   -> stopSession();
                case "list"   -> listSessions();
                case "help"   -> showHelp();
                case ""       -> {}
                default       -> setStatusMessage(YELLOW + "Comando desconhecido." + RESET, 3);
            }
        }
        
        stopLiveTimer();
        System.out.println(BLUE + "Até logo!" + RESET);
        System.exit(0);
    }
    private static void setStatusMessage(String message, int seconds) {
        statusMessage = message;
        messageExpiry = Instant.now().plusSeconds(seconds);
    }

    private static boolean confirmExit() {
        if(manager.getCurrentRuntime() != null) return true;
        
        stopLiveTimer(); // Para o timer para não bagunçar a pergunta
        System.out.print(RED + BOLD + "\r⚠ Sessão em progresso! Deseja cancelar e sair? [s/n]: " + RESET);
        String choice = scanner.nextLine().toLowerCase().trim();
        
        if (choice.equals("s")) {
            return true;
        } else {
            System.out.print(MOVE_UP + "\r" + ERASE_LINE); // Limpa a pergunta
            startLiveTimer(); // Reinicia o timer visual
            return false;
        }
    }

    private static void startSession() {
        if (manager.getCurrentRuntime() != null) return;
        manager.startSession();
        lastStartTime = Instant.now();
        startLiveTimer();
    }

    private static void pauseSession() {
        if (manager.getCurrentRuntime() != null && manager.getTimer().getState() == Timer.State.RUNNING) {
            manager.pauseSession();
        }
    }

    private static void resumeSession() {
        if (manager.getCurrentRuntime() != null && manager.getTimer().getState() == Timer.State.PAUSED) {
            manager.resumeSession();
            lastStartTime = Instant.now();
        }
    }

    private static void stopSession() {
        if (manager.getCurrentRuntime() == null) return;

        StopResult result = manager.stopSession();
        
        if (result == StopResult.TOO_SHORT) {
            setStatusMessage(RED + BOLD + "⚠ Sessão muito curta (mín. 5min)!" + RESET, 5);
        } else {
            stopLiveTimer();
              System.out.println(BLUE + "\r✔ Sessão salva!" + RESET + "\n\n");
        }
    }

    private static void startLiveTimer() {
        if (scheduler != null) scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        String[] spinner = { "⏳", "⌛"};
        
        scheduler.scheduleAtFixedRate(() -> {
            System.out.print(SAVE_CURSOR);
            System.out.print(MOVE_UP + "\r" + ERASE_LINE);

            // Prioridade 1: Mostrar mensagem de erro/aviso se existir e não expirou
            if (statusMessage != null && Instant.now().isBefore(messageExpiry)) {
                System.out.print(statusMessage);
            } 
            // Prioridade 2: Mostrar o Timer se estiver rodando
            else if (manager.getCurrentRuntime() != null) {
                Duration currentRun = (manager.getTimer().getState() == Timer.State.PAUSED) ? Duration.ZERO : Duration.between(lastStartTime, Instant.now());
                Duration totalDisplay = manager.getCurrentRuntime().getDuration().plus(currentRun);
                
                String time = String.format("%02d:%02d:%02d", 
                    totalDisplay.toHours(), totalDisplay.toMinutesPart(), totalDisplay.toSecondsPart());

                if (!(Timer.State.PAUSED == manager.getTimer().getState())) {
                    String icon = spinner[(int) (System.currentTimeMillis() / 500 % spinner.length)];
                    System.out.print(YELLOW + icon + " Foco: " + BOLD + time + RESET + " | 'stop' para encerrar");
                } else {
                    System.out.print(RED + BOLD + "‖ PAUSADO (" + time + ")" + RESET);
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

    private static void listSessions() {
        List<Session> sessions = repository.findAll();
        System.out.println(BOLD + "\n--- HISTÓRICO ---" + RESET);
        if (sessions.isEmpty()) System.out.println("Vazio.");
        else sessions.forEach(s -> System.out.printf("%s | %d min%n", s.sessionId, s.getTotalDuration().toMinutes()));
        System.out.println();
    }

    private static void showBanner() {
        System.out.println(BOLD + BLUE + "=== KAIRU PRODUCTIVITY TIMER ===" + RESET);
    }

    private static void showHelp() {
    System.out.println(CYAN + BOLD + "\nComandos disponíveis:" + RESET);

    String format = "  " + GREEN + "%-10s" + RESET + " ------> %s%n";


    System.out.printf(format, "start", "Inicia uma nova sessão de foco");
    System.out.printf(format, "pause", "Pausa a sessão atual");
    System.out.printf(format, "resume", "Retoma uma sessão pausada");
    System.out.printf(format, "stop", "Finaliza e salva a sessão");
    System.out.printf(format, "list", "Exibe seu histórico de estudos");
    System.out.printf(format, "exit", "Sai do KAIRu");
    System.out.println();    
  }
}
