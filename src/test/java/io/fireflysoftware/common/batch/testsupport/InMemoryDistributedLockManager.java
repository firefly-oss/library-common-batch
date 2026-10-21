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


package io.fireflysoftware.common.batch.testsupport;

import io.fireflysoftware.common.batch.lock.DistributedLock;
import io.fireflysoftware.common.batch.lock.DistributedLockManager;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple in-memory lock manager for tests, providing non-reentrant, non-blocking locks.
 */
public class InMemoryDistributedLockManager implements DistributedLockManager {

    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    public Optional<DistributedLock> tryLock(String key, Duration lockAtMostFor, Duration lockAtLeastFor) {
        Object marker = new Object();
        Object existing = locks.putIfAbsent(key, marker);
        if (existing != null) {
            return Optional.empty();
        }
        return Optional.of(() -> locks.remove(key, marker));
    }
}
