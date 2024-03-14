package de.mcworld.spring.sql.monitoring.explain.logmetric.metric;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@Component
public class SqlExplainMeterRegistry {

    private final Map<String, Map<SqlExplainCostType, Counter>> explainCounters = new HashMap<>();

    private MeterRegistry meterRegistry;

    public SqlExplainMeterRegistry(@Nullable MeterRegistry meterRegistry) {
        initializeCounter(meterRegistry);
    }

    private void initializeCounter(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNullElse(meterRegistry, Metrics.globalRegistry);
    }

    public void incrementCounter(SqlExplainCostType sqlExplainCostType, String explainSql) {
        getCounter(sqlExplainCostType, explainSql).increment();
    }

    private Counter getCounter(SqlExplainCostType sqlExplainCostType, String explainSql) {
        Map<SqlExplainCostType, Counter> counterMap = explainCounters.computeIfAbsent(explainSql,
                changeType1 -> new HashMap<>());
        return counterMap.computeIfAbsent(sqlExplainCostType,
                sqlExplainCostType1 -> buildCounter(sqlExplainCostType1, explainSql));
    }

    private Counter buildCounter(SqlExplainCostType sqlExplainCostType, String explainSql) {
        return Counter.builder(sqlExplainCostType.getMetric())
                .description(sqlExplainCostType.getDescription())
                .tags("sql", explainSql)
                .register(meterRegistry);
    }
}
