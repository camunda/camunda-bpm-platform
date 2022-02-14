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
  String getId();

  /**
   * Time when the incident happened.
   */
  Date getCreateTime();

  /**
   * Time when the incident has been resolved or deleted.
   */
  Date getEndTime();

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
  String getIncidentType();

  /**
   * Returns the incident message.
   */
  String getIncidentMessage();

  /**
   * Returns the specific execution on which this
   * incident has happened.
   */
  String getExecutionId();

  /**
   * Returns the id of the activity of the process instance
   * on which this incident has happened.
   */
  String getActivityId();

  /**
   * Returns the specific root process instance id of the process instance
   * on which this incident has happened.
   */
  String getRootProcessInstanceId();

  /**
   * Returns the specific process instance on which this
   * incident has happened.
   */
  String getProcessInstanceId();

  /**
   * Returns the id of the process definition of this
   * process instance on which the incident has happened.
   */
  String getProcessDefinitionId();

  /**
   * Returns the key of the process definition of this
   * process instance on which the incident has happened.
   */
  String getProcessDefinitionKey();

  /**
   * Returns the id of the incident on which this incident
   * has been triggered.
   */
  String getCauseIncidentId();

  /**
   * Returns the id of the root incident on which
   * this transitive incident has been triggered.
   */
  String getRootCauseIncidentId();

  /**
   * Returns the payload of this incident.
   */
  String getConfiguration();

  /**
   * Returns the history payload of this incident.
   */
  String getHistoryConfiguration();

  /**
   * Returns <code>true</code>, iff the corresponding incident
   * has not been deleted or resolved.
   */
  boolean isOpen();

  /**
   * Returns <code>true</code>, iff the corresponding incident
   * has been <strong>deleted</strong>.
   */
  boolean isDeleted();

  /**
  * Returns <code>true</code>, iff the corresponding incident
  * has been <strong>resolved</strong>.
  */
  boolean isResolved();

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

  /** The time the historic incident will be removed. */
  Date getRemovalTime();

  /**
   * Returns the id of the activity on which the last exception occurred.
   */
  String getFailedActivityId();

  /**
   * Returns the annotation of this incident
   */
  String getAnnotation();
}
