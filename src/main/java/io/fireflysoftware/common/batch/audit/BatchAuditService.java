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

/**
 * SPI for auditing batch job executions. Implementations are expected to persist
 * audit records (e.g., to a database). A default no-op implementation is provided
 * so the library works out-of-the-box and can be overridden by applications.
 */
public interface BatchAuditService {

    /**
     * Called before executing the job. Returns a record instance that will be passed to
     * success/fail methods.
     */
    BatchAuditRecord start(String jobName);

    /**
     * Called when the job finishes successfully.
     */
    void success(BatchAuditRecord record);

    /**
     * Called when the job fails with an exception.
     */
    void fail(BatchAuditRecord record, Exception e);
}
