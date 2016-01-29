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

/**
 * @author roman.smirnov
 */
public interface VariableInstanceQuery extends Query<VariableInstanceQuery, VariableInstance> {

  /** Only select the variable with the given Id
   * @param the id of the variable to select
   * @return the query object */
  VariableInstanceQuery variableId(String id);

  /** Only select variable instances which have the variable name. **/
  VariableInstanceQuery variableName(String variableName);

  /** Only select variable instances which have one of the variables names. **/
  VariableInstanceQuery variableNameIn(String... variableNames);

  /** Only select variable instances which have the name like the assigned variable name.
   * The string can include the wildcard character '%' to express like-strategy:
   * starts with (string%), ends with (%string) or contains (%string%).
   **/
  VariableInstanceQuery variableNameLike(String variableNameLike);

  /** Only select variable instances which have one of the executions ids. **/
  VariableInstanceQuery executionIdIn(String... executionIds);

  /** Only select variable instances which have one of the process instance ids. **/
  VariableInstanceQuery processInstanceIdIn(String... processInstanceIds);

  /** Only select variable instances which have one of the case execution ids. **/
  VariableInstanceQuery caseExecutionIdIn(String... caseExecutionIds);

  /** Only select variable instances which have one of the case instance ids. **/
  VariableInstanceQuery caseInstanceIdIn(String... caseInstanceIds);

  /** Only select variable instances which have one of the task ids. **/
  VariableInstanceQuery taskIdIn(String... taskIds);

  /** Only select variables instances which have on of the variable scope ids. **/
  VariableInstanceQuery variableScopeIdIn(String... variableScopeIds);

  /** Only select variable instances which have one of the activity instance ids. **/
  VariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds);

  /**
   * Only select variables instances which have the given name and value. The type
   * of variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfiguration#getVariableSerializers()}.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   * @param value variable value, can be null.
   */
  VariableInstanceQuery variableValueEquals(String name, Object value);

  /**
   * Only select variable instances which have the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   * @param value variable value, can be null.
   */
  VariableInstanceQuery variableValueNotEquals(String name, Object value);

  /**
   * Only select variable instances which value is greater than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  VariableInstanceQuery variableValueGreaterThan(String name, Object value);

  /**
   * Only select variable instances which value is greater than or equal to
   * the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  VariableInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select variable instances which value is less than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  VariableInstanceQuery variableValueLessThan(String name, Object value);

  /**
   * Only select variable instances which value is less than or equal to the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  VariableInstanceQuery variableValueLessThanOrEqual(String name, Object value);

  /**
   * Disable fetching of byte array and file values. By default, the query will fetch such values.
   * By calling this method you can prevent the values of (potentially large) blob data chunks
   * to be fetched. The variables themselves are nonetheless included in the query result.
   *
   * @return the query builder
   */
  VariableInstanceQuery disableBinaryFetching();

  /**
   * Disable deserialization of variable values that are custom objects. By default, the query
   * will attempt to deserialize the value of these variables. By calling this method you can
   * prevent such attempts in environments where their classes are not available.
   * Independent of this setting, variable serialized values are accessible.
   */
  VariableInstanceQuery disableCustomObjectDeserialization();

  /**
   * Only select variable instances which value is like the given value.
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy:
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  VariableInstanceQuery variableValueLike(String name, String value);

  /** Only select variable instances with one of the given tenant ids. */
  VariableInstanceQuery tenantIdIn(String... tenantIds);

  /** Order by variable name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  VariableInstanceQuery orderByVariableName();

  /** Order by variable type (needs to be followed by {@link #asc()} or {@link #desc()}). */
  VariableInstanceQuery orderByVariableType();

  /** Order by activity instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  VariableInstanceQuery orderByActivityInstanceId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of variable instances without tenant id is database-specific.
   */
  VariableInstanceQuery orderByTenantId();

}
