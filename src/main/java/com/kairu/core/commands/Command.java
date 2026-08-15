package com.kairu.core.commands;

import java.util.Set;

public interface Command<T> {
  String name();
  Set<String> aliases();        
  String description();
  CommandResult<T> execute(String[] args);
}
