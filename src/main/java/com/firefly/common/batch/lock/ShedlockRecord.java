package com.firefly.common.batch.lock;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Entity mapped to the default ShedLock table used for distributed locks.
 */
@Table("shedlock")
class ShedlockRecord {
    @Id
    private final String name;
    @Column("lock_until")
    private Instant lockUntil;
    @Column("locked_at")
    private Instant lockedAt;
    @Column("locked_by")
    private String lockedBy;

    ShedlockRecord(String name, Instant lockUntil, Instant lockedAt, String lockedBy) {
        this.name = name;
        this.lockUntil = lockUntil;
        this.lockedAt = lockedAt;
        this.lockedBy = lockedBy;
    }

    public String getName() { return name; }
    public Instant getLockUntil() { return lockUntil; }
    public Instant getLockedAt() { return lockedAt; }
    public String getLockedBy() { return lockedBy; }

    public void setLockUntil(Instant lockUntil) { this.lockUntil = lockUntil; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
}
