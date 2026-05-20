package com.kairu.core.persistence.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.kairu.core.session.Tag;
import com.kairu.core.session.TagRepository;

public class FileTagRepository implements TagRepository {

  private Gson gson;
  private Path file;

  public FileTagRepository(Path path, Gson gson){
    this.file = path;
    this.gson = gson;
  }
    
  @Override
  public void save(Tag tag){
    
    try{
      Files.createDirectories(file.getParent());
      HashSet<Tag> tagList;
      if (Files.exists(file) && Files.size(file) > 0){
        String olderJson = Files.readString(file);
        java.lang.reflect.Type typeLista = new com.google.gson.reflect.TypeToken<HashSet<Tag>>(){}.getType();
        tagList  = gson.fromJson(olderJson, typeLista);
      } else {
        tagList = new HashSet<>();
      }

      tagList.add(tag);
      String newJson = gson.toJson(tagList);
      Files.writeString(file , newJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    }catch(IOException e){
      throw new RuntimeException("Erro ao salvar tag",e);
    }
  }

  @Override
  public Set<Tag> findAll() {
    try {
      if (!Files.exists(file) || Files.size(file) == 0) {
        return new HashSet<>();
      }
      String json = Files.readString(file);
      java.lang.reflect.Type tipoLista = new com.google.gson.reflect.TypeToken<List<Tag>>(){}.getType();
      List<Tag> lista = gson.fromJson(json, tipoLista); 
      return lista != null ? new HashSet<>(lista) : new HashSet<>(); 
    } catch (IOException e) {
        throw new RuntimeException("Erro ao ler a lista de tags do arquivo", e);
    }
  }

  @Override
  public Optional<Tag> findByName(String name) {
    Set<Tag> todasAsTags = findAll();
    return todasAsTags.stream()
            .filter(tag -> tag.getName().equalsIgnoreCase(name))  
            .findFirst();
  }
}
