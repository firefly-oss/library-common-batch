package com.firefly.common.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "firefly.batch")
/**
 * Configuration properties that control the behavior of the batch library.
 * Bind these under the "firefly.batch" prefix in application properties.
 */
public class BatchProperties {
    /** Global switch to turn on/off the library's scheduling and registration. */
    private boolean enabled = true;

    private final Scheduler scheduler = new Scheduler();
    private final Audit audit = new Audit();
    private final Lock lock = new Lock();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Scheduler getScheduler() { return scheduler; }
    public Audit getAudit() { return audit; }
    public Lock getLock() { return lock; }

    public static class Scheduler {
        /** Enable/disable scheduling registration. */
        private boolean enabled = true;
        /** Thread pool size for the internal TaskScheduler when library provides it. */
        private int poolSize = 2;
        /** Thread name prefix for the internal TaskScheduler. */
        private String threadNamePrefix = "batch-scheduler-";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public String getThreadNamePrefix() { return threadNamePrefix; }
        public void setThreadNamePrefix(String threadNamePrefix) { this.threadNamePrefix = threadNamePrefix; }
    }

    public static class Audit {
        /** Enable/disable auditing. When disabled, NoOp is used even if a DB mode is configured. */
        private boolean enabled = true;
        /** Audit mode: "noop" (default) or "r2dbc" for DB persistence using R2DBC. */
        private String mode = "noop";
        /** When using DB mode, the table name to persist records. */
        private String table = "batch_audit";
        /** If true and Flyway is on the classpath, include library migrations to create the audit table. */
        private boolean createTable = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getTable() { return table; }
        public void setTable(String table) { this.table = table; }
        public boolean isCreateTable() { return createTable; }
        public void setCreateTable(boolean createTable) { this.createTable = createTable; }
    }

    public static class Lock {
        /** Enable/disable distributed locking usage if a provider is available. */
        private boolean enabled = true;
        /** Which provider to adapt to when none custom DistributedLockManager is present: "auto" or "shedlock". */
        private String provider = "auto";
        /** Default ShedLock-compatible lockAtMostFor. */
        private Duration atMostFor = Duration.ofMinutes(10);
        /** Default ShedLock-compatible lockAtLeastFor. */
        private Duration atLeastFor = Duration.ZERO;
        /** If true and Flyway is on the classpath, include library migrations to create the ShedLock table. */
        private boolean createTable = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public Duration getAtMostFor() { return atMostFor; }
        public void setAtMostFor(Duration atMostFor) { this.atMostFor = atMostFor; }
        public Duration getAtLeastFor() { return atLeastFor; }
        public void setAtLeastFor(Duration atLeastFor) { this.atLeastFor = atLeastFor; }
        public boolean isCreateTable() { return createTable; }
        public void setCreateTable(boolean createTable) { this.createTable = createTable; }
    }
}
