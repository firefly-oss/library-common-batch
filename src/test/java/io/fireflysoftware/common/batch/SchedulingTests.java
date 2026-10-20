package io.fireflysoftware.common.batch;

import io.fireflysoftware.common.batch.api.BatchJob;
import io.fireflysoftware.common.batch.api.ScheduledBatch;
import io.fireflysoftware.common.batch.audit.BatchAuditService;
import io.fireflysoftware.common.batch.autoconfig.BatchAutoConfiguration;
import io.fireflysoftware.common.batch.testsupport.CapturingTaskScheduler;
import io.fireflysoftware.common.batch.testsupport.TestBatchAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingTests {

    @Test
    void enabledAnnotatedJobIsScheduledAndExecutesSuccessfully() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(SuccessJobConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            TestBatchAuditService audit = ctx.getBean(TestBatchAuditService.class);
            SuccessJob job = ctx.getBean(SuccessJob.class);

            List<CapturingTaskScheduler.ScheduledItem> items = scheduler.getScheduledItems();
            assertThat(items).hasSize(1);
            Trigger trigger = items.get(0).getTrigger();
            assertThat(trigger).isInstanceOf(CronTrigger.class);
            assertThat(items.get(0).getCronExpressionOrToString()).contains("*/2");

            // Execute the scheduled runnable manually
            items.get(0).getRunnable().run();

            assertThat(audit.getStartCount()).isEqualTo(1);
            assertThat(audit.getSuccessCount()).isEqualTo(1);
            assertThat(audit.getFailCount()).isEqualTo(0);
            assertThat(job.getExecutionCount()).isEqualTo(1);
        });
    }

    @Test
    void failingJobReportsFailure() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(FailingJobConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            TestBatchAuditService audit = ctx.getBean(TestBatchAuditService.class);

            List<CapturingTaskScheduler.ScheduledItem> items = scheduler.getScheduledItems();
            assertThat(items).hasSize(1);
            items.get(0).getRunnable().run();

            assertThat(audit.getStartCount()).isEqualTo(1);
            assertThat(audit.getSuccessCount()).isEqualTo(0);
            assertThat(audit.getFailCount()).isEqualTo(1);
            assertThat(audit.getEvents().get(audit.getEvents().size()-1).errorMessage).contains("boom");
        });
    }

    @Test
    void disabledJobIsNotScheduled() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(DisabledJobConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            assertThat(scheduler.getScheduledItems()).isEmpty();
        });
    }

    @Test
    void nonAnnotatedBatchJobIsNotScheduled() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(NonAnnotatedJobConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            assertThat(scheduler.getScheduledItems()).isEmpty();
        });
    }

    @Test
    void multipleJobsAreRegisteredIndependently() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class))
                .withUserConfiguration(MultipleJobsConfig.class);

        runner.run(ctx -> {
            CapturingTaskScheduler scheduler = ctx.getBean(CapturingTaskScheduler.class);
            List<CapturingTaskScheduler.ScheduledItem> items = scheduler.getScheduledItems();
            assertThat(items).hasSize(2);
            assertThat(items.get(0).getTrigger()).isInstanceOf(CronTrigger.class);
            assertThat(items.get(1).getTrigger()).isInstanceOf(CronTrigger.class);
        });
    }

    // Test beans/configurations

    @Configuration
    static class CommonBeansConfig {
        @Bean
        TaskScheduler capturingTaskScheduler() { return new CapturingTaskScheduler(); }
        @Bean
        BatchAuditService testBatchAuditService() { return new TestBatchAuditService(); }
    }

    @Configuration
    static class SuccessJobConfig extends CommonBeansConfig {
        @Bean
        SuccessJob successJob() { return new SuccessJob(); }
    }

    @Configuration
    static class FailingJobConfig extends CommonBeansConfig {
        @Bean
        FailingJob failingJob() { return new FailingJob(); }
    }

    @Configuration
    static class DisabledJobConfig extends CommonBeansConfig {
        @Bean
        DisabledJob disabledJob() { return new DisabledJob(); }
    }

    @Configuration
    static class NonAnnotatedJobConfig extends CommonBeansConfig {
        @Bean
        NonAnnotatedJob nonAnnotatedJob() { return new NonAnnotatedJob(); }
    }

    @Configuration
    static class MultipleJobsConfig extends CommonBeansConfig {
        @Bean
        SuccessJob1 job1() { return new SuccessJob1(); }
        @Bean
        SuccessJob2 job2() { return new SuccessJob2(); }
    }

    @ScheduledBatch(cron = "*/2 * * * * *", enabled = true, description = "success job")
    static class SuccessJob implements BatchJob {
        private final AtomicInteger count = new AtomicInteger();
        @Override public String getName() { return "successJob"; }
        @Override public void execute() { count.incrementAndGet(); }
        int getExecutionCount() { return count.get(); }
    }

    @ScheduledBatch(cron = "*/3 * * * * *", enabled = true, description = "failing job")
    static class FailingJob implements BatchJob {
        @Override public String getName() { return "failingJob"; }
        @Override public void execute() { throw new RuntimeException("boom"); }
    }

    @ScheduledBatch(cron = "*/5 * * * * *", enabled = false, description = "disabled job")
    static class DisabledJob implements BatchJob {
        @Override public String getName() { return "disabledJob"; }
        @Override public void execute() { /* no-op */ }
    }

    static class NonAnnotatedJob implements BatchJob {
        @Override public String getName() { return "nonAnnotated"; }
        @Override public void execute() { /* no-op */ }
    }

    @ScheduledBatch(cron = "*/7 * * * * *")
    static class SuccessJob1 implements BatchJob {
        @Override public String getName() { return "job1"; }
        @Override public void execute() { }
    }

    @ScheduledBatch(cron = "*/11 * * * * *")
    static class SuccessJob2 implements BatchJob {
        @Override public String getName() { return "job2"; }
        @Override public void execute() { }
    }
}