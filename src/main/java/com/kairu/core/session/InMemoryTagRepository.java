package com.kairu.core.session;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class InMemoryTagRepository implements TagRepository{
  
  private Set<Tag> tagRepository = new HashSet<Tag>();


  @Override
  public void save(Tag tag) {
    if(tag == null){
      throw new IllegalArgumentException("the argument dont can be a null type");
    } 
    tagRepository.add(tag); 
  }

  @Override
  public Optional<Tag> findByName(String name) {
    for (Tag tag : tagRepository) {
      if(tag.getName().equals(name.toLowerCase().trim())){
        return Optional.of(tag);
      }
    }
    return Optional.empty();
  }

  @Override
  public Set<Tag> findAll(){
    final Set<Tag> tagList = Set.copyOf(tagRepository);
    return tagList;  
  }
}
