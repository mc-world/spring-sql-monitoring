package de.mcworld.spring.sql.monitoring.explain.config;

import java.util.Arrays;
import java.util.Locale;

public enum SqlExplainAcceptedStatement {

    SELECT, DELETE, INSERT, REPLACE, UPDATE, EXPLAIN;

    public static boolean acceptedForExplain(String sql) {
        final String sqlUpperCase = sql.toUpperCase(Locale.getDefault());
        return (!sqlUpperCase.contains(EXPLAIN.name()))
                && Arrays.stream(values()).anyMatch(
                e -> sqlUpperCase.contains(e.name()));
    }
}
