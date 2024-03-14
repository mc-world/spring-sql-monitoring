package de.mcworld.spring.sql.monitoring.explain.config;

import de.mcworld.spring.sql.monitoring.explain.stack.SqlExplainStack;
import de.mcworld.spring.sql.monitoring.timer.SqlCronTimer;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
public class SqlExplainQueryExecution implements ProxyDataSourceBuilder.SingleQueryExecution {

    final SqlExplainStack sqlExplainStack;
    private final SqlCronTimer sqlCronTimer;

    public SqlExplainQueryExecution(SqlExplainStack sqlExplainStack,
                                    SqlExplainConfigurationProperties properties) {
        this.sqlExplainStack = sqlExplainStack;
        sqlCronTimer = SqlCronTimer.parse(properties.getCronExpression());
    }

    @Override
    public void execute(ExecutionInfo executionInfo, List<QueryInfo> list) {
        if (sqlCronTimer.isCurrentNextDateTime()) {
            list.forEach(queryInfo -> {
                String sql = queryInfo.getQuery();
                if (SqlExplainAcceptedStatement.acceptedForExplain(sql)) {
                    SortedMap<Integer, Object> params = getParameterList(queryInfo.getParametersList());
                    sqlExplainStack.add(sql, params);
                }
            });
        }
    }

    private static SortedMap<Integer, Object> getParameterList(List<List<ParameterSetOperation>> paramsList) {
        final SortedMap<Integer, Object> params = new TreeMap<>();
        paramsList.forEach(parameterSetOperations -> parameterSetOperations.forEach(param -> {
            if (param != null) {
                Integer key = (int) param.getArgs()[0];
                Object value = null;
                if (!ParameterSetOperation.isSetNullParameterOperation(param)) {
                    value = param.getArgs()[1];
                }
                params.put(key, value);
            }
        }));
        return params;
    }
}
