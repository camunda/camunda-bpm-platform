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

package org.camunda.bpm.engine.runtime;

import java.util.Date;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.management.JobDefinition;


/**
 * Represents one job (timer, message, etc.).
 *
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public interface Job {

  /**
   * Returns the unique identifier for this job.
   */
  String getId();

  /**
   * Returns the date on which this job is supposed to be processed.
   */
  Date getDuedate();

  /**
   * Returns the id of the process instance which execution created the job.
   */
  String getProcessInstanceId();

  /**
   * Returns the id of the process definition which created the job.
   */
  String getProcessDefinitionId();

  /**
   * Returns the key of the process definition which created the job.
   */
  String getProcessDefinitionKey();

  /**
   * Returns the specific execution on which the job was created.
   */
  String getExecutionId();

  /**
   * Returns the number of retries this job has left.
   * Whenever the jobexecutor fails to execute the job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again
   * (ie a manual retry is required then).
   */
  int getRetries();

  /**
   * Returns the message of the exception that occurred, the last time the job was
   * executed. Returns null when no exception occurred.
   *
   * To get the full exception stacktrace,
   * use {@link ManagementService#getJobExceptionStacktrace(String)}
   */
  String getExceptionMessage();

  /**
   * Returns the id of the deployment in which context the job was created.
   */
  String getDeploymentId();

  /**
   * The id of the {@link JobDefinition} for this job.
   */
  String getJobDefinitionId();

  /**
   * Indicates whether this job is suspended. If a job is suspended,
   * the job will be not acquired by the job executor.
   *
   * @return true if this Job is currently suspended.
   */
  boolean isSuspended();

  /**
   * The job's priority that is a hint to job acquisition.
   *
   * @since 7.4
   */
  long getPriority();

  /**
   * The id of the tenant this job belongs to. Can be <code>null</code>
   * if the job belongs to no single tenant.
   */
  String getTenantId();

}
