package com.kairu.core.Statistics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kairu.core.session.InMemorySessionRepository;
import com.kairu.core.session.Session;
import com.kairu.core.session.Tag;
import com.kairu.core.time.Interval;
import com.kairu.core.Statistics.StatisticsService.MetricResult;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

class SessionStatsServiceTest {

    private InMemorySessionRepository repository;
    private SessionStatsService service;

    @BeforeEach
    void setup() {
        repository = new InMemorySessionRepository();
        service = new SessionStatsService(repository);
    }

    private Session criarSessao(Instant inicio, long duracaoSegundos, Tag tag) {
        Interval intervalo = new Interval(inicio, inicio.plusSeconds(duracaoSegundos));
        return new Session(UUID.randomUUID(), List.of(intervalo), tag);
    }

    // ======== getStatsForTag ========

    @Test
    void getStatsForTag_DeveRetornarMetricaVazia_QuandoTagNameForNulo() {
        MetricResult result = service.getStatsForTag(null);

        assertEquals("", result.groupKey());
        assertEquals(0, result.totalMinutes());
        assertEquals(0, result.sessionCount());
    }

    @Test
    void getStatsForTag_DeveRetornarMetricaVazia_QuandoTagNameForBlank() {
        MetricResult result = service.getStatsForTag("   ");

        assertEquals("", result.groupKey());
        assertEquals(0, result.totalMinutes());
        assertEquals(0, result.sessionCount());
    }

    @Test
    void getStatsForTag_DeveRetornarMetricaCorreta_QuandoTagExistir() {
        repository.save(criarSessao(Instant.now(), 3600, new Tag("trabalho")));

        MetricResult result = service.getStatsForTag("trabalho");

        assertEquals("trabalho", result.groupKey());
        assertEquals(60, result.totalMinutes());
        assertEquals(1, result.sessionCount());
    }

    @Test
    void getStatsForTag_DeveAcumularMultiplasSessoesComMesmaTag() {
        Tag tag = new Tag("estudo");
        repository.save(criarSessao(Instant.now(), 1800, tag));
        repository.save(criarSessao(Instant.now().plusSeconds(1), 3600, tag));

        MetricResult result = service.getStatsForTag("estudo");

        assertEquals("estudo", result.groupKey());
        assertEquals(90, result.totalMinutes());
        assertEquals(2, result.sessionCount());
    }

    @Test
    void getStatsForTag_DeveIgnorarCase_NaBusca() {
        repository.save(criarSessao(Instant.now(), 1800, new Tag("Projeto")));

        MetricResult result = service.getStatsForTag("PROJETO");

        assertEquals("PROJETO", result.groupKey());
        assertEquals(30, result.totalMinutes());
        assertEquals(1, result.sessionCount());
    }

    @Test
    void getStatsForTag_DeveRetornarMetricaVazia_QuandoTagNaoExistir() {
        repository.save(criarSessao(Instant.now(), 1800, new Tag("estudo")));

        MetricResult result = service.getStatsForTag("inexistente");

        assertEquals("inexistente", result.groupKey());
        assertEquals(0, result.totalMinutes());
        assertEquals(0, result.sessionCount());
    }

    // ======== getStatsForDate ========

    @Test
    void getStatsForDate_DeveRetornarMetricaVazia_QuandoDateForNulo() {
        MetricResult result = service.getStatsForDate(null);

        assertEquals("", result.groupKey());
        assertEquals(0, result.totalMinutes());
        assertEquals(0, result.sessionCount());
    }

    @Test
    void getStatsForDate_DeveRetornarMetricaCorreta_QuandoExistiremSessoesNaData() {
        LocalDate data = LocalDate.of(2025, 6, 15);
        Instant inicio = data.atStartOfDay(ZoneId.systemDefault()).toInstant();
        repository.save(criarSessao(inicio, 7200, new Tag("trabalho")));

        MetricResult result = service.getStatsForDate(data);

        assertEquals(data.toString(), result.groupKey());
        assertEquals(120, result.totalMinutes());
        assertEquals(1, result.sessionCount());
    }

