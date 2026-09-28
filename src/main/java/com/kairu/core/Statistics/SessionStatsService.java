package com.kairu.core.Statistics;

import com.kairu.core.session.Session;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.time.Clock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SessionStatsService implements StatisticsService {

    private final SessionRepository sessionRepository;
    private final Clock clock;
    private final ZoneId zoneId;

    public SessionStatsService(SessionRepository sessionRepository, Clock clock, ZoneId zoneId) {
        this.sessionRepository = sessionRepository;
        this.zoneId = zoneId;
        this.clock = clock;
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
      Map<LocalDate, List<Session>> groupedByDate = groupSessionsByDate(allSessions);

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
          .collect(Collectors.groupingBy(s -> s.getTag().getName()));
    }

    private List<Session> fyndSessionsForTag(List<Session> sessions, String tagName) {
        return sessions.stream()
                .filter(s -> tagName.equalsIgnoreCase(s.getTag().getName()))
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

  public int getCurrentStreak(){
    int streak = 0;

    List<Session> allSessions = sessionRepository.findAll();
    Map<LocalDate, List<Session>> days = groupSessionsByDate(allSessions);
    LocalDate today = LocalDate.now(java.time.ZoneId.systemDefault());
    
    if(days.get(today) != null){
      if(dayCoutToStreak(today, days)){
        streak ++;
      }
    }

    for (LocalDate cursor = today.minusDays(1) ; dayCoutToStreak(cursor, days) ; cursor = cursor.minusDays(1) ){
      streak ++;
    }

    return streak;
  }

  private Boolean dayCoutToStreak(LocalDate day, Map<LocalDate, List<Session>> days){
    long somaHorasDoDia = 0;
    List<Session> sessions = days.get(day);
    if(sessions == null){
      return false;
    }

    for (Session session : sessions) {
        somaHorasDoDia += session.getTotalDuration().toHours(); 
    }
    
    if (somaHorasDoDia >= 3 || sessions.size() >= 3) {
        return true;
    }

    return false;
  }
}
