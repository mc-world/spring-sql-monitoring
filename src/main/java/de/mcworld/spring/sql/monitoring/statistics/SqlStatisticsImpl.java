package de.mcworld.spring.sql.monitoring.statistics;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsLogMetricComposite;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsProperties;
import de.mcworld.spring.sql.monitoring.timer.SqlCronTimer;
import de.mcworld.spring.sql.monitoring.util.TriFunction;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.internal.StatisticsImpl;
import org.hibernate.stat.spi.StatisticsImplementor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SqlStatisticsImpl extends StatisticsImpl
        implements StatisticsImplementor {

    private final TriFunction<SqlStatisticsProperties, Statistics,
            SqlStatisticsLogMetricComposite, Boolean>
            functionAsync = (properties, stats, composite) -> {
        CompletableFuture.supplyAsync(() -> {
            composite.applyStats(stats);
            return true;
        });
        return true;
    };

    private final TriFunction<SqlStatisticsProperties, Statistics,
            SqlStatisticsLogMetricComposite, Boolean>
            functionAsyncTimeout = (properties, stats, composite) -> {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            composite.applyStats(stats);
            return true;
        }).orTimeout(properties.getAsyncTimeout(), TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (Exception e) {
            // Nothing doing
        }
        return true;
    };

    private final TriFunction<SqlStatisticsProperties, Statistics,
            SqlStatisticsLogMetricComposite, Boolean>
            functionSync = (properties, stats, composite) -> {
        composite.applyStats(stats);
        return true;
    };

    private final SessionFactoryImplementor sessionFactory;
    private SqlStatisticsProperties properties;
    private SqlCronTimer cronTimer;
    private SqlStatisticsLogMetricComposite logMetricComposite;
    private TriFunction<SqlStatisticsProperties, Statistics, SqlStatisticsLogMetricComposite, Boolean>
            function = (properties, stats, composite) -> true;

    public SqlStatisticsImpl(
            SessionFactoryImplementor sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
        if (sessionFactory.getSessionFactoryOptions().isStatisticsEnabled()) {
            super.setStatisticsEnabled(true);
        }
        initializeSqlStatisticsLogger(sessionFactory);
        initializeFunction();
    }

    private void initializeFunction() {
        if (properties.isStatisticsEnabled()) {
            if (properties.isAsyncDisabled()) {
                function = functionSync;
            } else {
                if (properties.getAsyncTimeout() > 0) {
                    function = functionAsyncTimeout;
                } else {
                    function = functionAsync;
                }
            }
        }
    }

    private void initializeSqlStatisticsLogger(SessionFactoryImplementor sessionFactory) {
        properties = new SqlStatisticsProperties(sessionFactory);
        logMetricComposite = new SqlStatisticsLogMetricComposite(properties);
        cronTimer = SqlCronTimer.parse(properties.getCronExpression());
    }

    @Override
    public void closeSession() {
        super.closeSession();
        if (cronTimer.isCurrentNextDateTime()) {
            function.apply(properties, sessionFactory.getStatistics(), logMetricComposite);
        }
    }
}