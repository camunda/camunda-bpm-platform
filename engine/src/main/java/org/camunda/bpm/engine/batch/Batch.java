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

/**
 * <p>A batch represents a number of jobs which
 * execute a number of commands asynchronously.
 *
 * <p>Batches have three types of jobs:
 * <ul>
 *   <li>Seed jobs: Create execution jobs
 *   <li>Execution jobs: Execute the actual action
 *   <li>Monitor jobs: Manage the batch once all execution jobs have been created
 *     (e.g. responsible for deletion of the Batch after completion).
 * </ul>
 *
 * <p>All three job types have independent job definitions. They can be controlled individually
 * (e.g. suspension) and are independently represented in the historic job log.
 */
public interface Batch {

  String TYPE_PROCESS_INSTANCE_MIGRATION = "instance-migration";

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

}
