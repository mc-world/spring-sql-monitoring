package de.mcworld.spring.sql.monitoring.statistics.logmetric;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.function.SqlStatisticsAnalyseMaxTimeFunction;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.function.SqlStatisticsFunction;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.function.SqlStatisticsLoggerFunction;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.function.SqlStatisticsMetricFunction;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.stat.Statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SqlStatisticsLogMetricComposite implements SqlStatisticsLogMetric {

    private final SqlStatisticsProperties statisticsProperties;
    private final List<SqlStatisticsFunction> children = new ArrayList<>();

    public SqlStatisticsLogMetricComposite(SqlStatisticsProperties statisticsProperties) {
        this.statisticsProperties = statisticsProperties;
        initializeChildren(statisticsProperties);
    }

    private void initializeChildren(SqlStatisticsProperties statisticsProperties) {
        new SqlStatisticsLoggerFunction().addFunctionToParent(this, statisticsProperties);
        new SqlStatisticsMetricFunction().addFunctionToParent(this, statisticsProperties);
        new SqlStatisticsAnalyseMaxTimeFunction().addFunctionToParent(this, statisticsProperties);
    }

    @Override
    public void applyStats(Statistics stats) {
        final List<String> queries = SqlStatisticsUtil.getQueries(stats);
        if (!queries.isEmpty()) {
            final Map<String, String> contextMap = SqlStatisticsUtil.createContextMap(stats, queries);
            if (!contextMap.isEmpty()) {
                children.forEach(child -> child.accept(contextMap, queries));
            }
        }
        stats.clear();
    }

    @Override
    public void addChild(SqlStatisticsFunction child) {
        children.add(child);
    }
}

