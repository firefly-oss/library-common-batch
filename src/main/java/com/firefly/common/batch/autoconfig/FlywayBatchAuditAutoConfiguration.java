package com.firefly.common.batch.autoconfig;

import com.firefly.common.batch.config.BatchProperties;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds the library's Flyway migration location when audit table creation is enabled by properties.
 */
@AutoConfiguration
@EnableConfigurationProperties(BatchProperties.class)
@ConditionalOnClass({FluentConfiguration.class, FlywayConfigurationCustomizer.class})
public class FlywayBatchAuditAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FlywayBatchAuditAutoConfiguration.class);

    private static final String LIB_LOCATION = "classpath:db/migration/firefly/batch";

    @Bean
    @ConditionalOnProperty(prefix = "firefly.batch.audit", name = "create-table", havingValue = "true")
    public FlywayConfigurationCustomizer fireflyBatchFlywayCustomizer() {
        return (FluentConfiguration cfg) -> {
            // Append our location without losing user-defined ones
            var existing = cfg.getLocations();
            List<String> locations = new ArrayList<>();
            if (existing != null && existing.length > 0) {
                locations.addAll(Arrays.stream(existing).map(Object::toString).toList());
            }
            if (!locations.contains(LIB_LOCATION)) {
                locations.add(LIB_LOCATION);
            }
            cfg.locations(locations.toArray(String[]::new));
            log.info("Added Firefly batch Flyway location. Effective locations={}", locations);
        };
    }
}
