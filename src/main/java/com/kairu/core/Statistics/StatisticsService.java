package com.kairu.core.Statistics;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    record MetricResult(String groupKey, long totalMinutes, long sessionCount) {}

    record TagStat(String tag, long totalMinutes, long sessionCount) {}

    record DaySummary(LocalDate date, List<TagStat> byTag, long totalMinutes, long totalSessions) {}

    List<MetricResult> getAllTagsStats();

    MetricResult getStatsForTag(String tagName);
    
    MetricResult getStatsForDate(LocalDate date);

    DaySummary getDaySummary(LocalDate date);

    List<MetricResult> getRecentDaysStats(int daysCount);

    List<MetricResult> getCurrentWeekStats();
}
