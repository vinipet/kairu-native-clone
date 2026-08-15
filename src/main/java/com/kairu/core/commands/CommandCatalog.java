package com.kairu.core.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CommandCatalog {
  private final Map<String, Command<?>> commands = new HashMap<>();

  public void register( Command<?> command ) {
    if(command == null) {
      throw new NullPointerException("command cannot be null");
    }

    if(command.name() == null) {
      throw new IllegalArgumentException("command must have a name");
    }
    String name = normalize(command.name());
    if(name.isBlank()) {
      throw new IllegalArgumentException("command must have a name");
    }
    if(commands.containsKey(name)) {
      throw new IllegalArgumentException("this command is already registered");
    }

    List<String> aliasList = new ArrayList<>();
    for (String alias : command.aliases()) {
      if(alias == null) {
        throw new IllegalArgumentException("this command has an empty alias");
      }
      String normalizedAlias = normalize(alias);
      if(normalizedAlias.isBlank()) {
        throw new IllegalArgumentException("this command has an empty alias");
      }
      if(commands.containsKey(normalizedAlias)) {
        throw new IllegalArgumentException("the alias '" + alias + "' is already used by another command");
      }
      if(normalizedAlias.equals(name)) {
        throw new IllegalArgumentException("this command has an alias with the same name as the command itself");
      }
      if(aliasList.contains(normalizedAlias)) {
        throw new IllegalArgumentException("this command has duplicate aliases: '" + alias + "'");
      }
      aliasList.add(normalizedAlias);
    }

    commands.put(name, command);

    for (String alias : aliasList) {
      commands.put(alias, command);
    }
  }
  
  public Optional<Command<?>> find(String name){
    if (name == null) { return Optional.empty(); }
    return Optional.ofNullable(commands.get(normalize(name)));
  }

  public Set<Command<?>> findAll(){
    return new HashSet<>(commands.values());
  }

  private String normalize(String value) {
    return value.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
  }
}
