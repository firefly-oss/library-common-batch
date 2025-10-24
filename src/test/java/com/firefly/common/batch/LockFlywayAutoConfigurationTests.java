package com.firefly.common.batch;

import com.firefly.common.batch.autoconfig.BatchAutoConfiguration;
import com.firefly.common.batch.autoconfig.FlywayShedLockAutoConfiguration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LockFlywayAutoConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BatchAutoConfiguration.class, FlywayShedLockAutoConfiguration.class));

    @Test
    void lockFlywayCustomizerIsRegisteredWhenCreateTableEnabled() {
        runner.withPropertyValues("firefly.batch.lock.create-table=true")
                .run(ctx -> {
                    assertThat(ctx.getBeansOfType(FlywayConfigurationCustomizer.class)).isNotEmpty();
                    FlywayConfigurationCustomizer customizer = ctx.getBean(FlywayConfigurationCustomizer.class);
                    // Smoke test: can call customize without exceptions with a bare FluentConfiguration
                    customizer.customize(new FluentConfiguration());
                });
    }

    @Test
    void lockFlywayCustomizerIsNotRegisteredByDefault() {
        runner.run(ctx -> {
            assertThat(ctx.getBeansOfType(FlywayConfigurationCustomizer.class)).isEmpty();
        });
    }
}
