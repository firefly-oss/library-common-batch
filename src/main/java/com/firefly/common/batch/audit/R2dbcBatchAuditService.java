package com.firefly.common.batch.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * R2DBC-based implementation of BatchAuditService using a repository instead of
 * hand-written SQL.
 */
public class R2dbcBatchAuditService implements BatchAuditService {

    private static final Logger log = LoggerFactory.getLogger(R2dbcBatchAuditService.class);

    private final R2dbcBatchAuditRepository repository;

    public R2dbcBatchAuditService(R2dbcBatchAuditRepository repository) {
        this.repository = repository;
        log.info("Initialized R2dbcBatchAuditService with repository {}", repository.getClass().getSimpleName());
    }

    @Override
    public BatchAuditRecord start(String jobName) {
        BatchAuditRecord r = new BatchAuditRecord(jobName);
        repository.insert(r);
        log.debug("[AUDIT] start job='{}' id='{}' at={} status={}", jobName, r.getId(), r.getStartTime(), r.getStatus());
        return r;
    }

    @Override
    public void success(BatchAuditRecord record) {
        record.markSuccess();
        repository.update(record);
        log.debug("[AUDIT] success job='{}' id='{}' endTime={} status={}", record.getJobName(), record.getId(), record.getEndTime(), record.getStatus());
    }

    @Override
    public void fail(BatchAuditRecord record, Exception e) {
        record.markFailed(e);
        repository.update(record);
        log.warn("[AUDIT] fail job='{}' id='{}' endTime={} status={} error={}", record.getJobName(), record.getId(), record.getEndTime(), record.getStatus(), e.toString());
    }
}
