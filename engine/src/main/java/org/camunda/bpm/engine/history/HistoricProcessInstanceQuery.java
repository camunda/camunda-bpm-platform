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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * Allows programmatic querying of {@link HistoricProcessInstance}s.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public interface HistoricProcessInstanceQuery extends Query<HistoricProcessInstanceQuery, HistoricProcessInstance> {

  /** Only select historic process instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricProcessInstanceQuery processInstanceId(String processInstanceId);

  /** Only select historic process instances whose id is in the given set of ids.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds);

  /** Only select historic process instances for the given process definition */
  HistoricProcessInstanceQuery processDefinitionId(String processDefinitionId);

  /** Only select historic process instances that are defined by a process
   * definition with the given key.  */
  HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey);

  /** Only select historic process instances that are defined by any given process
   * definition key.  */
  HistoricProcessInstanceQuery processDefinitionKeyIn(String... processDefinitionKeys);

  /** Only select historic process instances that don't have a process-definition of which the key is present in the given list */
  HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys);

  /** Only select historic process instances that are defined by a process
   * definition with the given name.  */
  HistoricProcessInstanceQuery processDefinitionName(String processDefinitionName);

  /**
   * Only select historic process instances that are defined by process definition which name
   * is like the given value.
   *
   * @param nameLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricProcessInstanceQuery processDefinitionNameLike(String nameLike);

  /** Only select historic process instances with the given business key */
  HistoricProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);

  /** Only select historic process instances whose business key is in the given set. */
  HistoricProcessInstanceQuery processInstanceBusinessKeyIn(String... processInstanceBusinessKeyIn);

  /**
   * Only select historic process instances which had a business key like the given value.
   *
   * @param processInstanceBusinessKeyLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricProcessInstanceQuery processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike);

  /** Only select historic process instances that are completely finished. */
  HistoricProcessInstanceQuery finished();

  /** Only select historic process instance that are not yet finished. */
  HistoricProcessInstanceQuery unfinished();

  /**
   * Only select historic process instances with incidents
   *
   * @return HistoricProcessInstanceQuery
   */
  HistoricProcessInstanceQuery withIncidents();

  /**
   * Only select historic process instances with root incidents
   *
   * @return HistoricProcessInstanceQuery
   */
  HistoricProcessInstanceQuery withRootIncidents();

  /** Only select historic process instances with incident status either 'open' or 'resolved'.
   * To get all process instances with incidents, use {@link HistoricProcessInstanceQuery#withIncidents()}.
   *
   * @param status indicates the incident status, which is either 'open' or 'resolved'
   * @return {@link HistoricProcessInstanceQuery}
   */
  HistoricProcessInstanceQuery incidentStatus(String status);

  /**
   * Only selects process instances with the given incident type.
   */
  HistoricProcessInstanceQuery incidentType(String incidentType);

  /**
   * Only select historic process instances with the given incident message.
   *
   * @param incidentMessage Incidents Message for which the historic process instances should be selected
   *
   * @return HistoricProcessInstanceQuery
   */
  HistoricProcessInstanceQuery incidentMessage(String incidentMessage);

  /**
   * Only select historic process instances which had an incident message like the given value.
   *
   * @param incidentMessageLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   *
   * @return HistoricProcessInstanceQuery
   */
  HistoricProcessInstanceQuery incidentMessageLike(String incidentMessageLike);

  /** Only select historic process instances which are associated with the given case instance id. */
  HistoricProcessInstanceQuery caseInstanceId(String caseInstanceId);

  /**
   * The query will match the names of variables in a case-insensitive way.
   */
  HistoricProcessInstanceQuery matchVariableNamesIgnoreCase();

  /**
   * The query will match the values of variables in a case-insensitive way.
   */
  HistoricProcessInstanceQuery matchVariableValuesIgnoreCase();

  /** Only select process instances which had a global variable with the given value
   * when they ended. Only select process instances which have a variable value
   * greater than the passed value. The type only applies to already ended
   * process instances, otherwise use a {@link ProcessInstanceQuery} instead! of
   * variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfiguration#getVariableSerializers()}. Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name of the variable, cannot be null. */
  HistoricProcessInstanceQuery variableValueEquals(String name, Object value);

  /** Only select process instances which had a global variable with the given name, but
   * with a different value than the passed value when they ended. Only select
   * process instances which have a variable value greater than the passed
   * value. Byte-arrays and {@link Serializable} objects (which are not
   * primitive type wrappers) are not supported.
   * @param name of the variable, cannot be null. */
  HistoricProcessInstanceQuery variableValueNotEquals(String name, Object value);

  /** Only select process instances which had a global variable value greater than the
   * passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported. Only select process instances which have a variable value
   * greater than the passed value.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricProcessInstanceQuery variableValueGreaterThan(String name, Object value);

  /** Only select process instances which had a global variable value greater than or
   * equal to the passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported. Only applies to already ended process instances, otherwise
   * use a {@link ProcessInstanceQuery} instead!
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);

  /** Only select process instances which had a global variable value less than the
   * passed value when the ended. Only applies to already ended process
   * instances, otherwise use a {@link ProcessInstanceQuery} instead! Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricProcessInstanceQuery variableValueLessThan(String name, Object value);

  /** Only select process instances which has a global variable value less than or equal
   * to the passed value when they ended. Only applies to already ended process
   * instances, otherwise use a {@link ProcessInstanceQuery} instead! Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value);

  /** Only select process instances which had global variable value like the given value
   * when they ended. Only applies to already ended process instances, otherwise
   * use a {@link ProcessInstanceQuery} instead! This can be used on string
   * variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  HistoricProcessInstanceQuery variableValueLike(String name, String value);

  /** Only select historic process instances that were started before the given date. */
  HistoricProcessInstanceQuery startedBefore(Date date);

  /** Only select historic process instances that were started after the given date. */
  HistoricProcessInstanceQuery startedAfter(Date date);

  /** Only select historic process instances that were started before the given date. */
  HistoricProcessInstanceQuery finishedBefore(Date date);

  /** Only select historic process instances that were started after the given date. */
  HistoricProcessInstanceQuery finishedAfter(Date date);

  /** Only select historic process instance that are started by the given user. */
  HistoricProcessInstanceQuery startedBy(String userId);

  /** Order by the process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceId();

  /** Order by the process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessDefinitionId();

  /** Order by the process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessDefinitionKey();

  /** Order by the process definition name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessDefinitionName();

  /** Order by the process definition version (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessDefinitionVersion();

  /** Order by the business key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceBusinessKey();

  /** Order by the start time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceStartTime();

  /** Order by the end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceEndTime();

  /** Order by the duration of the process instance (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceDuration();

  /** Only select historic process instances that are top level process instances. */
  HistoricProcessInstanceQuery rootProcessInstances();

  /** Only select historic process instances started by the given process
   * instance. {@link ProcessInstance) ids and {@link HistoricProcessInstance}
   * ids match. */
  HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId);

  /** Only select historic process instances having a sub process instance
   * with the given process instance id.
   *
   * Note that there will always be maximum only <b>one</b>
   * such process instance that can be the result of this query.
   */
  HistoricProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId);

  /** Only select historic process instances started by the given case
   * instance. */
  HistoricProcessInstanceQuery superCaseInstanceId(String superCaseInstanceId);

  /** Only select historic process instances having a sub case instance
   * with the given case instance id.
   *
   * Note that there will always be maximum only <b>one</b>
   * such process instance that can be the result of this query.
   */
  HistoricProcessInstanceQuery subCaseInstanceId(String subCaseInstanceId);

  /** Only select historic process instances with one of the given tenant ids. */
  HistoricProcessInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects historic process instances which have no tenant id. */
  HistoricProcessInstanceQuery withoutTenantId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic process instances without tenant id is database-specific.
   */
  HistoricProcessInstanceQuery orderByTenantId();

  /** Only select historic process instances that were started as of the provided
   * date. (Date will be adjusted to reflect midnight)
   * @deprecated use {@link #startedAfter(Date)} and {@link #startedBefore(Date)} instead */
  @Deprecated
  HistoricProcessInstanceQuery startDateBy(Date date);

  /** Only select historic process instances that were started on the provided date.
   * @deprecated use {@link #startedAfter(Date)} and {@link #startedBefore(Date)} instead */
  @Deprecated
  HistoricProcessInstanceQuery startDateOn(Date date);

  /** Only select historic process instances that were finished as of the
   * provided date. (Date will be adjusted to reflect one second before midnight)
   * @deprecated use {@link #startedAfter(Date)} and {@link #startedBefore(Date)} instead */
  @Deprecated
  HistoricProcessInstanceQuery finishDateBy(Date date);

  /** Only select historic process instances that were finished on provided date.
   * @deprecated use {@link #startedAfter(Date)} and {@link #startedBefore(Date)} instead */
  @Deprecated
  HistoricProcessInstanceQuery finishDateOn(Date date);

  /** Only select historic process instances that executed an activity after the given date. */
  HistoricProcessInstanceQuery executedActivityAfter(Date date);

  /** Only select historic process instances that executed an activity before the given date. */
  HistoricProcessInstanceQuery executedActivityBefore(Date date);

  /** Only select historic process instances that executed activities with given ids. */
  HistoricProcessInstanceQuery executedActivityIdIn(String... ids);

  /** Only select historic process instances that have active activities with given ids. */
  HistoricProcessInstanceQuery activeActivityIdIn(String... ids);

  /** Only select historic process instances that executed an job after the given date. */
  HistoricProcessInstanceQuery executedJobAfter(Date date);

  /** Only select historic process instances that executed an job before the given date. */
  HistoricProcessInstanceQuery executedJobBefore(Date date);

  /** Only select historic process instances that are active. */
  HistoricProcessInstanceQuery active();

  /** Only select historic process instances that are suspended. */
  HistoricProcessInstanceQuery suspended();

  /** Only select historic process instances that are completed. */
  HistoricProcessInstanceQuery completed();

  /** Only select historic process instances that are externallyTerminated. */
  HistoricProcessInstanceQuery externallyTerminated();

  /** Only select historic process instances that are internallyTerminated. */
  HistoricProcessInstanceQuery internallyTerminated();

  /**
   * <p>After calling or(), a chain of several filter criteria could follow. Each filter criterion that follows or()
   * will be linked together with an OR expression until the OR query is terminated. To terminate the OR query right
   * after the last filter criterion was applied, {@link #endOr()} must be invoked.</p>
   *
   * @return an object of the type {@link HistoricProcessInstanceQuery} on which an arbitrary amount of filter criteria could be applied.
   * The several filter criteria will be linked together by an OR expression.
   *
   * @throws ProcessEngineException when or() has been invoked directly after or() or after or() and trailing filter
   * criteria. To prevent throwing this exception, {@link #endOr()} must be invoked after a chain of filter criteria to
   * mark the end of the OR query.
   * */
  HistoricProcessInstanceQuery or();

  /**
   * <p>endOr() terminates an OR query on which an arbitrary amount of filter criteria were applied. To terminate the
   * OR query which has been started by invoking {@link #or()}, endOr() must be invoked. Filter criteria which are
   * applied after calling endOr() are linked together by an AND expression.</p>
   *
   * @return an object of the type {@link HistoricProcessInstanceQuery} on which an arbitrary amount of filter criteria could be applied.
   * The filter criteria will be linked together by an AND expression.
   *
   * @throws ProcessEngineException when endOr() has been invoked before {@link #or()} was invoked. To prevent throwing
   * this exception, {@link #or()} must be invoked first.
   * */
  HistoricProcessInstanceQuery endOr();
}
