package de.mcworld.spring.sql.monitoring.explain.database;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainAnalyser;
import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;
import de.mcworld.spring.sql.monitoring.explain.config.SqlExplainConfigurationProperties;
import de.mcworld.spring.sql.monitoring.explain.logmetric.SqlExplainLogMetric;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

@Component
@Slf4j
@AllArgsConstructor
@ConditionalOnExpression("${spring.jpa.properties.sql-monitoring.explain.enabled:true}")
@ConditionalOnProperty(value = "spring.jpa.properties.sql-monitoring.explain.database", havingValue = "postgres")
public class SqlExplainPostgres implements SqlExplain {

    private static final String EXPLAIN = "explain %s";

    private final SqlExplainConfigurationProperties properties;
    private final EntityManager em;
    private final SqlExplainLogMetric sqlExplainLogMetric;

    @PostConstruct
    public void init() {
        if (properties.getIgnoreList() == null) {
            properties.setIgnoreList(Collections.emptyList());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SqlExplainCostType explain(String sql, SortedMap<Integer, Object> params) {
        SqlExplainCostType result = SqlExplainCostType.EXPLAIN_UNKNOWN;
        String explainSql = String.format(EXPLAIN, sql);
        List<String> resultList = Collections.emptyList();
        Exception exception = null;
        try {
            if (properties.getIgnoreList().stream().anyMatch(s ->
                    sql.toLowerCase(Locale.getDefault()).contains(s))) {
                result = SqlExplainCostType.EXPLAIN_IGNORE;
            } else {
                Query query = em.createNativeQuery(explainSql);
                params.forEach(query::setParameter);
                resultList = query.getResultList();
                result = SqlExplainAnalyser.analyseResult(resultList, properties.getCostMax());
            }
        } catch (Exception ex) {
            result = SqlExplainCostType.EXPLAIN_EXCEPTION;
            exception = ex;
        } finally {
            sqlExplainLogMetric.applyExplain(new SqlExplainLogMetric.ExplainRecord(result, explainSql,
                    resultList, exception));
        }
        return result;
    }
}
