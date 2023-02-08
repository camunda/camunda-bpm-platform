/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.management;

import java.util.Date;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;

/**
 * Fluent builder to update the number of retries for one or multiple jobs asynchronously.
 */
public interface SetJobRetriesAsyncBuilder {

  /**
   * Specifies a due date to be set on the referenced {@link Job jobs}.
   *
   * When the number of retries of a job are incremented it is not automatically scheduled for immediate execution.
   * When a {@link Job} is executed is determined by the due date. By setting the due date together with the job retries, the scheduled execution date of the
   * job can be adjusted.
   *
   * @param dueDate The new due date for the updated jobs. If it is null, the due date will be set to null. If
   * {@link ProcessEngineConfiguration#isEnsureJobDueDateNotNull() ensureJobDueDateNotNull} is true, the due date will be set to the current date instead of null.
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder dueDate(Date dueDate);

  /**
   * Closes the fluent builder and creates a Batch that sets the job retries and executes any additional instructions.
   *
   * @throws ProcessEngineException Check the builder methods and their linked counterparts in {@link ManagementService} for more detail
   */
  Batch executeAsync();
}