    @Test
    void getStatsForDate_DeveAgruparMultiplasSessoesNaMesmaData() {
        LocalDate data = LocalDate.of(2025, 6, 15);
        Instant inicio = data.atStartOfDay(ZoneId.systemDefault()).toInstant();
        repository.save(criarSessao(inicio, 1800, new Tag("estudo")));
        repository.save(criarSessao(inicio.plusSeconds(3600), 3600, new Tag("trabalho")));

        MetricResult result = service.getStatsForDate(data);

        assertEquals(data.toString(), result.groupKey());
        assertEquals(90, result.totalMinutes());
        assertEquals(2, result.sessionCount());
    }

    @Test
    void getStatsForDate_DeveRetornarMetricaVazia_QuandoNaoExistirSessaoNaData() {
        repository.save(criarSessao(Instant.now(), 1800, new Tag("teste")));

        LocalDate dataInexistente = LocalDate.of(2020, 1, 1);
        MetricResult result = service.getStatsForDate(dataInexistente);

        assertEquals(dataInexistente.toString(), result.groupKey());
        assertEquals(0, result.totalMinutes());
        assertEquals(0, result.sessionCount());
    }

    // ======== getRecentDaysStats ========

    @Test
    void getRecentDaysStats_DeveRetornarListaVazia_QuandoDaysCountForZero() {
        List<MetricResult> results = service.getRecentDaysStats(0);

        assertTrue(results.isEmpty());
    }

    @Test
    void getRecentDaysStats_DeveRetornarListaVazia_QuandoDaysCountForNegativo() {
        List<MetricResult> results = service.getRecentDaysStats(-1);

        assertTrue(results.isEmpty());
    }

    @Test
    void getRecentDaysStats_DeveRetornarTamanhoCorreto() {
        List<MetricResult> results = service.getRecentDaysStats(5);

        assertEquals(5, results.size());
    }

    @Test
    void getRecentDaysStats_DeveConterSessaoDeHoje() {
        LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
        repository.save(criarSessao(Instant.now(), 3600, new Tag("teste")));

        List<MetricResult> results = service.getRecentDaysStats(3);

        MetricResult resultadoHoje = results.stream()
                .filter(r -> r.groupKey().equals(hoje.toString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dia de hoje não encontrado nos resultados"));

        assertEquals(60, resultadoHoje.totalMinutes());
        assertEquals(1, resultadoHoje.sessionCount());
    }

    @Test
    void getRecentDaysStats_DeveRetornarZeros_QuandoNaoHaSessoes() {
        List<MetricResult> results = service.getRecentDaysStats(3);

        assertEquals(3, results.size());
        results.forEach(r -> {
            assertEquals(0, r.totalMinutes());
            assertEquals(0, r.sessionCount());
        });
    }

    // ======== getCurrentWeekStats ========

    @Test
    void getCurrentWeekStats_DeveRetornar7Dias() {
        List<MetricResult> results = service.getCurrentWeekStats();

        assertEquals(7, results.size());
    }

    @Test
    void getCurrentWeekStats_DeveIniciarNaSegunda() {
        List<MetricResult> results = service.getCurrentWeekStats();

        LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        assertEquals(segunda.toString(), results.get(0).groupKey());
    }

    @Test
    void getCurrentWeekStats_DeveConterSessaoDeHoje() {
        Instant agora = Instant.now();
        repository.save(criarSessao(agora, 1800, new Tag("teste")));

        List<MetricResult> results = service.getCurrentWeekStats();
        LocalDate hoje = LocalDate.now(ZoneId.systemDefault());

        MetricResult resultadoHoje = results.stream()
                .filter(r -> r.groupKey().equals(hoje.toString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dia de hoje não encontrado na semana"));

        assertEquals(30, resultadoHoje.totalMinutes());
        assertEquals(1, resultadoHoje.sessionCount());
    }

    @Test
    void getCurrentWeekStats_DeveRetornarZeros_QuandoNaoHaSessoes() {
        List<MetricResult> results = service.getCurrentWeekStats();

        assertEquals(7, results.size());
        results.forEach(r -> {
            assertEquals(0, r.totalMinutes());
            assertEquals(0, r.sessionCount());
        });
    }
}
