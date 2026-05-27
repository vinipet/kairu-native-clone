package com.kairu.core.Statistics;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    record MetricResult(String groupKey, long totalMinutes, long sessionCount) {}

    List<MetricResult> getAllTagsStats();

    MetricResult getStatsForTag(String tagName);
    
    MetricResult getStatsForDate(LocalDate date);

    List<MetricResult> getRecentDaysStats(int daysCount);

    List<MetricResult> getCurrentWeekStats();
}
