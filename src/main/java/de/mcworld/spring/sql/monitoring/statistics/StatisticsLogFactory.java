package de.mcworld.spring.sql.monitoring.statistics;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.spi.StatisticsFactory;
import org.hibernate.stat.spi.StatisticsImplementor;

public class StatisticsLogFactory implements StatisticsFactory {

    @Override
    public StatisticsImplementor buildStatistics(
            SessionFactoryImplementor sessionFactory) {
        return new SqlStatisticsImpl(sessionFactory);
    }
}
