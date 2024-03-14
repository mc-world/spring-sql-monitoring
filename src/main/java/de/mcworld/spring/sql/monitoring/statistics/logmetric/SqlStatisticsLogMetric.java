package de.mcworld.spring.sql.monitoring.statistics.logmetric;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.function.SqlStatisticsFunction;
import org.hibernate.stat.Statistics;

public interface SqlStatisticsLogMetric {
    void applyStats(Statistics stats);

    public void addChild(SqlStatisticsFunction child);
}
