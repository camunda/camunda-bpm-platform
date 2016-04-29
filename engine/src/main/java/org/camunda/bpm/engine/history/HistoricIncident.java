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

import org.camunda.bpm.engine.runtime.Incident;

/**
 * Represents a historic {@link Incident incident} that is stored permanently.
 *
 * @author Roman Smirnov
 *
 */
public interface HistoricIncident {

  /**
   * Returns the unique identifier for this incident.
   */
  public String getId();

  /**
   * Time when the incident happened.
   */
  public Date getCreateTime();

  /**
   * Time when the incident has been resolved or deleted.
   */
  public Date getEndTime();

  /**
   * Returns the type of this incident to identify the
   * kind of incident.
   *
   * <p>
   *
   * For example: <code>failedJobs</code> will be returned
   * in the case of an incident, which identify failed job
   * during the execution of a process instance.
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
   * Returns the key of the process definition of this
   * process instance on which the incident has happened.
   */
  public String getProcessDefinitionKey();

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
   * Returns <code>true</code>, iff the corresponding incident
   * has not been deleted or resolved.
   */
  public boolean isOpen();

  /**
   * Returns <code>true</code>, iff the corresponding incident
   * has been <strong>deleted</strong>.
   */
  public boolean isDeleted();

  /**
  * Returns <code>true</code>, iff the corresponding incident
  * has been <strong>resolved</strong>.
  */
  public boolean isResolved();

  /**
   * Returns the id of the tenant this incident belongs to. Can be <code>null</code>
   * if the incident belongs to no single tenant.
   */
  public String getTenantId();

  /**
   * Returns the id of the job definition the incident belongs to. Can be <code>null</code>
   * if the incident belongs to no job definition.
   */
  String getJobDefinitionId();

}
