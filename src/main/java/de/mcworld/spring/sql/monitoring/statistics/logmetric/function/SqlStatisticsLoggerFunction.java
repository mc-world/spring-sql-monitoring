package de.mcworld.spring.sql.monitoring.statistics.logmetric.function;

import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsLogMetric;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsProperties;
import de.mcworld.spring.sql.monitoring.statistics.logmetric.SqlStatisticsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;

import java.util.List;
import java.util.Map;

@Slf4j
public class SqlStatisticsLoggerFunction implements SqlStatisticsFunction {

    @Override
    public void addFunctionToParent(SqlStatisticsLogMetric parent, SqlStatisticsProperties statisticsProperties) {
        if (statisticsProperties.getFunctionList().contains("logger")) {
            parent.addChild(this);
        }
    }

    @Override
    public void accept(Map<String, String> contextMap, List<String> queries) {
        ThreadContext.putAll(contextMap);
        log.info(SqlStatisticsUtil.SQL_MONITORING_STATISTICS);
        ThreadContext.removeAll(contextMap.keySet());
    }
}

