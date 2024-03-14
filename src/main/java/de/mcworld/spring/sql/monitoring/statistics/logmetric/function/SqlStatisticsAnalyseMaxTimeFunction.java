package de.mcworld.spring.sql.monitoring.statistics.logmetric.function;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsLogMetric;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsProperties;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsUtil;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.metric.SqlStatisticsMeterRegistry;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.metric.SqlStatisticsMetricType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class SqlStatisticsAnalyseMaxTimeFunction implements SqlStatisticsFunction {
    public static final String SQL_MONITORING_STATISTICS_MAX_TIME = "SQL-Monitoring-Statistics-Max-Time";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_TO_HIGH = "monitoring.statistics.queryExecutionMaxTime_to_high";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_QUERY = "monitoring.statistics.queryExecutionMaxTime_query";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME = "monitoring.statistics.queryExecutionMaxTime";
    public static final List<String> CONTEXT_MONITORING_STATISTICS_LIST = List.of(
            CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_TO_HIGH,
            CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_QUERY,
            CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME);
    private final SqlStatisticsMeterRegistry meterRegistry = new SqlStatisticsMeterRegistry();
    private int executionMaxTime = 0;

    @Override
    public void addFunctionToParent(SqlStatisticsLogMetric parent, SqlStatisticsProperties statisticsProperties) {
        if ((executionMaxTime = statisticsProperties.getExecutionMaxTime()) > 0
                && statisticsProperties.getFunctionList().contains("analyseMaxTime")) {
            parent.addChild(this);
        }
    }

    @Override
    public void accept(Map<String, String> contextMap, List<String> queries) {
        String maxTimeQuery = contextMap.get(
                SqlStatisticsUtil.CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_QUERY);
        int maxTime = getMaxTime(contextMap);
        boolean isExecutionMaxTimeToHigh = maxTime >= executionMaxTime;
        exposeMetric(List.of(maxTimeQuery), isExecutionMaxTimeToHigh);
        logging(maxTimeQuery, maxTime, isExecutionMaxTimeToHigh);
    }

    private static int getMaxTime(Map<String, String> contextMap) {
        try {
            String maxTimeStr = contextMap.get(
                    SqlStatisticsUtil.CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME);
            return Integer.parseInt(maxTimeStr);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public void logging(String maxTimeQuery, int maxTime, boolean isExecutionMaxTimeToHigh) {
        ThreadContext.put(CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_TO_HIGH, "" + isExecutionMaxTimeToHigh);
        ThreadContext.put(CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_QUERY, maxTimeQuery);
        ThreadContext.put(CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME, "" + maxTime);
        log.info(SQL_MONITORING_STATISTICS_MAX_TIME);
        ThreadContext.removeAll(CONTEXT_MONITORING_STATISTICS_LIST);
    }

    private void exposeMetric(List<String> queries, boolean isExecutionMaxTimeToHigh) {
        if (isExecutionMaxTimeToHigh) {
            meterRegistry.incrementCounter(SqlStatisticsMetricType.STATISTICS_EXECUTION_MAX_TIME_TO_HIGH, queries);
        } else {
            meterRegistry.incrementCounter(SqlStatisticsMetricType.STATISTICS_EXECUTION_MAX_TIME_OK, queries);
        }
    }
}

