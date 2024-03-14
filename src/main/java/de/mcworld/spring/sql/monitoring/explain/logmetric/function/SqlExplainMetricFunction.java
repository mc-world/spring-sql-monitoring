package de.mcworld.spring.sql.monitoring.explain.logmetric.function;

import de.mcworld.spring.sql.monitoring.explain.config.SqlExplainConfigurationProperties;
import de.mcworld.spring.sql.monitoring.explain.logmetric.SqlExplainLogMetric;
import de.mcworld.spring.sql.monitoring.explain.logmetric.metric.SqlExplainMeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@Slf4j
@Component
@AllArgsConstructor
public class SqlExplainMetricFunction implements SqlExplainFunction {

    private final SqlExplainMeterRegistry sqlExplainMeterRegistry;
    private final SqlExplainConfigurationProperties properties;

    @Override
    public void addFunctionToParent(SqlExplainLogMetric parent) {
        if (properties.getFunctionList().contains("metric")) {
            parent.addChild(this);
        }
    }

    @Override
    public void accept(SqlExplainLogMetric.ExplainRecord explainRecord) {
        sqlExplainMeterRegistry.incrementCounter(explainRecord.costType(), explainRecord.explainSql());
    }
}
