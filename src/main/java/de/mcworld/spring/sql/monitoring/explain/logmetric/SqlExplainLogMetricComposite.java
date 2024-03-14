package de.mcworld.spring.sql.monitoring.explain.logmetric;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;
import de.mcworld.spring.sql.monitoring.explain.config.SqlExplainConfigurationProperties;
import de.mcworld.spring.sql.monitoring.explain.logmetric.function.SqlExplainFunction;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@AllArgsConstructor
@Component
@Slf4j
public class SqlExplainLogMetricComposite implements SqlExplainLogMetric {

    private final SqlExplainConfigurationProperties explainProperties;
    private final List<SqlExplainFunction> sqlExplainFunctions;
    private final List<SqlExplainFunction> children = new ArrayList<>();

    @PostConstruct
    private void init() {
        sqlExplainFunctions.forEach(function -> function.addFunctionToParent(this));
    }

    @Override
    public void applyExplain(ExplainRecord explainRecord) {
        if (SqlExplainCostType.EXPLAIN_IGNORE.getCode() != explainRecord.costType().getCode()) {
            children.forEach(child -> child.accept(explainRecord));
        }
    }

    @Override
    public void addChild(SqlExplainFunction child) {
        children.add(child);
    }
}

