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

/**
 * Represents an instance of an external task that is created when
 * a service-task like activity (i.e. service task, send task, ...) with
 * attribute <code>camunda:type="external"</code> is executed.
 *
 * @author Thorben Lindhauer
 */
public interface ExternalTask {

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
   * @return short error message submitted with the latest reported failure executing this task;
   *   <code>null</code> if no failure was reported previously or if no error message
   *   was submitted
   *
   * @see ExternalTaskService#handleFailure(String, String,String, String, int, long)
   *
   * To get the full error details,
   * use {@link ExternalTaskService#getExternalTaskErrorDetails(String)}
   */
  String getErrorMessage();

  /**
   * @return true if the external task is suspended; a suspended external task
   * cannot be completed, thereby preventing process continuation
   */
  boolean isSuspended();

  /**
   * @return the id of the tenant the task belongs to. Can be <code>null</code>
   * if the task belongs to no single tenant.
   */
  String getTenantId();

  /**
   * Returns the priority of the external task.
   *
   * @return the priority of the external task
   */
  long getPriority();

  /**
   * Returns a map containing all custom extension properties of the external task.
   * 
   * @return the properties, never <code>null</code>
   */
  Map<String, String> getExtensionProperties();

  /**
   * Returns the business key of the process instance the external task belongs to
   *
   * @return the business key
   */
  String getBusinessKey();

}
