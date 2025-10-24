package com.firefly.common.batch.testsupport;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Test TaskScheduler that captures scheduled tasks for assertions. Only the
 * schedule(Runnable, Trigger) method is implemented as it's the one used by the library.
 */
public class CapturingTaskScheduler implements TaskScheduler {

    public static class ScheduledItem {
        public final Runnable runnable;
        public final Trigger trigger;
        ScheduledItem(Runnable runnable, Trigger trigger) {
            this.runnable = runnable;
            this.trigger = trigger;
        }
        public Runnable getRunnable() { return runnable; }
        public Trigger getTrigger() { return trigger; }
        public String getCronExpressionOrToString() {
            if (trigger instanceof CronTrigger) {
                // Try to read expression via public accessor if available; otherwise fall back to toString
                try {
                    return (String) CronTrigger.class.getMethod("getExpression").invoke(trigger);
                } catch (Exception ignored) {
                    return trigger.toString();
                }
            }
            return trigger.toString();
        }
    }

    private final List<ScheduledItem> scheduled = Collections.synchronizedList(new ArrayList<>());

    public List<ScheduledItem> getScheduledItems() {
        return scheduled;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        scheduled.add(new ScheduledItem(task, trigger));
        return null; // Not needed for tests
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        throw new UnsupportedOperationException("schedule(Runnable, Instant) not supported in tests");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        throw new UnsupportedOperationException("scheduleAtFixedRate(Instant, Duration) not supported in tests");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        throw new UnsupportedOperationException("scheduleAtFixedRate(Duration) not supported in tests");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        throw new UnsupportedOperationException("scheduleWithFixedDelay(Instant, Duration) not supported in tests");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        throw new UnsupportedOperationException("scheduleWithFixedDelay(Duration) not supported in tests");
    }
}