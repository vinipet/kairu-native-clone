package com.kairu;

import java.util.List;
import java.util.Scanner;

import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.EventListener;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.Session;
import com.kairu.core.session.SessionCompletedPersistenceListener;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.time.Clock;
import com.kairu.core.time.RealClock;
import com.kairu.core.time.TimerFactory;


public class Application {

  public static void main(String[] args) {

    EventBus bus = new SimpleEventBus();
    Clock clock = new RealClock();
    TimerFactory factory = new TimerFactory(clock, bus);
    SessionManager manager = new SessionManager(bus, clock, factory);
    SessionRepository repository = new InMemorySessionRepository();
    EventListener<SessionCompletedEvent> repoListener = new SessionCompletedPersistenceListener(repository);
    bus.subscribeListener(SessionCompletedEvent.class, repoListener);

    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    System.out.println("O kairu esta iniciando...");
    System.out.println("os comandos basicos do sistema são:");
    System.out.printf("%-15s ----------------> %s%n", "start", "inicia a contagem");
    System.out.printf("%-15s ----------------> %s%n", "pause", "pausa a contagem");
    System.out.printf("%-15s ----------------> %s%n", "resume", "resume a contagem");
    System.out.printf("%-15s ----------------> %s%n", "stop", "finaliza a contagem");
    System.out.printf("%-15s ----------------> %s%n", "exit", "finaliza a aplicação");
    System.out.printf("%-15s ----------------> %s%n", "list", "mostra a lista de sessões");
    System.out.println("inicie quando estiver pronto");
    System.out.println("rodando >>>");
    

    while(running){
      String input = scanner.nextLine().toLowerCase().trim();
      
      switch (input) {
        case "start":
          startSession(manager);
        break;
        case "pause":
          pauseSession(manager);
        break;
        case "resume":
          resumeSession(manager);
        break;
        case "stop":
         stopSession(manager);
        break;
        case "exit":
          running = false;
        break;
        case "list":
          listSession(repository);
        break;
        default:
        break;
      }
    }
    scanner.close();

    System.out.println(">>>  fechando aplicação");
	}

  private static void startSession(SessionManager manager){
    try {
      manager.startSession();
      System.out.println("<<< sessão iniciada >>>");
    } catch (Exception e) {
      System.out.println(">>>  " + e.getMessage());
    }
  }
  private static void pauseSession(SessionManager manager){
    try {
      manager.pauseSession();
      System.out.println("<<< sessão pausada >>>");
    } catch (Exception e) {
      System.out.println(">>>  " + e.getMessage());
    }
  }
  private static void resumeSession(SessionManager manager){
    try {
      manager.resumeSession();
      System.out.println("<<< sessão retomada >>>");
    } catch (Exception e) {
      System.out.println(">>>  " + e.getMessage());
    }
  }
  private static void stopSession(SessionManager manager){
    try {
      manager.stopSession();
      System.out.println("<<< sessão acabou >>>");
    } catch (Exception e) {
      System.out.println(">>>  " + e.getMessage());
    }
  }
  private static void listSession(SessionRepository repository){
    List<Session> list = repository.findAll();
    list.forEach((session) -> {
      System.out.println(session.sessionId + ">>" + session.getTotalDuration());
    });
  }

}
