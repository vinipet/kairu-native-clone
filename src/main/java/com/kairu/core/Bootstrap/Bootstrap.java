package com.kairu.core.Bootstrap;


import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.persistence.PersistencePaths;
import com.kairu.core.session.SessionCompletedPersistenceListener;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.session.TagRepository;
import com.kairu.core.time.Clock;

public class Bootstrap{
  
  public static ApplicationContext createFileContext(Clock clock){
    EventBus bus = new SimpleEventBus();
    SessionRepository repository =  PersistenceModule.initializeFileSessionPersistence(PersistencePaths.sessionsFile());
    SessionManager manager = CoreModule.createCore(bus, clock);
    TagRepository tagRepository = PersistenceModule.initializeFileTagPersistence(PersistencePaths.TagFile());

    bus.subscribeListener(SessionCompletedEvent.class, new SessionCompletedPersistenceListener(repository));

    return new ApplicationContext(bus, repository, manager, tagRepository);
  }


  public static ApplicationContext createInMemoryContext(Clock clock){
    EventBus bus = new SimpleEventBus();
    SessionRepository repository =  PersistenceModule.initializeInMemorySessionPercistence();
    SessionManager manager = CoreModule.createCore(bus, clock);
    TagRepository tagRepository = PersistenceModule.initializeInMemoryTagPersistence();

    bus.subscribeListener(SessionCompletedEvent.class, new SessionCompletedPersistenceListener(repository));

    return new ApplicationContext(bus, repository, manager, tagRepository);
  }
}
