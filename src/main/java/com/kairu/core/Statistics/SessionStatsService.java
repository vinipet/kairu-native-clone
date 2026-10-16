package com.kairu.core.Statistics;

import com.kairu.core.session.Session;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.time.Clock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SessionStatsService implements StatisticsService {

    private final SessionRepository sessionRepository;
    private final Clock clock;

    public SessionStatsService(SessionRepository sessionRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
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
    public DaySummary getDaySummary(LocalDate date) {
        if (date == null) {
            throw new IllegalStateException("date can't be null");
        }

        List<Session> allSessions = sessionRepository.findAll();
        Map<LocalDate, List<Session>> groupedByDate = groupSessionsByDate(allSessions);
        List<Session> dateSessions = groupedByDate.getOrDefault(date, List.of());

        Map<String, List<Session>> byTag = dateSessions.stream()
                .collect(Collectors.groupingBy(s -> s.getTag().getName()));

        List<TagStat> tagStats = byTag.entrySet().stream()
                .map(entry -> {
                    long minutes = entry.getValue().stream()
                            .mapToLong(s -> s.getTotalDuration().toMinutes())
                            .sum();
                    return new TagStat(entry.getKey(), minutes, entry.getValue().size());
                })
                .sorted(Comparator.comparing(TagStat::tag))
                .toList();

        long totalMinutes = tagStats.stream().mapToLong(TagStat::totalMinutes).sum();
        long totalSessions = tagStats.stream().mapToLong(TagStat::sessionCount).sum();

        return new DaySummary(date, tagStats, totalMinutes, totalSessions);
    }

    @Override
    public List<MetricResult> getRecentDaysStats(int daysCount) {
      if (daysCount <= 0) {
        return List.of();
      }

      List<Session> allSessions = sessionRepository.findAll();
      Map<LocalDate, List<Session>> groupedByDate = groupSessionsByDate(allSessions);
      
      return java.util.stream.IntStream.range(0, daysCount)
          .mapToObj(i -> clock.today().minusDays(daysCount - 1 - i)) 
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

      // Finds the Monday of the current week
      java.time.LocalDate monday = clock.today().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

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
      Map<LocalDate, List<Session>> groupedMap = new HashMap<>();

      if (sessions == null || sessions.isEmpty()) {
        return groupedMap;
      }
      
      for (Session session : sessions) {
        if (session != null && session.getStartedAt() != null) {
            // Descobre a data da sessão
            LocalDate date = session.getStartedAt()
                                    .atZone(clock.getZoneId())
                                    .toLocalDate();

            // Se a data ainda não estiver no Map, cria uma lista nova. 
            // Depois, adiciona a sessão na lista correspondente àquela data.
            groupedMap.computeIfAbsent(date, k -> new ArrayList<>()).add(session);
        }
      }

      return groupedMap;    
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
    
    if(days.get(clock.today()) != null){
      if(dayCoutToStreak(clock.today(), days)){
        streak ++;
      }
    }

    for (LocalDate cursor = clock.today().minusDays(1) ; dayCoutToStreak(cursor, days) ; cursor = cursor.minusDays(1) ){
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
