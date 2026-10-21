/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.common.batch;

import io.fireflysoftware.common.batch.api.BatchJob;
import io.fireflysoftware.common.batch.api.ScheduledBatch;
import io.fireflysoftware.common.batch.audit.BatchAuditService;
import io.fireflysoftware.common.batch.autoconfig.BatchAutoConfiguration;
import io.fireflysoftware.common.batch.testsupport.CapturingTaskScheduler;
import io.fireflysoftware.common.batch.testsupport.InMemoryDistributedLockManager;
import io.fireflysoftware.common.batch.testsupport.TestBatchAuditService;
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
