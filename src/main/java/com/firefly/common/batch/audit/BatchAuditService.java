package com.firefly.common.batch.audit;

/**
 * SPI for auditing batch job executions. Implementations are expected to persist
 * audit records (e.g., to a database). A default no-op implementation is provided
 * so the library works out-of-the-box and can be overridden by applications.
 */
public interface BatchAuditService {

    /**
     * Called before executing the job. Returns a record instance that will be passed to
     * success/fail methods.
     */
    BatchAuditRecord start(String jobName);

    /**
     * Called when the job finishes successfully.
     */
    void success(BatchAuditRecord record);

    /**
     * Called when the job fails with an exception.
     */
    void fail(BatchAuditRecord record, Exception e);
}
