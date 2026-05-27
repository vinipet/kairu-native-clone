package com.kairu.core.Statistics;

import com.kairu.core.session.Session;
import com.kairu.core.session.SessionRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SessionStatsService implements StatisticsService {

    private final SessionRepository sessionRepository;

    public SessionStatsService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<MetricResult> getAllTagsStats() {
        List<Session> allSessions = sessionRepository.findAll();

        Map<String, List<Session>> groupedByTag = groupSessionsByTag(allSessions);

        return groupedByTag.entrySet().stream()
                .map(entry -> calculateMetrics(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public MetricResult getStatsForTag(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            return new MetricResult("", 0, 0);
        }

        List<Session> allSessions = sessionRepository.findAll();

        List<Session> filteredSessions = fyndSessionsForTag(allSessions, tagName);

        return calculateMetrics(tagName, filteredSessions);
    }

    @Override
    public MetricResult getStatsForDate(LocalDate date) {
      if (date == null) {
        return new MetricResult("", 0, 0);
      }

      List<Session> allSessions = sessionRepository.findAll();
      
      Map<java.time.LocalDate, List<Session>> groupedByDate = groupSessionsByDate(allSessions);
    
      List<Session> dateSessions = groupedByDate.getOrDefault(date, List.of());

      return calculateMetrics(date.toString(), dateSessions);
    }

    @Override
    public List<MetricResult> getRecentDaysStats(int daysCount) {
      if (daysCount <= 0) {
        return List.of();
      }

      List<Session> allSessions = sessionRepository.findAll();
      Map<java.time.LocalDate, List<Session>> groupedByDate = groupSessionsByDate(allSessions);

      java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.systemDefault());
      
      return java.util.stream.IntStream.range(0, daysCount)
          .mapToObj(i -> today.minusDays(daysCount - 1 - i)) 
          .map(date -> {
            List<Session> dateSessions = groupedByDate.getOrDefault(date, List.of());
                return calculateMetrics(date.toString(), dateSessions);
          })
          .toList();
    }

    @Override
    public List<MetricResult> getCurrentWeekStats() {
      List<Session> allSessions = sessionRepository.findAll();
      Map<java.time.LocalDate, List<Session>> groupedByDate = groupSessionsByDate(allSessions);

      java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.systemDefault());
      // Finds the Monday of the current week
      java.time.LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

      // Generates the 7 days of the current week (Monday to Sunday)
      return java.util.stream.IntStream.range(0, 7)
            .mapToObj(monday::plusDays)
            .map(date -> {
                List<Session> dateSessions = groupedByDate.getOrDefault(date, List.of());
                return calculateMetrics(date.toString(), dateSessions);
            })
            .toList();
    }



    private Map<String, List<Session>> groupSessionsByTag(List<Session> sessions) {
        return sessions.stream()
                .filter(s -> s.getTag() != null && s.getTag().getName() != null)
                .collect(Collectors.groupingBy(s -> s.getTag().getName()));
    }

    private List<Session> fyndSessionsForTag(List<Session> sessions, String tagName) {
        return sessions.stream()
                .filter(s -> s.getTag() != null && tagName.equalsIgnoreCase(s.getTag().getName()))
                .toList();
    }

    private Map<LocalDate, List<Session>> groupSessionsByDate(List<Session> sessions) {
        return sessions.stream()
          .filter(s -> s.getStartedAt() != null)
          .collect(Collectors.groupingBy(s -> {
            return s.getStartedAt()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        }));
    }


    private MetricResult calculateMetrics(String groupKey, List<Session> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return new MetricResult(groupKey, 0, 0);
        }

        long totalMinutes = sessions.stream()
                .mapToLong(s -> s.getTotalDuration().toMinutes())
                .sum();

        long sessionCount = sessions.size();

        return new MetricResult(groupKey, totalMinutes, sessionCount);
    }
}
