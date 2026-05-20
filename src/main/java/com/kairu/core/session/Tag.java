package com.kairu.core.session;

import java.util.Objects;

public class Tag {
    private final String name;

  public Tag(String name) {
    this.name = name.toLowerCase().trim();
  }

  public String getName() {
    return name;
  }

  
  @Override
  public boolean equals(Object obj){
     if (!(obj instanceof Tag tag)) return false;
     return tag.getName().equals(this.name);    
  }

  @Override
  public int hashCode(){
    return Objects.hash(this.name);
  }
}
