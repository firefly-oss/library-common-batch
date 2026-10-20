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
