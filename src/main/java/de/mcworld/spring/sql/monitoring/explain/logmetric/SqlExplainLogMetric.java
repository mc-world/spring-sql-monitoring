package de.mcworld.spring.sql.monitoring.explain.logmetric;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;
import de.mcworld.spring.sql.monitoring.explain.logmetric.function.SqlExplainFunction;

import java.util.List;

public interface SqlExplainLogMetric {
    record ExplainRecord(SqlExplainCostType costType, String explainSql, List<String> resultList, Exception exception) {
    }

    void applyExplain(ExplainRecord explainRecord);

    public void addChild(SqlExplainFunction child);
}
