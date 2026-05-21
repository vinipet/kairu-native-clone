package com.kairu.core.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.kairu.core.persistence.GsonFactory;
import com.kairu.core.session.Session;
import com.kairu.core.session.Tag;
import com.kairu.core.time.Interval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class SessionRepositoryTest {

    @TempDir
    Path tempDir; 
    Gson gson;
    Path tempFile;
    FileSessionRepository repository;

  @BeforeEach
  void setup() {
        this.gson = GsonFactory.create();
        tempFile = tempDir.resolve("test-sessions.json");
        repository = new FileSessionRepository(tempFile, gson);
  }

  @Test
  void deveCriarArquivoEAdicionarSessaoComSucesso() throws IOException {
        List<Interval> intervals = new ArrayList<>();
        intervals.add(new Interval(Instant.now(), Instant.now().plusSeconds(600)));
        Session session = new Session(UUID.randomUUID(), intervals, new Tag("test"));

        repository.save(session);

        assertTrue(Files.exists(tempFile), "O arquivo deveria ter sido criado");
        
        List<String> linhas = Files.readAllLines(tempFile);
        assertEquals(1, linhas.size(), "O arquivo deve ter exatamente 1 linha");
        assertTrue(linhas.get(0).contains(session.sessionId.toString()), "O JSON deve conter o ID da sessão");
    }

  @Test
  void deveAcumularSessoesNoMesmoArquivo() throws IOException { 
    List<Interval> intervals = new ArrayList<>();
    intervals.add(new Interval(Instant.now(), Instant.now().plusSeconds(600)));

    repository.save(new Session(UUID.randomUUID(), intervals,new Tag("test")));
    repository.save(new Session(UUID.randomUUID(), intervals,new Tag("test")));

    List<String> linhas = Files.readAllLines(tempFile);
    assertEquals(2, linhas.size(), "O arquivo deveria ter 2 linhas (uma para cada sessão)");
  }

  @Test
  void canShowSavedSession(){
    List<Interval> intervals = new ArrayList<>();
    intervals.add(new Interval(Instant.now(), Instant.now().plusSeconds(600)));
    UUID expectedId = UUID.randomUUID();

    repository.save(new Session(expectedId, intervals,new Tag("test")));


    List<Session> result = repository.findAll();

    assertEquals(1, result.size());
    assertEquals(expectedId, result.get(0).sessionId);
  }

  @Test
  void canSaveAndShowMoreThanOneSession(){
    List<Interval> intervals = new ArrayList<>();
    intervals.add(new Interval(Instant.now(), Instant.now().plusSeconds(600)));
    UUID expectedId1 = UUID.randomUUID();
    UUID expectedId2 = UUID.randomUUID();
    UUID expectedId3 = UUID.randomUUID();

    repository.save(new Session(expectedId1, intervals, new Tag("test")));
    repository.save(new Session(expectedId2, intervals,new Tag("test")));
    repository.save(new Session(expectedId3, intervals,new Tag("test")));

    List<Session> result = repository.findAll();

    assertEquals(3, result.size());
    assertEquals(expectedId1, result.get(0).sessionId);
    assertEquals(expectedId2, result.get(1).sessionId);
    assertEquals(expectedId3, result.get(2).sessionId);
    assertNotEquals(expectedId3, result.get(0).sessionId);
  }

  @Test
  void findById_DeveRetornarSessao_QuandoOIdExistirNoArquivo() throws IOException {
    UUID idProcurado = UUID.randomUUID();
    Session s1 = new Session(idProcurado, List.of(new Interval(Instant.now(), Instant.now().plusSeconds(10))),new Tag("test"));
    Session s2 = new Session(UUID.randomUUID(), List.of(new Interval(Instant.now(), Instant.now().plusSeconds(20))),new Tag("test"));
    
    repository.save(s1);
    repository.save(s2);

    Optional<Session> resultado = repository.findById(idProcurado);

    assertTrue(resultado.isPresent(), "A sessão deveria ser encontrada");
    assertEquals(idProcurado, resultado.get().sessionId);
  }

  @Test
  void findById_DeveRetornarOptionalEmpty_QuandoOIdNaoExistirNoArquivo() throws IOException {
    Session s1 = new Session(UUID.randomUUID(), List.of(new Interval(Instant.now(), Instant.now().plusSeconds(10))),new Tag("test"));
    repository.save(s1);

    Optional<Session> resultado = repository.findById(UUID.randomUUID()); // ID aleatório

    assertTrue(resultado.isEmpty(), "O Optional deveria estar vazio");
  }

  @Test
  void findById_DeveRetornarOptionalEmpty_QuandoOArquivoNaoExistir() {

    Optional<Session> resultado = repository.findById(UUID.randomUUID());

    assertTrue(resultado.isEmpty(), "Não deve quebrar se o arquivo não existir, apenas retornar vazio");
  }

  @Test
  void findById_DeveRetornarOptionalEmpty_QuandoOArquivoEstiverVazio() throws IOException {
    Optional<Session> resultado = repository.findById(UUID.randomUUID());

      assertTrue(resultado.isEmpty(), "Arquivo vazio deve retornar Optional.empty");
    }

    @Test
    void findById_DeveRetornarSessaoCorreta_QuandoHouverMultiplosRegistros() throws IOException {
      UUID idAlvo = UUID.randomUUID();
      repository.save(new Session(UUID.randomUUID(), List.of(new Interval(Instant.now(), Instant.now().plusSeconds(5))),new Tag("test")));
      repository.save(new Session(UUID.randomUUID(), List.of(new Interval(Instant.now(), Instant.now().plusSeconds(10))),new Tag("test")));
      repository.save(new Session(idAlvo, List.of(new Interval(Instant.now(), Instant.now().plusSeconds(15))),new Tag("test"))); // Alvo no meio

      Optional<Session> resultado = repository.findById(idAlvo);

      assertTrue(resultado.isPresent());
      assertEquals(idAlvo, resultado.get().sessionId);
  }
}
