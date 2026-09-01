package com.kairu.core.persistence.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
      throw new IllegalStateException("Erro ao persistir sessão",e);
    }
  }

  @Override
  public List<Session> findAll() {
    
    if (!Files.exists(file)) {
        return List.of();
    }

    List<Session> sessions = new ArrayList<>();

    try (BufferedReader reader = Files.newBufferedReader(file)) {
        String line;
        for (int lineNum = 1; (line = reader.readLine()) != null; lineNum++) {
            if (line.isBlank()) {
                continue;
            }
            try {
                SessionDTO dto = gson.fromJson(line, SessionDTO.class);
                sessions.add(SessionMapper.toEntity(dto));
            } catch (Exception e) {
                System.err.printf("Aviso: linha %d do arquivo de sessões ignorada — %s%n", lineNum, e.getMessage());
            }
        }
    } catch (IOException e) {
        throw new IllegalStateException("Erro ao ler arquivo de sessões", e);
    }

    return sessions;
  }

  @Override
  public Optional<Session> findById(UUID sessionId) {
    if (!Files.exists(file)) {
        return Optional.empty();
    }

    try (BufferedReader reader = Files.newBufferedReader(file)) {
        String line;
        for (int lineNum = 1; (line = reader.readLine()) != null; lineNum++) {
            if (line.isBlank()) {
                continue;
            }
            try {
                SessionDTO dto = gson.fromJson(line, SessionDTO.class);
                Session session = SessionMapper.toEntity(dto);
                if (session.sessionId.equals(sessionId)) {
                    return Optional.of(session);
                }
            } catch (Exception e) {
                System.err.printf("Aviso: linha %d do arquivo de sessões ignorada — %s%n", lineNum, e.getMessage());
            }
        }
    } catch (IOException e) {
        throw new IllegalStateException("Erro ao ler o arquivo de sessões", e);
    }

    return Optional.empty();
  }
}
