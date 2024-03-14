package de.mcworld.spring.sql.monitoring.statistics.logmetric;

import lombok.Data;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class SqlStatisticsProperties {

    private boolean statsClear;
    private String cronExpression;
    private long asyncTimeout;
    private boolean asyncDisabled;
    private boolean statisticsEnabled;
    private int executionMaxTime;
    private List<String> functionList = new ArrayList<>();

    public SqlStatisticsProperties(SessionFactoryImplementor sessionFactory) {
        ConfigurationService config = sessionFactory.getServiceRegistry()
                .getService(ConfigurationService.class);
        Map<String, Object> settings = config.getSettings();
        statisticsEnabled = "true".equals(settings.get("sql-monitoring.statistics.enabled"));
        cronExpression = (String) settings.get("sql-monitoring.statistics.cronExpression");
        asyncDisabled = "true".equals(settings.get("sql-monitoring.statistics.asyncDisabled"));
        asyncTimeout = SqlStatisticsUtil.parseToInt(
                (String) settings.get("sql-monitoring.statistics.asyncTimeout"));
        executionMaxTime = SqlStatisticsUtil.parseToInt(
                (String) settings.get("sql-monitoring.statistics.executionMaxTime"));
        functionList.add((String) settings.get("sql-monitoring.statistics.functionList.0"));
        functionList.add((String) settings.get("sql-monitoring.statistics.functionList.1"));
        functionList.add((String) settings.get("sql-monitoring.statistics.functionList.2"));
        functionList.removeAll(Collections.singleton(null));
    }
}