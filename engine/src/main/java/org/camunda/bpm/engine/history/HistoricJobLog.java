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
package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.runtime.Job;

/**
 * <p>The {@link HistoricJobLog} is used to have a log containing
 * information about {@link Job job} execution. The log provides
 * details about the complete lifecycle of a {@link Job job}:</p>
 * <ul>
 *   <li>job created</li>
 *   <li>job execution failed</li>
 *   <li>job execution successful</li>
 *   <li>job was deleted</li>
 * </ul>
 *
 * An instance of {@link HistoricJobLog} represents a state in
 * the lifecycle of a {@link Job job}.
 *
 * @author Roman Smirnov
 *
 * @since 7.3
 */
public interface HistoricJobLog {

  /**
   * Returns the unique identifier for <code>this</code> historic job log.
   */
  String getId();

  /**
   * Returns the time when <code>this</code> log occurred.
   */
  Date getTimestamp();

  /**
   * Returns the id of the associated job.
   */
  String getJobId();

  /**
   * Returns the due date of the associated job when <code>this</code> log occurred.
   */
  Date getJobDueDate();

  /**
   * Returns the retries of the associated job before the associated job has
   * been executed and when <code>this</code> log occurred.
   */
  int getJobRetries();

  /**
   * Returns the priority of the associated job when <code>this</code> log entry was created.
   *
   * @since 7.4
   */
  long getJobPriority();

  /**
   * Returns the message of the exception that occurred by executing the associated job.
   *
   * To get the full exception stacktrace,
   * use {@link HistoryService#getHistoricJobLogExceptionStacktrace(String)}
   */
  String getJobExceptionMessage();

  /**
   * Returns the id of the job definition on which the associated job was created.
   */
  String getJobDefinitionId();

  /**
   * Returns the job definition type of the associated job.
   */
  String getJobDefinitionType();

  /**
   * Returns the job definition configuration type of the associated job.
   */
  String getJobDefinitionConfiguration();

  /**
   * Returns the id of the activity on which the associated job was created.
   */
  String getActivityId();

  /**
   * Returns the specific execution id on which the associated job was created.
   */
  String getExecutionId();

  /**
   * Returns the specific process instance id on which the associated job was created.
   */
  String getProcessInstanceId();

  /**
   * Returns the specific process definition id on which the associated job was created.
   */
  String getProcessDefinitionId();

  /**
   * Returns the specific process definition key on which the associated job was created.
   */
  String getProcessDefinitionKey();

  /**
   * Returns the specific deployment id on which the associated job was created.
   */
  String getDeploymentId();

  /**
   * Returns the id of the tenant this job log entry belongs to. Can be <code>null</code>
   * if the job log entry belongs to no single tenant.
   */
  public String getTenantId();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the creation of the associated job.
   */
  boolean isCreationLog();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the failed execution of the associated job.
   */
  boolean isFailureLog();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the successful execution of the associated job.
   */
  boolean isSuccessLog();

  /**
   * Returns <code>true</code> when <code>this</code> log represents
   * the deletion of the associated job.
   */
  boolean isDeletionLog();

}
