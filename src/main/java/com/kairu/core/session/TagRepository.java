package com.kairu.core.session;

import java.util.Optional;
import java.util.Set;

public interface TagRepository {
   
  public void save(Tag tag);
  public Optional<Tag> findByName(String name);
  public Set<Tag> findAll();

}
