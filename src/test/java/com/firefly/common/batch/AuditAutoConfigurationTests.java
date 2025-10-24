package com.firefly.common.batch;

import com.firefly.common.batch.audit.BatchAuditService;
import com.firefly.common.batch.audit.NoOpBatchAuditService;
import com.firefly.common.batch.audit.R2dbcBatchAuditService;
import com.firefly.common.batch.autoconfig.BatchAutoConfiguration;
import com.firefly.common.batch.autoconfig.FlywayBatchAuditAutoConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class AuditAutoConfigurationTests {

    private final ApplicationContextRunner baseRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class, FlywayBatchAuditAutoConfiguration.class));

    @Test
    void defaultModeIsNoopEvenIfConnectionFactoryExists() {
        baseRunner.withUserConfiguration(CFConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(BatchAuditService.class);
            assertThat(ctx.getBean(BatchAuditService.class)).isInstanceOf(NoOpBatchAuditService.class);
        });
    }

    @Test
    void r2dbcModeCreatesR2dbcAuditServiceWhenConnectionFactoryPresent() {
        baseRunner.withUserConfiguration(CFConfig.class)
                .withPropertyValues("firefly.batch.audit.mode=r2dbc")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(BatchAuditService.class);
                    assertThat(ctx.getBean(BatchAuditService.class)).isInstanceOf(R2dbcBatchAuditService.class);
                });
    }

    @Test
    void r2dbcModeWithoutConnectionFactoryFallsBackToNoop() {
        baseRunner.withPropertyValues("firefly.batch.audit.mode=r2dbc").run(ctx -> {
            assertThat(ctx).hasSingleBean(BatchAuditService.class);
            assertThat(ctx.getBean(BatchAuditService.class)).isInstanceOf(NoOpBatchAuditService.class);
        });
    }

    @Test
    void flywayCustomizerIsRegisteredWhenCreateTableEnabled() {
        baseRunner.withPropertyValues("firefly.batch.audit.create-table=true").run(ctx -> {
            assertThat(ctx.getBeansOfType(FlywayConfigurationCustomizer.class)).isNotEmpty();
            FlywayConfigurationCustomizer customizer = ctx.getBean(FlywayConfigurationCustomizer.class);
            // Smoke test: can call customize without exceptions with a bare FluentConfiguration
            customizer.customize(new FluentConfiguration());
        });
    }

    @Test
    void flywayCustomizerIsNotRegisteredByDefault() {
        baseRunner.run(ctx -> {
            assertThat(ctx.getBeansOfType(FlywayConfigurationCustomizer.class)).isEmpty();
        });
    }

    @Configuration
    static class CFConfig {
        @Bean
        ConnectionFactory connectionFactory() {
            return new DummyConnectionFactory();
        }
    }

    static class DummyConnectionFactory implements ConnectionFactory {
        @Override
        public reactor.core.publisher.Mono<io.r2dbc.spi.Connection> create() {
            return Mono.error(new UnsupportedOperationException("Not used in tests"));
        }
        @Override
        public ConnectionFactoryMetadata getMetadata() { return () -> "H2"; }
    }
}
