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
package org.camunda.bpm.client.task;

import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.Date;
import java.util.Map;

/**
 * <p>Represents an external task</p>
 *
 * @author Tassilo Weidner
 */
public interface ExternalTask {

  /**
   * @return the id of the activity that this external task belongs to
   */
  String getActivityId();

  /**
   * @return the id of the activity instance that the external task belongs to
   */
  String getActivityInstanceId();

  /**
   * @return the error message that was supplied when the last failure of this task was reported
   */
  String getErrorMessage();

  /**
   * @return the error details submitted with the latest reported failure executing this task
   */
  String getErrorDetails();

  /**
   * @return the id of the execution that the external task belongs to
   */
  String getExecutionId();

  /**
   * @return the id of the external task
   */
  String getId();

  /**
   * @return the time that the task's most recent lock expires or has expired
   */
  Date getLockExpirationTime();

  /**
   * @return the time that the task was created.
   */
  Date getCreateTime();

  /**
   * @return the id of the process definition the external task is defined in
   */
  String getProcessDefinitionId();

  /**
   * @return the key of the process definition the external task is defined in
   */
  String getProcessDefinitionKey();

  /**
   * @return the version tag of the process definition the tasks activity belongs to
   */
  String getProcessDefinitionVersionTag();

  /**
   * @return the id of the process instance the external task belongs to
   */
  String getProcessInstanceId();

  /**
   * @return the number of retries the task currently has left
   */
  Integer getRetries();

  /**
   * @return the id of the worker that possesses or possessed the most recent lock
   */
  String getWorkerId();

  /**
   * @return the topic name of the external task
   */
  String getTopicName();

  /**
   * @return the id of the tenant the external task belongs to
   */
  String getTenantId();

  /**
   * @return the priority of the external task
   */
  long getPriority();

  /**
   * Returns an untyped variable of the task's ancestor execution hierarchy
   *
   * @param variableName of the variable to be returned
   * @param <T> the type of the variable
   * @throws ValueMapperException if an object cannot be deserialized
   *
   * @return
   * <ul>
   *   <li> an untyped variable if such a named variable exists
   *   <li> null if such a named variable not exists
   * </ul>
   */
  <T> T getVariable(String variableName);

  /**
   * Returns a typed variable of the task's ancestor execution hierarchy
   *
   * @param variableName of the variable to be returned
   * @param <T> the type of the variable
   * @throws ValueMapperException if an object cannot be deserialized
   *
   * @return
   * <ul>
   *   <li> a typed variable if such a named variable exists
   *   <li> null if such a named variable not exists
   * </ul>
   */
  <T extends TypedValue> T getVariableTyped(String variableName);

  /**
   * Returns a typed variable of the task's ancestor execution hierarchy
   *
   * @param variableName of the variable to be returned
   * @param deserializeObjectValue
   * <ul>
   *   <li> {@code false} to retrieve the object without deserialization
   *   <li> {@code true} to retrieve the deserialized object
   * </ul>
   * @param <T> the type of the variable
   * @throws ValueMapperException if an object cannot be deserialized
   *
   * @return
   * <ul>
   *   <li> a typed variable if such a named variable exists
   *   <li> null if such a named variable not exists
   * </ul>
   */
  <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeObjectValue);

  /**
   * Returns untyped variables that exist in the task's ancestor execution hierarchy
   *
   * @throws ValueMapperException if an object cannot be deserialized
   * @return a map of untyped variables that contains an entry for each variable
   */
  Map<String, Object> getAllVariables();

  /**
   * Returns typed variables that exist in the task's ancestor execution hierarchy
   *
   * @throws ValueMapperException if an object cannot be deserialized
   * @return a map of typed variables that contains an entry for each variable
   */
  VariableMap getAllVariablesTyped();

  /**
   * Returns typed variables that exist in the task's ancestor execution hierarchy
   *
   * @throws ValueMapperException if an object cannot be deserialized
   * @param deserializeObjectValues
   * <ul>
   *   <li> {@code false} to retrieve the object without deserialization
   *   <li> {@code true} to retrieve the deserialized object
   * </ul>
   * @return a map of typed variables that contains an entry for each variable
   */
  VariableMap getAllVariablesTyped(boolean deserializeObjectValues);

  /**
   * Returns the business key of the process instance the external task is associated with
   *
   * @return the business key
   */
  String getBusinessKey();
  
  /**
   * Returns the value of the extension property for a given key or <code>null</code> if the property was not available.
   * 
   * @see ExternalTask#getExtensionProperties()
   * @return the extension property, or <code>null</code> if not available
   */
  String getExtensionProperty(String propertyKey);

  /**
   * Returns all available extension properties. Extension properties must be
   * defined at the external task activity inside the BPMN model and explicitly
   * fetched (e.g. by calling
   * {@link TopicSubscriptionBuilder#includeExtensionProperties(boolean)}) to be
   * available.
   * 
   * If no extension properties are available the returned map will be empty.
   * 
   * @return a map of available extension properties, never <code>null</code>
   */
  Map<String, String> getExtensionProperties();
  
}

