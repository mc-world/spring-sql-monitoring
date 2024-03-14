package de.mcworld.spring.sql.monitoring.statistics.logmetric.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SqlStatisticsMetricType {

    STATISTICS_N_PLUS_ONE_SUSPECTED("N plus one suspected.", "monitoring_statistics_nplus1_suspected_count"),
    STATISTICS_N_PLUS_ONE_OK("N plus one okay.", "monitoring_statistics_nplus1_ok_count"),
    STATISTICS_EXECUTION_MAX_TIME_TO_HIGH("Execution max time too high.", "monitoring_statistics_execution_max_time_to_high_count"),
    STATISTICS_EXECUTION_MAX_TIME_OK("Execution max time max okay.", "monitoring_statistics_execution_max_time_ok_count");

    private final String description;
    private final String metric;
}
