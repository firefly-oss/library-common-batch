package io.fireflysoftware.common.batch;

import io.fireflysoftware.common.batch.audit.BatchAuditService;
import io.fireflysoftware.common.batch.audit.NoOpBatchAuditService;
import io.fireflysoftware.common.batch.autoconfig.BatchAutoConfiguration;
import io.fireflysoftware.common.batch.autoconfig.ScheduledBatchRegistrar;
import io.fireflysoftware.common.batch.scheduling.BatchSchedulerService;
import io.fireflysoftware.common.batch.testsupport.CapturingTaskScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class));

    @Test
    void defaultBeansAreProvided() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(TaskScheduler.class);
            assertThat(ctx.getBean(TaskScheduler.class)).isInstanceOf(ThreadPoolTaskScheduler.class);

            assertThat(ctx).hasSingleBean(BatchAuditService.class);
            assertThat(ctx.getBean(BatchAuditService.class)).isInstanceOf(NoOpBatchAuditService.class);

            assertThat(ctx).hasSingleBean(BatchSchedulerService.class);
            assertThat(ctx).hasSingleBean(ScheduledBatchRegistrar.class);
        });
    }

    @Test
    void userProvidedAuditServiceOverridesDefault() {
        contextRunner.withUserConfiguration(CustomAuditConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(BatchAuditService.class);
            assertThat(ctx.getBean(BatchAuditService.class).getClass().getName()).isEqualTo(CustomAuditService.class.getName());
            assertThat(ctx.getBeansOfType(NoOpBatchAuditService.class)).isEmpty();
        });
    }

    @Test
    void userProvidedTaskSchedulerOverridesDefault() {
        contextRunner.withUserConfiguration(CustomSchedulerConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(TaskScheduler.class);
            assertThat(ctx.getBean(TaskScheduler.class)).isInstanceOf(CapturingTaskScheduler.class);
            assertThat(ctx.getBeansOfType(ThreadPoolTaskScheduler.class)).isEmpty();
        });
    }

    @Configuration
    static class CustomAuditConfig {
        @Bean
        BatchAuditService customAuditService() {
            return new CustomAuditService();
        }
    }

    static class CustomAuditService implements BatchAuditService {
        @Override
        public io.fireflysoftware.common.batch.audit.BatchAuditRecord start(String jobName) {
            return new io.fireflysoftware.common.batch.audit.BatchAuditRecord(jobName);
        }
        @Override
        public void success(io.fireflysoftware.common.batch.audit.BatchAuditRecord record) { }
        @Override
        public void fail(io.fireflysoftware.common.batch.audit.BatchAuditRecord record, Exception e) { }
    }

    @Configuration
    static class CustomSchedulerConfig {
        @Bean
        TaskScheduler taskScheduler() {
            return new CapturingTaskScheduler();
        }
    }
}