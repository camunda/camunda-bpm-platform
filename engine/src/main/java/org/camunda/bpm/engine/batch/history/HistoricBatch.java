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
package org.camunda.bpm.engine.batch.history;

import java.util.Date;

/**
 * Historic representation of a {@link org.camunda.bpm.engine.batch.Batch}.
 */
public interface HistoricBatch {

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
   * @return the date the batch was started
   */
  Date getStartTime();

  /**
   * @return the date the batch was completed
   */
  Date getEndTime();

}
