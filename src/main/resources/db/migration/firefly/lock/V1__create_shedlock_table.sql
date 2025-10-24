-- Library-provided migration to create the ShedLock table used for distributed locks
-- Apply only if you enable `firefly.batch.lock.create-table=true`

CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP    NULL,
    locked_at  TIMESTAMP    NULL,
    locked_by  VARCHAR(255) NULL
);
