package de.mcworld.spring.sql.monitoring.explain.logmetric.function;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;
import de.mcworld.spring.sql.monitoring.explain.config.SqlExplainConfigurationProperties;
import de.mcworld.spring.sql.monitoring.explain.logmetric.SqlExplainLogMetric;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@Slf4j
@Component
@AllArgsConstructor
public class SqlExplainLoggerFunction implements SqlExplainFunction {

    private static final String LOG_MESSAGE = "SQL-Monitoring-Explain";
    public static final String CONTEXT_MONITORING_EXPLAIN_COST = "monitoring.explain.cost";
    public static final String CONTEXT_MONITORING_EXPLAIN_SQL = "monitoring.explain.sql";
    public static final String CONTEXT_MONITORING_EXPLAIN_SQL_RESULT = "monitoring.explain.sql.result";
    private static final List<String> THREAD_CONTEXT_KEYS = List.of(CONTEXT_MONITORING_EXPLAIN_COST,
            CONTEXT_MONITORING_EXPLAIN_SQL,
            CONTEXT_MONITORING_EXPLAIN_SQL_RESULT);
    private final SqlExplainConfigurationProperties properties;

    @Override
    public void addFunctionToParent(SqlExplainLogMetric parent) {
        if (properties.getFunctionList().contains("logger")) {
            parent.addChild(this);
        }
    }

    @Override
    public void accept(SqlExplainLogMetric.ExplainRecord explainRecord) {
        logging(explainRecord.costType(), explainRecord.explainSql(), explainRecord.resultList(),
                explainRecord.exception());
    }

    private static void logging(SqlExplainCostType costType, String explainSql, List<String> resultList, Exception ex) {
        putThreadContext(costType, explainSql, resultList);
        if (SqlExplainCostType.EXPLAIN_COST_OK.getCode() == costType.getCode()) {
            log.info(LOG_MESSAGE);
        } else if (SqlExplainCostType.EXPLAIN_COST_TO_HIGH.getCode() == costType.getCode() ||
                SqlExplainCostType.EXPLAIN_EXCEPTION.getCode() == costType.getCode()) {
            log.error(LOG_MESSAGE, ex);
        } else {
            log.warn(LOG_MESSAGE);
        }
        ThreadContext.removeAll(THREAD_CONTEXT_KEYS);
    }

    private static void putThreadContext(SqlExplainCostType result, String explainSql, List<String> resultList) {
        ThreadContext.put(CONTEXT_MONITORING_EXPLAIN_COST, result.name());
        ThreadContext.put(CONTEXT_MONITORING_EXPLAIN_SQL, explainSql);
        ThreadContext.put(CONTEXT_MONITORING_EXPLAIN_SQL_RESULT, resultList.toString());
    }
}
