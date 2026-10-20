package io.fireflysoftware.common.batch.autoconfig;

import io.fireflysoftware.common.batch.config.BatchProperties;
import net.javacrumbs.shedlock.core.LockProvider;
import io.r2dbc.spi.ConnectionFactory;
import net.javacrumbs.shedlock.provider.r2dbc.R2dbcLockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that provides a default ShedLock {@link LockProvider}
 * backed by R2DBC so that applications do not need to define it explicitly.
 *
 * It becomes active when:
 * - firefly.batch.lock.enabled=true (default)
 * - a {@link ConnectionFactory} bean exists
 * - no other {@link LockProvider} bean is defined
 * - ShedLock R2DBC provider is on the classpath
 */
@AutoConfiguration
@EnableConfigurationProperties(BatchProperties.class)
public class R2dbcLockProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(R2dbcLockProviderAutoConfiguration.class);

    /**
     * Exposes a default R2dbcLockProvider bean backed by the provided ConnectionFactory.
     * Applications can override it by declaring their own LockProvider bean.
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnProperty(prefix = "firefly.batch.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LockProvider defaultR2dbcLockProvider(ConnectionFactory connectionFactory) {
        // Uses default table name "shedlock" matching the library-provided Flyway migration
        log.info("Initialized default R2dbcLockProvider using ConnectionFactory {}", connectionFactory.getClass().getSimpleName());
        return new R2dbcLockProvider(connectionFactory);
    }
}
