package com.kairu.core.Bootstrap;

import com.kairu.core.bus.EventBus;
import com.kairu.core.bus.SimpleEventBus;
import com.kairu.core.event.SessionCompletedEvent;
import com.kairu.core.persistence.PersistencePaths;
import com.kairu.core.session.SessionCompletedPersistenceListener;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.SessionRepository;

public class Bootstrap{
  
  public static ApplicationContext createContext(){
    EventBus bus = new SimpleEventBus();
    SessionRepository repository =  PersistenceModule.initializeFilePersistence(PersistencePaths.sessionsFile());
    SessionManager manager = CoreModule.createCore(bus);


    bus.subscribeListener(SessionCompletedEvent.class, new SessionCompletedPersistenceListener(repository));

    return new ApplicationContext(bus, repository, manager);
  }

}
