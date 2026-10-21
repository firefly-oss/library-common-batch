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

import io.fireflysoftware.common.batch.autoconfig.BatchAutoConfiguration;
import io.fireflysoftware.common.batch.autoconfig.FlywayShedLockAutoConfiguration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
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
