package com.kairu.core.Bootstrap;

import java.nio.file.Path;

import com.kairu.core.persistence.GsonFactory;
import com.kairu.core.persistence.repository.FileSessionRepository;
import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.SessionRepository;

public class PersistenceModule{


  public static SessionRepository initializeFilePersistence(Path path){
    return new FileSessionRepository(path, GsonFactory.create());
  }

  public static SessionRepository initializeInMemoryPercistence(){
    return new InMemorySessionRepository();
  }
}
