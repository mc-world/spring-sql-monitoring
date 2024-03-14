package de.mcworld.spring.sql.monitoring.explain.logmetric.function;

import de.mcworld.spring.sql.monitoring.explain.logmetric.SqlExplainLogMetric;

import java.util.function.Consumer;

public interface SqlExplainFunction extends Consumer<SqlExplainLogMetric.ExplainRecord> {

    void addFunctionToParent(SqlExplainLogMetric parent);
}
