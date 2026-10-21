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

import java.time.Duration;
import java.util.Optional;

/**
 * SPI for providing a distributed lock implementation (e.g., DB-backed).
 *
 * Applications can provide a bean of this type to enable cluster-wide single execution
 * of jobs. If no bean is present, jobs will execute without distributed locking.
 */
public interface DistributedLockManager {

    /**
     * Attempts to acquire a lock for the given key.
     *
     * @param key            lock key (should be unique per job)
     * @param lockAtMostFor  maximum duration to keep the lock in case of a crash
     * @param lockAtLeastFor minimum duration to keep the lock (0 for none)
     * @return Optional containing the acquired lock; empty if lock could not be acquired
     */
    Optional<DistributedLock> tryLock(String key, Duration lockAtMostFor, Duration lockAtLeastFor);
}
