package de.mcworld.spring.sql.monitoring.explain.stack;

import java.util.SortedMap;

public record SqlExplainStackElement(String sql, SortedMap<Integer, Object> param) {
}
