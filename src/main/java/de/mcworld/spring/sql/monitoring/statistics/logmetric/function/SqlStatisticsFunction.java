package de.mcworld.spring.sql.monitoring.statistics.logmetric.function;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsLogMetric;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsProperties;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface SqlStatisticsFunction extends BiConsumer<Map<String, String>, List<String>> {

    void addFunctionToParent(SqlStatisticsLogMetric parent, SqlStatisticsProperties statisticsProperties);
}
