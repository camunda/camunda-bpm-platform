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
package org.camunda.bpm.engine.externaltask;

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Represents an external task that was fetched and locked.
 *
 * @author Thorben Lindhauer
 */
public interface LockedExternalTask {

  /**
   * @return the id of the task
   */
  String getId();

  /**
   * @return the name of the topic the task belongs to
   */
  String getTopicName();

  /**
   * @return the id of the worker that has locked the task
   */
  String getWorkerId();

  /**
   * @return the absolute time at which the lock expires
   */
  Date getLockExpirationTime();

  /**
   * @return the id of the process instance the task exists in
   */
  String getProcessInstanceId();

  /**
   * @return the id of the execution that the task is assigned to
   */
  String getExecutionId();

  /**
   * @return the id of the activity for which the task is created
   */
  String getActivityId();

  /**
   * @return the id of the activity instance in which context the task exists
   */
  String getActivityInstanceId();

  /**
   * @return the id of the process definition the tasks activity belongs to
   */
  String getProcessDefinitionId();

  /**
   * @return the key of the process definition the tasks activity belongs to
   */
  String getProcessDefinitionKey();

  /**
   * @return the version tag of the process definition the tasks activity belongs to
   */
  String getProcessDefinitionVersionTag();

  /**
   * @return the number of retries left. The number of retries is provided by
   *   a task client, therefore the initial value is <code>null</code>.
   */
  Integer getRetries();

  /**
   * @return the full error message submitted with the latest reported failure executing this task;
   *   <code>null</code> if no failure was reported previously or if no error message
   *   was submitted
   *
   * @see ExternalTaskService#handleFailure(String, String, String, int, long)
   */
  String getErrorMessage();

  /**
   * @return error details submitted with the latest reported failure executing this task;
   *   <code>null</code> if no failure was reported previously or if no error details
   *   was submitted
   *
   * @see ExternalTaskService#handleFailure(String, String, String, String, int, long)
   */
  String getErrorDetails();

  /**
   * @return a map of variables that contains an entry for every variable
   *   that was specified at fetching time, if such a variable exists in the tasks
   *   ancestor execution hierarchy.
   */
  VariableMap getVariables();

  /**
   * @return the id of the tenant the task belongs to. Can be <code>null</code>
   * if the task belongs to no single tenant.
   */
  String getTenantId();

  /**
   * Returns the priority of the locked external task.
   * The default priority is 0.
   * @return the priority of the external task
   */
  long getPriority();

  /**
   * Returns the business key of the process instance the external task belongs to
   *
   * @return the business key
   */
  String getBusinessKey();

  /**
   * Returns a map of custom extension properties if the fetch instructions
   * indicate to include extension properties.
   * 
   * If extension properties are included, the returned map contains any
   * extension property that is defined in the model definition of the external
   * task. If extension properties not included or no properties are defined for
   * the external task, the map will be empty.
   * 
   * @return a map with all defined custom extension properties, never
   *         <code>null</code>
   */
  Map<String, String> getExtensionProperties();

}
