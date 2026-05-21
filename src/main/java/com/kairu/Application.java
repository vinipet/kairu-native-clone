package com.kairu;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.Bootstrap.Bootstrap;
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
    private static ApplicationContext context;
    private static ScheduledExecutorService scheduler;
    private static Scanner scanner = new Scanner(System.in);
    

    // Sistema de Notificação no Timer
    private static String statusMessage = null;
    private static Instant messageExpiry = null;

    public static void main(String[] args) {
        context = Bootstrap.createFileContext(new RealClock());   
        manager = context.getManager();
        
        showBanner();
        System.out.println("\n\n"); 
            
        while (true) {
            System.out.print(BOLD + CYAN + "kairu "+ getPromptTagSuffix() + "> " + RESET);
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().toLowerCase().trim();

            if (input.equals("exit")) {
                if (confirmExit()) break;
                else continue;
            }

            switch (input) {
                case "start"      -> safeRun(Application::startSession);
                case "pause"      -> safeRun(Application::pauseSession);
                case "resume"     -> safeRun(Application::resumeSession);
                case "stop"       -> safeRun(Application::stopSession);
                case "list"       -> safeRun(Application::listSessions);
                case "help"       -> safeRun(Application::showHelp);
                case "create tag"  -> safeRun(Application::createTag);
                case "list tag"    -> safeRun(Application::listTags);
                case ""           -> {}
                default           -> setStatusMessage(YELLOW + "Comando desconhecido." + RESET, 3);
            }
        }
        
        stopLiveTimer();
        System.out.println(BLUE + "Até logo!" + RESET);
        System.exit(0);
    }

    private static void setStatusMessage(String message, int seconds) {
      if (manager.getCurrentRuntime() != null) {
        statusMessage = message;
        messageExpiry = Instant.now().plusSeconds(seconds);
      } else {
        System.out.println(message);
      }    
    }

    private static boolean confirmExit() {
        if(manager.getCurrentRuntime() == null){
          return true;
        }
        
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
        if (manager.getCurrentRuntime() != null){
          setStatusMessage(RED + "Erro: Sessão ja iniciada." + RESET, 5);
          return;
        }

        System.out.print(RED + BOLD + "Digite a tag da sessão:" + RESET);
        listTags();
        String choice = scanner.nextLine().toLowerCase().trim();
        
        Optional<Tag> tagEncontrada = context.getTagRepository().findByName(choice);

        if (tagEncontrada.isPresent()) {
          System.out.print(MOVE_UP + "\r" + ERASE_LINE); // Limpa a pergunta
          manager.startSession(tagEncontrada.get());
          startLiveTimer();
        } else {
          setStatusMessage("Digite uma tag válida, ou crie uma nova tag", 5);
        }
    }

    private static void pauseSession() {
            manager.pauseSession();
    }

    private static void resumeSession() {
            manager.resumeSession();
    }

    private static void stopSession() {
        if (manager.getCurrentRuntime() == null) return;

        StopResult result = manager.stopSession();
        
        if (result == StopResult.TOO_SHORT) {
          stopLiveTimer();
          while(true){  
            System.out.print(RED + BOLD + "\r⚠ Sessão muito curta (" + manager.getCurrentRuntime().getDuration().toMinutes() + "min). " +
                                                                  "Deseja descartá-la e encerrar? [s/n]: " + RESET);
            String choice = scanner.nextLine().toLowerCase().trim();

            if (choice.equals("s")){
              manager.cancelSession();
              System.out.println(YELLOW + "\r" + ERASE_LINE + "Sessão descartada." + RESET + "\n");
              break;
            } else if( choice.equals("n")){
              System.out.print(MOVE_UP + "\r" + ERASE_LINE); // Limpa a pergunta
              startLiveTimer(); // Volta o relógio
              break;
            } else{
              System.out.print(YELLOW + "\r⚠ Opção inválida! Digite 's' ou 'n'." + RESET);
              try { 
                Thread.sleep(2000); 
              } catch (InterruptedException ignored) {}
                System.out.print("\r" + ERASE_LINE);
            }
          }
        
        } else {
            stopLiveTimer();
            System.out.println(BLUE + "\r✔ Sessão salva!" + RESET + "\n\n");
        }
    }

    private static void startLiveTimer() {
        if (scheduler != null){
          scheduler.shutdownNow();
        }
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
                Duration totalDisplay = manager.getCurrentRuntime().getDuration();
                
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
    List<Session> sessions = context.getSessionRepository().findAll();
    System.out.println(BOLD + "\n--- HISTÓRICO ---" + RESET);
    if (sessions.isEmpty()) System.out.println("Vazio.");
    else sessions.forEach(s -> System.out.printf(
                         "[%s] | %s | %d min | %d intervalos%n",
                         s.getTag() != null ? s.getTag().getName() : "Sem Tag",  
                         s.getStartedAt(),                                                             
                         s.getTotalDuration().toMinutes(),                      
                         s.getIntervals().size()
    ));
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
  
  private static void safeRun(Runnable action){
    try{
      action.run();
    }catch(Exception e){
      setStatusMessage(RED + "Erro: " + e.getMessage() + RESET, 5);
    }
  }
  
  private static void createTag(){
    if (context.getManager().getCurrentRuntime() == null) {
      System.out.print(RED + BOLD + "Escolha o nome da Tag: " + RESET);
      String choice = scanner.nextLine();
      if (choice == null || choice.isBlank()) {
        setStatusMessage(RED + "Erro: O nome da tag não pode ser vazio!" + RESET, 4);
        return; // Corta a execução aqui para não salvar no repositório
      }


      Tag tag = new Tag(choice);
      context.getTagRepository().save(tag);
        
      System.out.println(GREEN + "✔ Tag '" + choice + "' criada com sucesso!" + RESET + "\n");
    } else {
      setStatusMessage(RED + "Vai estudar, FOCO!!!" + RESET, 5);
    }
  }

  private static void listTags(){
    Set<Tag> tags = context.getTagRepository().findAll();
    System.out.println(BOLD + "\n--- TAGS ---" + RESET);
    if (tags.isEmpty()){
      System.out.println("Vazio");
    } else {
      tags.forEach(s -> System.out.printf("- %s%n", s.getName()));
    }
  }

  private static String getPromptTagSuffix() {
    var runtime = manager.getCurrentRuntime();
    
    if (runtime == null || manager.getCurrentTag() == null) {
        return "";
    }
    return " " + YELLOW + "[" + manager.getCurrentTag().getName() + "]" + CYAN;
  }
}
