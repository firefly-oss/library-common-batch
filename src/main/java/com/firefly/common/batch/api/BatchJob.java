package com.firefly.common.batch.api;

/**
 * Base interface for batch jobs to be scheduled by the library.
 */
public interface BatchJob {
    /**
     * A logical name for the job (used for auditing and logs).
     */
    String getName();

    /**
     * Job execution logic. Any thrown exception will be captured and audited as failure.
     */
    void execute();
}
