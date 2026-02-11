-- Library-provided migration to create the audit table used by library-common-batch
-- Apply only if you enable `firefly.batch.audit.create-table=true`

CREATE TABLE IF NOT EXISTS batch_audit (
    id            VARCHAR(64)  PRIMARY KEY,
    job_name      VARCHAR(255) NOT NULL,
    start_time    TIMESTAMP    NOT NULL,
    end_time      TIMESTAMP    NULL,
    status        VARCHAR(16)  NOT NULL,
    error_message TEXT         NULL
);
