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

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.query.Query;



/** Allows programmatic querying of {@link Execution}s.
 *
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public interface ExecutionQuery extends Query<ExecutionQuery, Execution>{

  /** Only select executions which have the given process definition key. **/
  ExecutionQuery processDefinitionKey(String processDefinitionKey);

  /** Only select executions which have the given process definition id. **/
  ExecutionQuery processDefinitionId(String processDefinitionId);

  /** Only select executions which have the given process instance id. **/
  ExecutionQuery processInstanceId(String processInstanceId);

  /** Only select executions that belong to a process instance with the given business key */
  ExecutionQuery processInstanceBusinessKey(String processInstanceBusinessKey);

  /** Only select executions with the given id. **/
  ExecutionQuery executionId(String executionId);

  /** Only select executions which contain an activity with the given id. **/
  ExecutionQuery activityId(String activityId);

  /**
   * Only select executions which have a local variable with the given value. The type
   * of variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfiguration#getVariableSerializers()}.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ExecutionQuery variableValueEquals(String name, Object value);

  /**
   * Only select executions which have a local variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ExecutionQuery variableValueNotEquals(String name, Object value);


  /**
   * Only select executions which have a local variable value greater than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueGreaterThan(String name, Object value);

  /**
   * Only select executions which have a local variable value greater than or equal to
   * the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select executions which have a local variable value less than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueLessThan(String name, Object value);

  /**
   * Only select executions which have a local variable value less than or equal to the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select executions which have a local variable value like the given value.
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy:
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  ExecutionQuery variableValueLike(String name, String value);

  /**
   * Only select executions which are part of a process that have a variable
   * with the given name set to the given value.
   */
  ExecutionQuery processVariableValueEquals(String variableName, Object variableValue);

  /**
   * Only select executions which are part of a process that have a variable  with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  ExecutionQuery processVariableValueNotEquals(String variableName, Object variableValue);

  // event subscriptions //////////////////////////////////////////////////

  /**
   * @see #signalEventSubscriptionName(String)
   */
  @Deprecated
  ExecutionQuery signalEventSubscription(String signalName);

  /**
   * Only select executions which have a signal event subscription
   * for the given signal name.
   *
   * (The signalName is specified using the 'name' attribute of the signal element
   * in the BPMN 2.0 XML.)
   *
   * @param signalName the name of the signal the execution has subscribed to
   */
  ExecutionQuery signalEventSubscriptionName(String signalName);

  /**
   * Only select executions which have a message event subscription
   * for the given messageName.
   *
   * (The messageName is specified using the 'name' attribute of the message element
   * in the BPMN 2.0 XML.)
   *
   * @param messageName the name of the message the execution has subscribed to
   */
  ExecutionQuery messageEventSubscriptionName(String messageName);

  /**
   * Only select executions that have a message event subscription.
   * Use {@link #messageEventSubscriptionName(String)} to filter for executions
   * with message event subscriptions with a certain name.
   */
  ExecutionQuery messageEventSubscription();

  /**
   * Only selects executions which are suspended, because their process instance is suspended.
   */
  ExecutionQuery suspended();

  /**
   * Only selects executions which are active (i.e. not suspended).
   */
  ExecutionQuery active();

  /**
   * Only selects executions with the given incident type.
   */
  ExecutionQuery incidentType(String incidentType);

  /**
   * Only selects executions with the given incident id.
   */
  ExecutionQuery incidentId(String incidentId);

  /**
   * Only selects executions with the given incident message.
   */
  ExecutionQuery incidentMessage(String incidentMessage);

  /**
   * Only selects executions with an incident message like the given.
   */
  ExecutionQuery incidentMessageLike(String incidentMessageLike);

   /** Only selects executions with one of the given tenant ids. */
  ExecutionQuery tenantIdIn(String... tenantIds);

  /** Only selects executions which have no tenant id. */
  ExecutionQuery withoutTenantId();

  //ordering //////////////////////////////////////////////////////////////

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessInstanceId();

  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessDefinitionKey();

  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessDefinitionId();

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of executions without tenant id is database-specific. */
  ExecutionQuery orderByTenantId();

}
