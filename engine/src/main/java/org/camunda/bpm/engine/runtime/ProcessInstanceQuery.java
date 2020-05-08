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
package org.camunda.bpm.engine.runtime;

import java.io.Serializable;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.query.Query;

/**
 * Allows programmatic querying of {@link ProcessInstance}s.
 *
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 */
public interface ProcessInstanceQuery extends Query<ProcessInstanceQuery, ProcessInstance> {

  /** Select the process instance with the given id */
  ProcessInstanceQuery processInstanceId(String processInstanceId);

  /** Select process instances whose id is in the given set of ids */
  ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds);

  /** Select process instances with the given business key */
  ProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);

  /** Select process instance with the given business key, unique for the given process definition */
  ProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey, String processDefinitionKey);

  /**
   * Select process instances with a business key like the given value.
   *
   * @param processInstanceBusinessKeyLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  ProcessInstanceQuery processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike);

  /**
   * Select the process instances which are defined by a process definition with
   * the given key.
   */
  ProcessInstanceQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Select the process instances for any given process definition keys.
   */
  ProcessInstanceQuery processDefinitionKeyIn(String... processDefinitionKeys);

  /** Select historic process instances that don't have a process-definition of which the key is present in the given list */
  ProcessInstanceQuery processDefinitionKeyNotIn(String... processDefinitionKeys);

  /**
   * Selects the process instances which are defined by a process definition
   * with the given id.
   */
  ProcessInstanceQuery processDefinitionId(String processDefinitionId);

  /**
   * Selects the process instances which belong to the given deployment id.
   * @since 7.4
   */
  ProcessInstanceQuery deploymentId(String deploymentId);

  /**
   * Select the process instances which are a sub process instance of the given
   * super process instance.
   */
  ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId);

  /**
   * Select the process instance that have as sub process instance the given
   * process instance. Note that there will always be maximum only <b>one</b>
   * such process instance that can be the result of this query.
   */
  ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId);

  /**
   * Selects the process instances which are associated with the given case instance id.
   */
  ProcessInstanceQuery caseInstanceId(String caseInstanceId);

  /**
   * Select the process instances which are a sub process instance of the given
   * super case instance.
   *
   * @since 7.3
   */
  ProcessInstanceQuery superCaseInstanceId(String superCaseInstanceId);

  /**
   * Select the process instance that has as sub case instance the given
   * case instance. Note that there will always be at most <b>one</b>
   * such process instance that can be the result of this query.
   *
   * @since 7.3
   */
  ProcessInstanceQuery subCaseInstanceId(String subCaseInstanceId);

  /**
   * The query will match the names of process-variables in a case-insensitive way.
   */
  ProcessInstanceQuery matchVariableNamesIgnoreCase();

  /**
   * The query will match the values of process-variables in a case-insensitive way.
   */
  ProcessInstanceQuery matchVariableValuesIgnoreCase();

  /**
   * Only select process instances which have a global variable with the given value. The type
   * of variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfiguration#getVariableSerializers()}.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ProcessInstanceQuery variableValueEquals(String name, Object value);

  /**
   * Only select process instances which have a global variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ProcessInstanceQuery variableValueNotEquals(String name, Object value);


  /**
   * Only select process instances which have a variable value greater than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueGreaterThan(String name, Object value);

  /**
   * Only select process instances which have a global variable value greater than or equal to
   * the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select process instances which have a global variable value less than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueLessThan(String name, Object value);

  /**
   * Only select process instances which have a global variable value less than or equal to the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select process instances which have a global variable value like the given value.
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy:
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  ProcessInstanceQuery variableValueLike(String name, String value);

  /**
   * Only selects process instances which are suspended, either because the
   * process instance itself is suspended or because the corresponding process
   * definition is suspended
   */
  ProcessInstanceQuery suspended();

  /**
   * Only selects process instances which are active, which means that
   * neither the process instance nor the corresponding process definition
   * are suspended.
   */
  ProcessInstanceQuery active();

  /**
   * Only selects process instances with at least one incident.
   */
  ProcessInstanceQuery withIncident();

  /**
   * Only selects process instances with the given incident type.
   */
  ProcessInstanceQuery incidentType(String incidentType);

  /**
   * Only selects process instances with the given incident id.
   */
  ProcessInstanceQuery incidentId(String incidentId);

  /**
   * Only selects process instances with the given incident message.
   */
  ProcessInstanceQuery incidentMessage(String incidentMessage);

  /**
   * Only selects process instances with an incident message like the given.
   */
  ProcessInstanceQuery incidentMessageLike(String incidentMessageLike);

  /** Only select process instances with one of the given tenant ids. */
  ProcessInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects process instances which have no tenant id. */
  ProcessInstanceQuery withoutTenantId();

  /**
   * <p>Only selects process instances with leaf activity instances
   * or transition instances (async before, async after) in
   * at least one of the given activity ids.
   *
   * <p><i>Leaf instance</i> means this filter works for instances
   * of a user task is matched, but not the embedded sub process it is
   * contained in.
   */
  ProcessInstanceQuery activityIdIn(String... activityIds);

  /** Only selects process instances which are top level process instances. */
  ProcessInstanceQuery rootProcessInstances();

  /** Only selects process instances which don't have subprocesses and thus are leaves of the execution tree. */
  ProcessInstanceQuery leafProcessInstances();

  /** Only selects process instances which process definition has no tenant id. */
  ProcessInstanceQuery processDefinitionWithoutTenantId();

  //ordering /////////////////////////////////////////////////////////////////

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessInstanceId();

  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessDefinitionKey();

  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessDefinitionId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of process instances without tenant id is database-specific.
   */
  ProcessInstanceQuery orderByTenantId();

  /** Order by the business key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByBusinessKey();

  /**
   * <p>After calling or(), a chain of several filter criteria could follow. Each filter criterion that follows or()
   * will be linked together with an OR expression until the OR query is terminated. To terminate the OR query right
   * after the last filter criterion was applied, {@link #endOr()} must be invoked.</p>
   *
   * @return an object of the type {@link ProcessInstanceQuery} on which an arbitrary amount of filter criteria could be applied.
   * The several filter criteria will be linked together by an OR expression.
   *
   * @throws ProcessEngineException when or() has been invoked directly after or() or after or() and trailing filter
   * criteria. To prevent throwing this exception, {@link #endOr()} must be invoked after a chain of filter criteria to
   * mark the end of the OR query.
   * */
  ProcessInstanceQuery or();

  /**
   * <p>endOr() terminates an OR query on which an arbitrary amount of filter criteria were applied. To terminate the
   * OR query which has been started by invoking {@link #or()}, endOr() must be invoked. Filter criteria which are
   * applied after calling endOr() are linked together by an AND expression.</p>
   *
   * @return an object of the type {@link ProcessInstanceQuery} on which an arbitrary amount of filter criteria could be applied.
   * The filter criteria will be linked together by an AND expression.
   *
   * @throws ProcessEngineException when endOr() has been invoked before {@link #or()} was invoked. To prevent throwing
   * this exception, {@link #or()} must be invoked first.
   * */
  ProcessInstanceQuery endOr();
}
