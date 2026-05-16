package com.kairu.core.persistence.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.kairu.core.persistence.dto.SessionDTO;
import com.kairu.core.persistence.mapper.SessionMapper;
import com.kairu.core.session.Session;
import com.kairu.core.session.SessionRepository;

public class FileSessionRepository implements SessionRepository {

  private Gson gson;
  private Path file;

  public FileSessionRepository(Path path, Gson gson){
    this.file = path;
    this.gson = gson;
  }
    
  @Override
  public void save(Session session){
    
    try{
      Files.createDirectories(file.getParent());
      
      SessionDTO dto = SessionMapper.toDTO(session);
      String json = gson.toJson(dto);
      Files.writeString(
        file,
        json + System.lineSeparator(),
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
      );
    }catch(IOException e){
      throw new RuntimeException("Erro ao persistir sessão",e);
    }
  }

  @Override
  public List<Session> findAll() {
    
    if (!Files.exists(file)) {
        return List.of();
    }

    try (Stream<String> lines = Files.lines(file)) {

        return lines
            .map(line -> gson.fromJson(line, SessionDTO.class))
            .map(SessionMapper::toEntity)
            .toList();

    } catch (IOException e) {
        throw new IllegalStateException("failed to read sessions", e);
    }
  }

  @Override
  public Optional<Session> findById(UUID sessionId) {
    if (!Files.exists(file)) {
        return Optional.empty();
    }

    try (Stream<String> lines = Files.lines(file)) {
        return lines
            .map(line -> gson.fromJson(line, SessionDTO.class))
            .map(SessionMapper::toEntity)
            .filter(session -> session.sessionId.equals(sessionId))
            .findFirst();
    } catch (IOException e) {
        throw new RuntimeException("Erro ao ler o arquivo de sessões", e);
    }
  }
}
