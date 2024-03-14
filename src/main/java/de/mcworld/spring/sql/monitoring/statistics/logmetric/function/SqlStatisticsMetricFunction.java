package de.mcworld.spring.sql.monitoring.statistics.logmetric.function;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsLogMetric;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsProperties;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsUtil;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.metric.SqlStatisticsMeterRegistry;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.metric.SqlStatisticsMetricType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class SqlStatisticsMetricFunction implements SqlStatisticsFunction {
    private final SqlStatisticsMeterRegistry meterRegistry = new SqlStatisticsMeterRegistry();

    @Override
    public void addFunctionToParent(SqlStatisticsLogMetric parent, SqlStatisticsProperties statisticsProperties) {
        if (statisticsProperties.getFunctionList().contains("metric")) {
            parent.addChild(this);
        }
    }

    @Override
    public void accept(Map<String, String> contextMap, List<String> queries) {
        String nPlusOneSuspected = contextMap.get(
                SqlStatisticsUtil.CONTEXT_MONITORING_STATISTICS_N_PLUS_ONE_SUSPECTED);
        exposeMetric(queries, nPlusOneSuspected);
    }

    private void exposeMetric(List<String> queries, String nPlusOneSuspected) {
        if (nPlusOneSuspected.equals("true")) {
            meterRegistry.incrementCounter(SqlStatisticsMetricType.STATISTICS_N_PLUS_ONE_SUSPECTED, queries);
        } else {
            meterRegistry.incrementCounter(SqlStatisticsMetricType.STATISTICS_N_PLUS_ONE_OK, queries);
        }
    }
}

