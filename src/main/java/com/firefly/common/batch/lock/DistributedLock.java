package com.firefly.common.batch.lock;

/**
 * Represents an acquired distributed lock. Implementations must ensure that calling
 * {@link #unlock()} releases the lock in the underlying storage.
 */
public interface DistributedLock extends AutoCloseable {
    /**
     * Releases the lock.
     */
    void unlock();

    @Override
    default void close() {
        unlock();
    }
}
