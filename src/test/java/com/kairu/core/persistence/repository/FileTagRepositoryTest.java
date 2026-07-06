package com.kairu.core.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.kairu.core.session.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

class FileTagRepositoryTest {

    @TempDir
    Path tempDir;

    Path tempFile;
    Gson gson;
    FileTagRepository repository;

    @BeforeEach
    void setup() {
        tempFile = tempDir.resolve("tags-test.json");
        gson = new Gson();
        repository = new FileTagRepository(tempFile, gson);
    }

    // --- TESTES DO MÉTODO save() ---

    @Test
    void save_DeveCriarArquivoESalvarPrimeiraTag_QuandoArquivoNaoExistir() throws IOException {
        Tag tag = new Tag("Estudo");

        repository.save(tag);

        assertTrue(Files.exists(tempFile));
        String conteudo = Files.readString(tempFile);
        assertTrue(conteudo.contains("estudo"));
    }

    @Test
    void save_DeveAcumularTags_QuandoAdicionadasEmSequencia() {
        repository.save(new Tag("Trabalho"));
        repository.save(new Tag("Lazer"));

        Set<Tag> tags = repository.findAll();
        assertEquals(2, tags.size());
    }

    @Test
    void save_NaoDeveDuplicarTag_QuandoAdicionarMesmoNome() {
        repository.save(new Tag("Projetos"));
        repository.save(new Tag("Projetos"));

        Set<Tag> tags = repository.findAll();
        assertEquals(1, tags.size(), "O Set não deveria permitir tags duplicadas em disco");
    }

    @Test
    void save_DeveIgnorarCaseEDuplicar_QuandoNomesDiferentesApenasPorCaso() {
        repository.save(new Tag("Urgente"));
        repository.save(new Tag("urgente"));

        Set<Tag> tags = repository.findAll();
        assertEquals(1, tags.size(), "Tags com mesmo nome ignorando case devem ser tratadas como iguais");
    }

    // --- TESTES DO MÉTODO findAll() ---

    @Test
    void findAll_DeveRetornarSetVazio_QuandoArquivoNaoExistir() {
        Set<Tag> tags = repository.findAll();

        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    void findAll_DeveRetornarSetVazio_QuandoArquivoEstiverVazio() throws IOException {
        Files.createFile(tempFile);

        Set<Tag> tags = repository.findAll();

        assertTrue(tags.isEmpty());
    }

    @Test
    void findAll_DeveRetornarTodasAsTagsSalvasNoArquivo() {
        repository.save(new Tag("Urgente"));
        repository.save(new Tag("Backlog"));
        repository.save(new Tag("Fazer"));

        Set<Tag> tags = repository.findAll();

        assertEquals(3, tags.size());
    }

    // --- TESTES DO MÉTODO findByName() ---

    @Test
    void findByName_DeveRetornarTag_QuandoONomeExistirExatamenteIgual() {
        repository.save(new Tag("Saúde"));

        Optional<Tag> resultado = repository.findByName("Saúde");

        assertTrue(resultado.isPresent());
        assertEquals("saúde", resultado.get().getName());
    }

    @Test
    void findByName_DeveRetornarTag_IgnorandoCase() {
        repository.save(new Tag("Finanças"));

        Optional<Tag> resultado = repository.findByName("FINANÇAS");

        assertTrue(resultado.isPresent());
        assertEquals("finanças", resultado.get().getName());
    }

    @Test
    void findByName_DeveRetornarOptionalEmpty_QuandoONomeNaoExistir() {
        repository.save(new Tag("Academia"));

        Optional<Tag> resultado = repository.findByName("Viagens");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByName_DeveRetornarOptionalEmpty_QuandoArquivoEstiverVazio() {
        Optional<Tag> resultado = repository.findByName("QualquerCoisa");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByName_DeveRetornarOptionalEmpty_QuandoArquivoNaoExistir() {
        Path tempFile2 = tempDir.resolve("inexistente.json");
        FileTagRepository repositorioSemArquivo = new FileTagRepository(tempFile2, gson);

        Optional<Tag> resultado = repositorioSemArquivo.findByName("teste");

        assertTrue(resultado.isEmpty());
    }
}
