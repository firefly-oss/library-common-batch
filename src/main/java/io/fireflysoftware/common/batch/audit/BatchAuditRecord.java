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


package io.fireflysoftware.common.batch.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Simple audit record used by the audit service to track job executions.
 * Implementations may persist this to a database; this class is a simple POJO
 * to carry data across the scheduling lifecycle.
 */
@Table("batch_audit")
public class BatchAuditRecord {
    @Id
    private final String id;
    @Column("job_name")
    private final String jobName;
    @Column("start_time")
    private final Instant startTime;
    @Column("end_time")
    private Instant endTime;
    @Column("status")
    private Status status;
    @Column("error_message")
    private String errorMessage;
    @Transient
    private String errorStacktrace;

    public enum Status { STARTED, SUCCESS, FAILED }

    public BatchAuditRecord(String jobName) {
        this(UUID.randomUUID().toString(), jobName, Instant.now());
    }

    public BatchAuditRecord(String id, String jobName, Instant startTime) {
        this.id = id;
        this.jobName = jobName;
        this.startTime = startTime;
        this.status = Status.STARTED;
    }

    public String getId() { return id; }
    public String getJobName() { return jobName; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Status getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public String getErrorStacktrace() { return errorStacktrace; }

    public void markSuccess() {
        this.status = Status.SUCCESS;
        this.endTime = Instant.now();
    }

    public void markFailed(Throwable t) {
        this.status = Status.FAILED;
        this.endTime = Instant.now();
        if (t != null) {
            this.errorMessage = t.getMessage();
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement e : t.getStackTrace()) {
                sb.append(e.toString()).append("\n");
            }
            this.errorStacktrace = sb.toString();
        }
    }
}
