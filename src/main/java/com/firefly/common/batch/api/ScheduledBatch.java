package com.firefly.common.batch.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a BatchJob as scheduled with cron.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledBatch {
    /** Cron expression defining when the job should run. */
    String cron();

    /** Whether the job scheduling is enabled. */
    boolean enabled() default true;

    /** Optional human-readable description. */
    String description() default "";
}
