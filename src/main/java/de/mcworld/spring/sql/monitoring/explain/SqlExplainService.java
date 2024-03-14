package de.mcworld.spring.sql.monitoring.explain;

import de.mcworld.spring.sql.monitoring.explain.analyse.SqlExplainCostType;
import de.mcworld.spring.sql.monitoring.explain.config.SqlExplainConfigurationProperties;
import de.mcworld.spring.sql.monitoring.explain.database.SqlExplain;
import de.mcworld.spring.sql.monitoring.explain.scheduler.CustomTaskScheduler;
import de.mcworld.spring.sql.monitoring.explain.stack.SqlExplainStack;
import de.mcworld.spring.sql.monitoring.explain.stack.SqlExplainStackCallBack;
import de.mcworld.spring.sql.monitoring.explain.stack.SqlExplainStackElement;
import de.mcworld.spring.sql.monitoring.util.TriFunction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@Component
public class SqlExplainService implements SqlExplainStackCallBack, SqlExplainWork {

    public static final String SQL_EXPLAIN_TASK_ID = UUID.randomUUID().toString();

    private final TriFunction<SqlExplain, SqlExplainStackElement, Long, Boolean>
            functionSync = (sqlExplain, sqlParam, asyncTimeout) -> {
        sqlExplain.explain(sqlParam.sql(), sqlParam.param());
        return true;
    };

    private final TriFunction<SqlExplain, SqlExplainStackElement, Long, Boolean>
            functionAsync = (sqlExplain, sqlParam, asyncTimeout) -> {

        CompletableFuture.supplyAsync(() ->
                sqlExplain.explain(sqlParam.sql(), sqlParam.param())
        );
        return true;
    };

    private final TriFunction<SqlExplain, SqlExplainStackElement, Long, Boolean>
            functionAsyncTimeout = (sqlExplain, sqlParam, asyncTimeout) -> {
        CompletableFuture<SqlExplainCostType> future = CompletableFuture.supplyAsync(() ->
                sqlExplain.explain(sqlParam.sql(), sqlParam.param())
        ).orTimeout(asyncTimeout, TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (Exception e) {
            return false;
        }
        return true;
    };

    public final CustomTaskScheduler customTaskScheduler;
    private final SqlExplainStack sqlExplainStack;
    private final SqlExplain sqlExplain;
    private final SqlExplainConfigurationProperties properties;
    private TriFunction<SqlExplain, SqlExplainStackElement, Long, Boolean> function =
            (sqlExplain, sqlParam, asyncTimeout) -> true;

    @PostConstruct
    private void init() {
        sqlExplainStack.setCallBack(this);
        initializeFunction();
    }

    private void initializeFunction() {
        if (properties.isAsyncEnabled()) {
            if (properties.getAsyncTimeout() > 0) {
                function = functionAsyncTimeout;
            } else {
                function = functionAsync;
            }
        } else {
            function = functionSync;
        }
    }

    @Override
    public void newItem() {
        customTaskScheduler.schedule(this::scheduleTask, SQL_EXPLAIN_TASK_ID);
    }

    public void scheduleTask() {
        SqlExplainStackElement sqlParam = sqlExplainStack.pop();
        while (sqlParam != null) {
            function.apply(sqlExplain, sqlParam, properties.getAsyncTimeout());
            sqlParam = sqlExplainStack.pop();
        }
        customTaskScheduler.cancelTask(SQL_EXPLAIN_TASK_ID);
    }

    @Override
    public boolean inWork() {
        return customTaskScheduler.existTask(SQL_EXPLAIN_TASK_ID);
    }
}