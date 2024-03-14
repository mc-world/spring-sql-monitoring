package de.mcworld.spring.sql.monitoring.explain.stack;

import java.util.SortedMap;

public interface SqlExplainStack {
    void add(String sql, SortedMap<Integer, Object> params);

    SqlExplainStackElement pop();

    void setCallBack(SqlExplainStackCallBack sqlStackCallBack);
}
