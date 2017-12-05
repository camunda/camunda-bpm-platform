/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.batch;

import org.camunda.bpm.engine.ManagementService;

/**
 * <p>A batch represents a number of jobs which
 * execute a number of commands asynchronously.
 * <p>
 * <p>Batches have three types of jobs:
 * <ul>
 * <li>Seed jobs: Create execution jobs
 * <li>Execution jobs: Execute the actual action
 * <li>Monitor jobs: Manage the batch once all execution jobs have been created
 * (e.g. responsible for deletion of the Batch after completion).
 * </ul>
 * <p>
 * <p>All three job types have independent job definitions. They can be controlled individually
 * (e.g. suspension) and are independently represented in the historic job log.
 */
public interface Batch {

  String TYPE_PROCESS_INSTANCE_MIGRATION = "instance-migration";
  String TYPE_PROCESS_INSTANCE_MODIFICATION = "instance-modification";
  String TYPE_PROCESS_INSTANCE_RESTART = "instance-restart";
  String TYPE_PROCESS_INSTANCE_DELETION = "instance-deletion";
  String TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE = "instance-update-suspension-state";
  String TYPE_HISTORIC_PROCESS_INSTANCE_DELETION = "historic-instance-deletion";
  String TYPE_HISTORIC_DECISION_INSTANCE_DELETION = "historic-decision-instance-deletion";
  String TYPE_SET_JOB_RETRIES = "set-job-retries";
  String TYPE_SET_EXTERNAL_TASK_RETRIES = "set-external-task-retries";

  /**
   * @return the id of the batch
   */
  String getId();

  /**
   * @return the type of the batch
   */
  String getType();

  /**
   * @return the number of batch execution jobs required to complete the batch
   */
  int getTotalJobs();

  /**
   * @return the number of batch execution jobs already created by the seed job
   */
  int getJobsCreated();

  /**
   * @return number of batch jobs created per batch seed job invocation
   */
  int getBatchJobsPerSeed();

  /**
   * @return the number of invocations executed per batch job
   */
  int getInvocationsPerBatchJob();

  /**
   * @return the id of the batch seed job definition
   */
  String getSeedJobDefinitionId();

  /**
   * @return the id of the batch monitor job definition
   */
  String getMonitorJobDefinitionId();

  /**
   * @return the id of the batch job definition
   */
  String getBatchJobDefinitionId();

  /**
   * @return the batch's tenant id or null
   */
  String getTenantId();

  /**
   * <p>
   * Indicates whether this batch is suspended. If a batch is suspended,
   * the batch jobs will not be acquired by the job executor.
   * </p>
   * <p>
   * <p>
   * <strong>Note:</strong> It is still possible to manually suspend and activate
   * jobs and job definitions using the {@link ManagementService}, which will
   * not change the suspension state of the batch.
   * </p>
   *
   * @return true if this batch is currently suspended, false otherwise
   * @see ManagementService#suspendBatchById(String)
   * @see ManagementService#activateBatchById(String)
   */
  boolean isSuspended();

}
