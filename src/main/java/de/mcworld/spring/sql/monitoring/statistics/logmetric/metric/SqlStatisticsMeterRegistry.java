package de.mcworld.spring.sql.monitoring.statistics.logmetric.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlStatisticsMeterRegistry {

    private final Map<String, Map<SqlStatisticsMetricType, Counter>> statisticsCounters = new ConcurrentHashMap<>();

    public void incrementCounter(SqlStatisticsMetricType sqlStatisticsMetricType, List<String> counterTags) {
        Map<SqlStatisticsMetricType, Counter> counterMap = statisticsCounters.computeIfAbsent(counterTags.toString(),
                changeType1 -> new HashMap<>());
        Counter counter = counterMap.computeIfAbsent(sqlStatisticsMetricType,
                sqlExplainCostType1 -> buildCounter(sqlStatisticsMetricType, counterTags));
        counter.increment();
    }

    private Counter buildCounter(SqlStatisticsMetricType sqlStatisticsMetricType, List<String> counterTags) {
        return Counter.builder(sqlStatisticsMetricType.getMetric())
                .description(sqlStatisticsMetricType.getDescription())
                .tags("sql", counterTags.toString())
                .register(Metrics.globalRegistry);
    }
}
