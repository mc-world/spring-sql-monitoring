package de.mcworld.spring.sql.monitoring.statistics.logmetric;

import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;

import java.util.*;
import java.util.stream.Collectors;

public class SqlStatisticsUtil {
    private static final String IGNORE_SQL = "explain";
    public static final String SQL_MONITORING_STATISTICS = "SQL-Monitoring-Statistics";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_QUERY = "monitoring.statistics.queryExecutionMaxTimeQuery";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME = "monitoring.statistics.queryExecutionMaxTime";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_COUNT = "monitoring.statistics.queryExecutionCount";
    public static final String CONTEXT_MONITORING_STATISTICS_ENTITY_FETCH_COUNT = "monitoring.statistics.entityFetchCount";
    public static final String CONTEXT_MONITORING_STATISTICS_PREPARE_STATEMENT_COUNT = "monitoring.statistics.prepareStatementCount";
    public static final String CONTEXT_MONITORING_STATISTICS_N_PLUS_ONE_SUSPECTED = "monitoring.statistics.nPlusOneSuspected";
    public static final String CONTEXT_MONITORING_STATISTICS_QUERY = "monitoring.statistics.query_";

    private SqlStatisticsUtil() {
    }

    public static List<String> getQueries(Statistics stats) {
        return Arrays.stream(stats.getQueries())
                .filter(s -> !s.startsWith(IGNORE_SQL))
                .collect(Collectors.toList());
    }

    public static Map<String, String> createContextMap(Statistics stats, List<String> queries) {
        Map<String, String> contextMap = new HashMap<>();
        int sizeDiff = stats.getQueries().length - queries.size();
        long executionMaxTime = stats.getQueryExecutionMaxTime();
        String executionMaxTimeQuery = stats.getQueryExecutionMaxTimeQueryString();
        long queryExecutionCount = stats.getQueryExecutionCount();
        long entityFetchCount = stats.getEntityFetchCount();
        long prepareStatementCount = stats.getPrepareStatementCount();
        if (queryExecutionCount == 0 &&
                entityFetchCount == 0 &&
                prepareStatementCount == 0) {
            return contextMap;
        } else if (sizeDiff == 0) {
            for (int i = 0; i < queries.size(); i++) {
                String query = queries.get(i);
                contextMap.put(CONTEXT_MONITORING_STATISTICS_QUERY + i, query);
            }
        } else {
            if (prepareStatementCount > sizeDiff) {
                prepareStatementCount = prepareStatementCount - sizeDiff;
            }
            queryExecutionCount = 0;
            executionMaxTime = 0;
            for (int i = 0; i < queries.size(); i++) {
                String query = queries.get(i);
                contextMap.put(CONTEXT_MONITORING_STATISTICS_QUERY + i, query);
                QueryStatistics queryStatistics = stats.getQueryStatistics(query);
                queryExecutionCount += queryStatistics.getExecutionCount();
                if (queryStatistics.getExecutionMaxTime() > executionMaxTime) {
                    executionMaxTime = queryStatistics.getExecutionMaxTime();
                    executionMaxTimeQuery = query;
                }
            }
        }
        contextMap.put(CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME_QUERY, executionMaxTimeQuery);
        contextMap.put(CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_MAX_TIME, "" + executionMaxTime);
        contextMap.put(CONTEXT_MONITORING_STATISTICS_QUERY_EXECUTION_COUNT, "" + queryExecutionCount);
        contextMap.put(CONTEXT_MONITORING_STATISTICS_ENTITY_FETCH_COUNT, "" + entityFetchCount);
        contextMap.put(CONTEXT_MONITORING_STATISTICS_PREPARE_STATEMENT_COUNT, "" + prepareStatementCount);
        boolean nPlusOneSuspected = (queryExecutionCount != 1 ||
                entityFetchCount != 0 ||
                prepareStatementCount != (queryExecutionCount + entityFetchCount));
        contextMap.put(CONTEXT_MONITORING_STATISTICS_N_PLUS_ONE_SUSPECTED, "" + nPlusOneSuspected);
        return contextMap;
    }

    public static Integer parseToInt(String value) {
        try {
            return Optional.ofNullable(value)
                    .map(v -> v.replaceAll("\\s+", ""))
                    .filter(v -> !v.isEmpty())
                    .map(Integer::valueOf)
                    .orElse(0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

