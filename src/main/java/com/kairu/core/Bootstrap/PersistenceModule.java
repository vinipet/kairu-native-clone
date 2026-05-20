package com.kairu.core.Bootstrap;

import java.nio.file.Path;

import com.kairu.core.persistence.GsonFactory;
import com.kairu.core.persistence.repository.FileSessionRepository;
import com.kairu.core.persistence.repository.FileTagRepository;
import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.InMemoryTagRepository;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.session.TagRepository;

public class PersistenceModule{


  public static SessionRepository initializeFileSessionPersistence(Path path){
    return new FileSessionRepository(path, GsonFactory.create());
  }

  public static SessionRepository initializeInMemorySessionPercistence(){
    return new InMemorySessionRepository();
  }

  public static TagRepository initializeInMemoryTagPersistence(){
    return new InMemoryTagRepository();
  }

  public static TagRepository initializeFileTagPersistence(Path path){
    return new FileTagRepository(path, GsonFactory.create());
  }
}
