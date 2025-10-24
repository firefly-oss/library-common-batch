package com.firefly.common.batch.autoconfig;

import com.firefly.common.batch.api.BatchJob;
import com.firefly.common.batch.api.ScheduledBatch;
import com.firefly.common.batch.config.BatchProperties;
import com.firefly.common.batch.scheduling.BatchSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Map;

/**
 * Scans for beans implementing BatchJob and annotated with @ScheduledBatch,
 * registering them with the BatchSchedulerService when the context is ready.
 */
public class ScheduledBatchRegistrar implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ScheduledBatchRegistrar.class);

    private final ApplicationContext applicationContext;
    private final BatchSchedulerService schedulerService;
    private final BatchProperties properties;

    /**
     * Creates a registrar that will discover and register BatchJob beans after the context is ready.
     */
    public ScheduledBatchRegistrar(ApplicationContext applicationContext, BatchSchedulerService schedulerService, BatchProperties properties) {
        this.applicationContext = applicationContext;
        this.schedulerService = schedulerService;
        this.properties = properties;
    }

    /**
     * Lifecycle callback invoked after all singletons are created; performs job discovery and registration.
     */
    @Override
    public void afterSingletonsInstantiated() {
        if (!properties.isEnabled() || !properties.getScheduler().isEnabled()) {
            log.info("Batch scheduling is disabled by configuration (firefly.batch.enabled={}, firefly.batch.scheduler.enabled={}). Skipping job registration.",
                    properties.isEnabled(), properties.getScheduler().isEnabled());
            return;
        }
        Map<String, BatchJob> jobs = applicationContext.getBeansOfType(BatchJob.class);
        if (jobs.isEmpty()) {
            log.info("No BatchJob beans found to register.");
            return;
        }
        jobs.forEach((beanName, job) -> {
            Class<?> targetClass = AopUtils.getTargetClass(job);
            ScheduledBatch meta = AnnotatedElementUtils.findMergedAnnotation(targetClass, ScheduledBatch.class);
            if (meta != null) {
                schedulerService.registerJob(job, meta);
            } else {
                log.debug("Bean '{}' of type {} is a BatchJob but not annotated with @ScheduledBatch; skipping.", beanName, targetClass.getName());
            }
        });
    }
}
