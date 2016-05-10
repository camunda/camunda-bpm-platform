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

/**
 * An {@link Incident} represents a failure in the execution of
 * a process instance.
 *
 * <p>
 *
 * A possible failure could be for example a failed {@link Job}
 * during the execution, so that the job retry is equal zero
 * (<code>job.retries == 0</code>). In that case an incident
 * will be created an the <code>incidentType</code> will be set
 * to <code>failedJobs</code>.
 *
 * <p>
 *
 * Furthermore, it is possible to create custom incidents with
 * an individually <code>incidentType</code> to indicate a failure
 * in the execution.
 *
 *
 * @author roman.smirnov
 *
 */
public interface Incident {

  /**
   * Handler type for incidents created on job execution failure
   */
  public static final String FAILED_JOB_HANDLER_TYPE = "failedJob";

  /**
   * Handler type for incidents created on external task failure
   */
  public static final String EXTERNAL_TASK_HANDLER_TYPE = "failedExternalTask";

  /**
   * Returns the unique identifier for this incident.
   */
  public String getId();

  /**
   * Time when the incident happened.
   */
  public Date getIncidentTimestamp();

  /**
   * Returns the type of this incident to identify the
   * kind of incident.
   *
   * <p>
   *
   * For example: <code>failedJobs</code> will be returned
   * in the case of an incident, which identify failed job
   * during the execution of a process instance.
   *
   * @see Incident#FAILED_JOB_HANDLER_TYPE
   * @see Incident#EXTERNAL_TASK_HANDLER_TYPE
   */
  public String getIncidentType();

  /**
   * Returns the incident message.
   */
  public String getIncidentMessage();

  /**
   * Returns the specific execution on which this
   * incident has happened.
   */
  public String getExecutionId();

  /**
   * Returns the id of the activity of the process instance
   * on which this incident has happened.
   */
  public String getActivityId();

  /**
   * Returns the specific process instance on which this
   * incident has happened.
   */
  public String getProcessInstanceId();

  /**
   * Returns the id of the process definition of this
   * process instance on which the incident has happened.
   */
  public String getProcessDefinitionId();

  /**
   * Returns the id of the incident on which this incident
   * has been triggered.
   */
  public String getCauseIncidentId();

  /**
   * Returns the id of the root incident on which
   * this transitive incident has been triggered.
   */
  public String getRootCauseIncidentId();

  /**
   * Returns the payload of this incident.
   */
  public String getConfiguration();

  /**
   * Returns the id of the tenant this incident belongs to. Can be <code>null</code>
   * if the incident belongs to no single tenant.
   */
  String getTenantId();

  /**
   * Returns the id of the job definition the incident belongs to. Can be <code>null</code>
   * if the incident belongs to no job definition.
   */
  String getJobDefinitionId();

}
