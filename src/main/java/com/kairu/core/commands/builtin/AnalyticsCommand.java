package com.kairu.core.commands.builtin;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.kairu.core.Bootstrap.ApplicationContext;
import com.kairu.core.Statistics.StatisticsService;
import com.kairu.core.commands.BuiltinCommand;
import com.kairu.core.commands.CommandResult;

public class AnalyticsCommand extends BuiltinCommand {

    private final ApplicationContext context;
    private final Scanner scanner;

    public AnalyticsCommand(ApplicationContext context, Scanner scanner) {
        this.context = context;
        this.scanner = scanner;
    }

    @Override
    public String name() {
        return "analytics";
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Abre o menu de analytics";
    }

    @Override
    protected CommandResult<String> run(String[] args) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\033[1m\033[34m=== KAIRU ANALYTICS ===\033[0m\n");
        sb.append("1. All Tags Summary\n");
        sb.append("2. Specific Tag Stats\n");
        sb.append("3. Specific Date Stats\n");
        sb.append("4. Recent Days Evolution\n");
        sb.append("5. Current Week Report\n");
        sb.append("6. show older list viewer\n");
        sb.append("7. Back to Main Menu\n");
        sb.append("\033[1m\033[36manalytics > \033[0m");

        System.out.print(sb);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                var stats = context.getAnalytics().getAllTagsStats();
                return CommandResult.success(formatMetricList("All Tags", stats));
            }
            case "2" -> {
                System.out.print("\033[1mEnter Tag Name: \033[0m");
                String tagName = scanner.nextLine().trim();
                var stat = context.getAnalytics().getStatsForTag(tagName);
                return CommandResult.success(formatSingleMetric(stat));
            }
            case "3" -> {
                System.out.print("\033[1mEnter Date (YYYY-MM-DD): \033[0m");
                String dateInput = scanner.nextLine().trim();
                try {
                    LocalDate date = LocalDate.parse(dateInput);
                    var stat = context.getAnalytics().getStatsForDate(date);
                    return CommandResult.success(formatSingleMetric(stat));
                } catch (java.time.format.DateTimeParseException e) {
                    return CommandResult.failure(new IllegalArgumentException("Invalid date format! Use YYYY-MM-DD"));
                }
            }
            case "4" -> {
                System.out.print("\033[1mHow many days back?: \033[0m");
                try {
                    int days = Integer.parseInt(scanner.nextLine().trim());
                    var stats = context.getAnalytics().getRecentDaysStats(days);
                    return CommandResult.success(formatMetricList("Recent Days Evolution", stats));
                } catch (NumberFormatException e) {
                    return CommandResult.failure(new IllegalArgumentException("Invalid number of days!"));
                }
            }
            case "5" -> {
                var stats = context.getAnalytics().getCurrentWeekStats();
                return CommandResult.success(formatMetricList("Current Week Report", stats));
            }
            case "6" -> {
                return CommandResult.success("list-sessions");
            }
            case "7" -> {
                return CommandResult.success("");
            }
            default -> {
                return CommandResult.failure(new IllegalArgumentException("Unknown analytics option!"));
            }
        }
    }

    private String formatMetricList(String title, List<StatisticsService.MetricResult> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\033[1m\033[32m--- ").append(title).append(" ---\033[0m\n");
        if (stats.isEmpty()) {
            sb.append("No data found for this period.\n");
            return sb.toString();
        }
        sb.append(String.format("\033[1m%-20s | %-12s | %-10s\033[0m%n", "Group/Key", "Total Time", "Sessions"));
        sb.append("-----------------------------------------------------\n");
        stats.forEach(stat -> sb.append(String.format("%-20s | %-10d min | %-10d%n",
                stat.groupKey(), stat.totalMinutes(), stat.sessionCount())));
        sb.append("\n");
        return sb.toString();
    }

    private String formatSingleMetric(StatisticsService.MetricResult stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\033[1m\033[32m--- Result ---\033[0m\n");
        sb.append(String.format("\033[1mTarget: \033[0m%s%n", stat.groupKey()));
        sb.append(String.format("\033[1mTotal Time: \033[0m%d min%n", stat.totalMinutes()));
        sb.append(String.format("\033[1mSessions: \033[0m%d%n%n", stat.sessionCount()));
        return sb.toString();
    }
}
