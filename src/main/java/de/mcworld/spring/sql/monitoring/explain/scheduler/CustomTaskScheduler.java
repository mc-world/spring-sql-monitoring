package de.mcworld.spring.sql.monitoring.explain.scheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Component
public class CustomTaskScheduler extends ThreadPoolTaskScheduler {

    private final Map<String, Future<?>> scheduledTasks = new IdentityHashMap<>();

    public CustomTaskScheduler() {
        setPoolSize(2);
        setThreadNamePrefix("SqlExplainThreadPoolTaskScheduler");
    }

    public void schedule(Runnable task, String id) {
        if (scheduledTasks.containsKey(id)) {
            return;
        }
        Future<?> future = super.submit(task);
        scheduledTasks.put(id, future);
    }

    public void scheduleAtFixedRate(Runnable task, Duration period, String id) {
        if (scheduledTasks.containsKey(id)) {
            return;
        }
        ScheduledFuture<?> future = super.scheduleAtFixedRate(task, period);
        scheduledTasks.put(id, future);
    }

    public void cancelTask(String id) {
        Future<?> future = scheduledTasks.get(id);
        if (future != null) {
            future.cancel(true);
            scheduledTasks.remove(id);
        }
    }

    public boolean existTask(String id) {
        return scheduledTasks.get(id) != null;
    }
}
