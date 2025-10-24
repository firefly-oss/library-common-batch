package com.firefly.common.batch;

import com.firefly.common.batch.api.BatchJob;
import com.firefly.common.batch.api.ScheduledBatch;
import com.firefly.common.batch.audit.BatchAuditService;
import com.firefly.common.batch.autoconfig.BatchAutoConfiguration;
import com.firefly.common.batch.testsupport.CapturingTaskScheduler;
import com.firefly.common.batch.testsupport.InMemoryDistributedLockManager;
import com.firefly.common.batch.testsupport.TestBatchAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class LockingTests {

    @Test
    void withDistributedLockOnlyOneExecutionOccursUnderConcurrentRuns() throws Exception {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(LockingConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            TestBatchAuditService audit = ctx.getBean(TestBatchAuditService.class);
            SlowJob job = ctx.getBean(SlowJob.class);

            List<CapturingTaskScheduler.ScheduledItem> items = scheduler.getScheduledItems();
            assertThat(items).hasSize(1);

            Runnable r = items.get(0).getRunnable();

            Thread t1 = new Thread(r);
            t1.start();
            // Small delay to ensure first thread acquires the lock
            Thread.sleep(20);
            // Second attempt while first holds the lock
            r.run();

            t1.join();

            assertThat(job.getExecutionCount()).isEqualTo(1);
            assertThat(audit.getStartCount()).isEqualTo(1);
            assertThat(audit.getSuccessCount()).isEqualTo(1);
            assertThat(audit.getFailCount()).isEqualTo(0);
        });
    }

    @Test
    void withoutDistributedLockBothExecutionsOccur() throws Exception {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(NoLockingConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            TestBatchAuditService audit = ctx.getBean(TestBatchAuditService.class);
            SlowJob job = ctx.getBean(SlowJob.class);

            List<CapturingTaskScheduler.ScheduledItem> items = scheduler.getScheduledItems();
            assertThat(items).hasSize(1);

            Runnable r = items.get(0).getRunnable();

            Thread t1 = new Thread(r);
            t1.start();
            Thread.sleep(20);
            r.run();
            t1.join();

            assertThat(job.getExecutionCount()).isEqualTo(2);
            assertThat(audit.getStartCount()).isEqualTo(2);
            assertThat(audit.getSuccessCount()).isEqualTo(2);
            assertThat(audit.getFailCount()).isEqualTo(0);
        });
    }

    // Test configs
    @Configuration
    static class CommonBeans {
        @Bean
        TaskScheduler capturingTaskScheduler() { return new CapturingTaskScheduler(); }
        @Bean
        BatchAuditService testBatchAuditService() { return new TestBatchAuditService(); }
        @Bean
        SlowJob slowJob() { return new SlowJob(); }
    }

    @Configuration
    static class LockingConfig extends CommonBeans {
        @Bean
        InMemoryDistributedLockManager inMemoryDistributedLockManager() { return new InMemoryDistributedLockManager(); }
    }

    @Configuration
    static class NoLockingConfig extends CommonBeans { }

    @ScheduledBatch(cron = "*/2 * * * * *", enabled = true)
    static class SlowJob implements BatchJob {
        private final AtomicInteger count = new AtomicInteger();
        @Override
        public String getName() { return "slowJob"; }
        @Override
        public void execute() {
            count.incrementAndGet();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) { }
        }
        int getExecutionCount() { return count.get(); }
    }
}
