package de.mcworld.spring.sql.monitoring.explain.database;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;

import java.util.SortedMap;

public interface SqlExplain {
    SqlExplainCostType explain(String sql, SortedMap<Integer, Object> params);
}
