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


package io.fireflysoftware.common.batch.api;

/**
 * Base interface for batch jobs to be scheduled by the library.
 */
public interface BatchJob {
    /**
     * A logical name for the job (used for auditing and logs).
     */
    String getName();

    /**
     * Job execution logic. Any thrown exception will be captured and audited as failure.
     */
    void execute();
}
