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

import io.fireflysoftware.common.batch.audit.BatchAuditRecord;
import io.fireflysoftware.common.batch.audit.BatchAuditService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test implementation to capture auditing lifecycle for assertions.
 */
public class TestBatchAuditService implements BatchAuditService {

    public static class Event {
        public final String type; // start|success|fail
        public final String jobName;
        public final String recordId;
        public final String errorMessage;
        Event(String type, String jobName, String recordId, String errorMessage) {
            this.type = type; this.jobName = jobName; this.recordId = recordId; this.errorMessage = errorMessage;
        }
    }

    private final List<Event> events = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger starts = new AtomicInteger();
    private final AtomicInteger successes = new AtomicInteger();
    private final AtomicInteger failures = new AtomicInteger();

    @Override
    public BatchAuditRecord start(String jobName) {
        BatchAuditRecord record = new BatchAuditRecord(jobName);
        events.add(new Event("start", jobName, record.getId(), null));
        starts.incrementAndGet();
        return record;
    }

    @Override
    public void success(BatchAuditRecord record) {
        record.markSuccess();
        events.add(new Event("success", record.getJobName(), record.getId(), null));
        successes.incrementAndGet();
    }

    @Override
    public void fail(BatchAuditRecord record, Exception e) {
        record.markFailed(e);
        events.add(new Event("fail", record.getJobName(), record.getId(), e != null ? e.getMessage() : null));
        failures.incrementAndGet();
    }

    public List<Event> getEvents() { return events; }
    public int getStartCount() { return starts.get(); }
    public int getSuccessCount() { return successes.get(); }
    public int getFailCount() { return failures.get(); }
}