package de.mcworld.spring.sql.monitoring.explain.stack;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@Component
public class SqlExplainStackHolder implements SqlExplainStack {

    private final List<SqlExplainStackCallBack> listenerList = new ArrayList<>();
    private final LinkedList<SqlExplainStackElement> stackPair = new LinkedList<>();

    @Override
    public void add(String sql, SortedMap<Integer, Object> params) {
        stackPair.add(new SqlExplainStackElement(sql, params));
        listenerList.forEach(SqlExplainStackCallBack::newItem);
    }

    @Override
    public SqlExplainStackElement pop() {
        return stackPair.poll();
    }

    @Override
    public void setCallBack(SqlExplainStackCallBack sqlStackCallBack) {
        listenerList.add(sqlStackCallBack);
    }
}
