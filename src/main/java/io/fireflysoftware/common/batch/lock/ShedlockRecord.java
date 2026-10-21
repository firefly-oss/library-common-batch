/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.common.batch.lock;

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
