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
package org.camunda.bpm.engine.delegate;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * Execution used in {@link JavaDelegate}s and {@link ExecutionListener}s.
 *
 * @author Tom Baeyens
 */
public interface DelegateExecution extends BaseDelegateExecution, BpmnModelExecutionContext, ProcessEngineServicesAware {

  /** Reference to the overall process instance */
  String getProcessInstanceId();

  /**
   * The business key for the process instance this execution is associated
   * with.
   */
  String getProcessBusinessKey();

  /**
   * Configure a business key on the process instance this execution is associated
   * with.
   *
   * @param businessKey the new business key
   */
  void setProcessBusinessKey(String businessKey);

  /**
   * The process definition key for the process instance this execution is
   * associated with.
   */
  String getProcessDefinitionId();

  /**
   * Gets the id of the parent of this execution. If null, the execution
   * represents a process-instance.
   */
  String getParentId();

  /**
   * Gets the id of the current activity.
   */
  String getCurrentActivityId();

  /**
   * Gets the name of the current activity.
   */
  String getCurrentActivityName();

  /**
   * return the Id of the activity instance currently executed by this execution
   */
  String getActivityInstanceId();

  /**
   * return the Id of the parent activity instance currently executed by this
   * execution
   */
  String getParentActivityInstanceId();

  /** return the Id of the current transition */
  String getCurrentTransitionId();

  /**
   * Return the process instance execution for this execution. In case this
   * execution is the process instance execution the method returns itself.
   */
  DelegateExecution getProcessInstance();

  /**
   * In case this delegate execution is the process instance execution
   * and this process instance was started by a call activity, this method
   * returns the execution which executed the call activity in the super process instance.
   *
   * @return the super execution or null.
   */
  DelegateExecution getSuperExecution();

  /**
   * Returns whether this execution has been canceled.
   */
  boolean isCanceled();

  /**
   * Return the id of the tenant this execution belongs to. Can be <code>null</code>
   * if the execution belongs to no single tenant.
   */
  String getTenantId();

  /**
   * Method to store variable in a specific scope identified by activity ID.
   *
   * @param variableName - name of the variable
   * @param value - value of the variable
   * @param activityId - activity ID which is associated with destination execution,
   *                   if not existing - exception will be thrown
   * @throws ProcessEngineException if scope with specified activity ID is not found
   */
  void setVariable (String variableName, Object value, String activityId);

  /**
   * Create an incident associated with this execution
   *
   * @param incidentType the type of incident
   * @param configuration
   * @return a new incident
   */
  Incident createIncident(String incidentType, String configuration);

  /**
   * Create an incident associated with this execution
   *
   * @param incidentType the type of incident
   * @param configuration
   * @param message
   * @return a new incident
   */
  Incident createIncident(String incidentType, String configuration, String message);

  /**
   * Resolve and remove an incident with given id
   *
   * @param incidentId
   */
  void resolveIncident(String incidentId);
}
