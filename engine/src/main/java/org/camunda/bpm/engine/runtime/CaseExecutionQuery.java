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
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Roman Smirnov
 *
 */
public interface CaseExecutionQuery extends Query<CaseExecutionQuery, CaseExecution> {

  /**
   * Only select case executions which have the given case instance id.
   *
   * @param caseInstanceId the id of the case instance
   */
  CaseExecutionQuery caseInstanceId(String caseInstanceId);

  /**
   * Only select case executions which have the given case definition id.
   *
   * @param caseDefinitionId the id of hte case definition
   */
  CaseExecutionQuery caseDefinitionId(String caseDefinitionId);

  /**
   * Only select case executions which have the given case definition key.
   *
   * @param caseDefinitionKey the key of the case definition
   */
  CaseExecutionQuery caseDefinitionKey(String caseDefinitionKey);

  /**
   * Only select case executions that belong to a case instance with the given business key
   *
   * @param caseInstanceBusinessKey the business key of the case instance
   */
  CaseExecutionQuery caseInstanceBusinessKey(String caseInstanceBusinessKey);

  /**
   * Only select case executions with the given id.
   *
   * @param executionId the id of the case execution
   */
  CaseExecutionQuery caseExecutionId(String executionId);

  /**
   * Only select case executions which contain an activity with the given id.
   *
   * @param activityId the id of the activity
   */
  CaseExecutionQuery activityId(String activityId);

  /** Only select case executions which are enabled. **/
  CaseExecutionQuery enabled();

  /** Only select case executions which are active. **/
  CaseExecutionQuery active();

  /** Only select case executions which are disabled. **/
  CaseExecutionQuery disabled();

  /**
   * Only select case executions which have a local variable with the given value. The type
   * of variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfigurationImpl#getVariableTypes()}.
   *
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable
   */
  CaseExecutionQuery variableValueEquals(String name, Object value);

  /**
   * Only select case executions which have a local variable with the given name, but
   * with a different value than the passed value.
   *
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable
   */
  CaseExecutionQuery variableValueNotEquals(String name, Object value);


  /**
   * Only select case executions which have a variable value greater than the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery variableValueGreaterThan(String name, Object value);

  /**
   * Only select case executions which have a local variable value greater than or equal to
   * the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery variableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select case executions which have a local variable value less than the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery variableValueLessThan(String name, Object value);

  /**
   * Only select case executions which have a local variable value less than or equal to the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery variableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select case executions which have a local variable value like the given value.
   *
   * This can be used on string variables only.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null. The string can include the
   *              wildcard character '%' to express like-strategy:
   *              starts with (string%), ends with (%string) or contains (%string%).
   */
  CaseExecutionQuery variableValueLike(String name, String value);

  /**
   * Only select case executions which are part of a case instance that have a variable
   * with the given name set to the given value. The type of variable is determined based
   * on the value, using types configured in {@link ProcessEngineConfiguration#getVariableTypes()}.
   *
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable
   */
  CaseExecutionQuery caseInstanceVariableValueEquals(String name, Object value);

  /**
   * Only select case executions which are part of a case instance that have a variable
   * with the given name, but with a different value than the passed value.
   *
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable
   */
  CaseExecutionQuery caseInstanceVariableValueNotEquals(String name, Object value);


  /**
   * Only select case executions which are part of a case instance that have a variable
   * with the given name and a variable value greater than the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery caseInstanceVariableValueGreaterThan(String name, Object value);

  /**
   * Only select case executions which are part of a case instance that have a
   * variable value greater than or equal to the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery caseInstanceVariableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select case executions which are part of a case instance that have a variable
   * value less than the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery caseInstanceVariableValueLessThan(String name, Object value);

  /**
   * Only select case executions which are part of a case instance that have a variable
   * value less than or equal to the passed value.
   *
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null
   */
  CaseExecutionQuery caseInstanceVariableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select case executions which are part of a case instance that have a variable value
   * like the given value.
   *
   * This can be used on string variables only.
   *
   * @param name the name of the variable, cannot be null
   * @param value the value of the variable, cannot be null. The string can include the
   *              wildcard character '%' to express like-strategy:
   *              starts with (string%), ends with (%string) or contains (%string%).
   */
  CaseExecutionQuery caseInstanceVariableValueLike(String name, String value);


  // ordering //////////////////////////////////////////////////////////////

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  CaseExecutionQuery orderByCaseExecutionId();

  /** Order by case definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  CaseExecutionQuery orderByCaseDefinitionKey();

  /** Order by case definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  CaseExecutionQuery orderByCaseDefinitionId();

}
