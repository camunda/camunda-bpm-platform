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

import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;

import java.util.Date;

/**
 * Programmatic querying for {@link HistoricDetail}s.
 *
 * @author Tom Baeyens
 */
public interface HistoricDetailQuery extends Query<HistoricDetailQuery, HistoricDetail> {

  /**
   * Only select the historic detail with the given id.
   *
   * @param id the historic detail to select
   * @return the query builder
   */
  HistoricDetailQuery detailId(String id);

  /** Only select historic variable updates with the given process instance.
   * {@link ProcessInstance} ids and {@link HistoricProcessInstance} ids match. */
  HistoricDetailQuery processInstanceId(String processInstanceId);

  /** Only select historic variable updates with the given case instance.
   * {@link CaseInstance} ids and {@link HistoricCaseInstance} ids match. */
  HistoricDetailQuery caseInstanceId(String caseInstanceId);

  /** Only select historic variable updates with the given execution.
   * Note that {@link Execution} ids are not stored in the history as first class citizen,
   * only process instances are.*/
  HistoricDetailQuery executionId(String executionId);

  /** Only select historic variable updates with the given case execution.
   * Note that {@link CaseExecution} ids are not stored in the history as first class citizen,
   * only case instances are.*/
  HistoricDetailQuery caseExecutionId(String caseExecutionId);

  /** Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}.
   * @deprecated since 5.2, use {@link #activityInstanceId(String)} instead */
  HistoricDetailQuery activityId(String activityId);

  /** Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}. */
  HistoricDetailQuery activityInstanceId(String activityInstanceId);

  /** Only select historic variable updates associated to the given {@link HistoricTaskInstance historic task instance}. */
  HistoricDetailQuery taskId(String taskId);

  /** Only select historic variable updates associated to the given {@link HistoricVariableInstance historic variable instance}. */
  HistoricDetailQuery variableInstanceId(String variableInstanceId);

  /** Only select historic process variables which match one of the given variable types. */
  HistoricDetailQuery variableTypeIn(String... variableTypes);

  /** Only select {@link HistoricFormProperty}s. */
  @Deprecated
  HistoricDetailQuery formProperties();

  /** Only select {@link HistoricFormField}s. */
  HistoricDetailQuery formFields();

  /** Only select {@link HistoricVariableUpdate}s. */
  HistoricDetailQuery variableUpdates();

  /**
   * Disable fetching of byte array and file values. By default, the query will fetch such values.
   * By calling this method you can prevent the values of (potentially large) blob data chunks to be fetched.
   *  The variables themselves are nonetheless included in the query result.
   *
   * @return the query builder
   */
  HistoricDetailQuery disableBinaryFetching();

  /**
   * Disable deserialization of variable values that are custom objects. By default, the query
   * will attempt to deserialize the value of these variables. By calling this method you can
   * prevent such attempts in environments where their classes are not available.
   * Independent of this setting, variable serialized values are accessible.
   */
  HistoricDetailQuery disableCustomObjectDeserialization();

  /** Exclude all task-related {@link HistoricDetail}s, so only items which have no
   * task-id set will be selected. When used together with {@link #taskId(String)}, this
   * call is ignored task details are NOT excluded.
   */
  HistoricDetailQuery excludeTaskDetails();

  /** Only select historic details with one of the given tenant ids. */
  HistoricDetailQuery tenantIdIn(String... tenantIds);

  /** Only selects historic details that have no tenant id. */
  HistoricDetailQuery withoutTenantId();

  /** Only select historic details with the given process instance ids. */
  HistoricDetailQuery processInstanceIdIn(String... processInstanceIds);

  /**
   * Select historic details related with given userOperationId.
   */
  HistoricDetailQuery userOperationId(String userOperationId);

  /** Only select historic details that have occurred before the given date (inclusive). */
  HistoricDetailQuery occurredBefore(Date date);

  /** Only select historic details that have occurred after the given date (inclusive). */
  HistoricDetailQuery occurredAfter(Date date);

  /** Only select historic details that were set during the process start. */
  HistoricDetailQuery initial();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic details without tenant id is database-specific.
   */
  HistoricDetailQuery orderByTenantId();

  HistoricDetailQuery orderByProcessInstanceId();

  HistoricDetailQuery orderByVariableName();

  HistoricDetailQuery orderByFormPropertyId();

  HistoricDetailQuery orderByVariableType();

  HistoricDetailQuery orderByVariableRevision();

  HistoricDetailQuery orderByTime();

  /**
   * <p>Sort the {@link HistoricDetail historic detail events} in the order in which
   * they occurred and needs to be followed by {@link #asc()} or {@link #desc()}.</p>
   *
   * <p>The set of all {@link HistoricVariableUpdate historic variable update events} is
   * a <strong>partially ordered set</strong>. Due to this fact {@link HistoricVariableUpdate
   * historic variable update events} for two different {@link VariableInstance variable
   * instances} are <strong>incomparable</strong>. So that it is not possible to sort
   * the {@link HistoricDetail historic variable update events} for two {@link VariableInstance
   * variable instances} in the order they occurred. Just for one {@link VariableInstance variable
   * instance} the set of {@link HistoricVariableUpdate historic variable update events} can be
   * <strong>totally ordered</strong> by using {@link #variableInstanceId(String)} and {@link
   * #orderPartiallyByOccurrence()} which will return a result set ordered by its occurrence.</p>
   *
   * <p><strong>For example:</strong><br>
   * An execution variable <code>myVariable</code> will be updated multiple times:</p>
   *
   * <code>
   * runtimeService.setVariable("anExecutionId", "myVariable", 1000);<br>
   * execution.setVariable("myVariable", 5000);<br>
   * runtimeService.setVariable("anExecutionId", "myVariable", 2500);<br>
   * runtimeService.removeVariable("anExecutionId", "myVariable");
   * </code>
   *
   * <p>As a result there exists four {@link HistoricVariableUpdate historic variable update events}.</p>
   *
   * <p>By using {@link #variableInstanceId(String)} and {@link #orderPartiallyByOccurrence()} it
   * is possible to sort the events in the order in which they occurred. The following query</p>
   *
   * <code>
   * historyService.createHistoricDetailQuery()<br>
   * &nbsp;&nbsp;.variableInstanceId("myVariableInstId")<br>
   * &nbsp;&nbsp;.orderPartiallyByOccurrence()<br>
   * &nbsp;&nbsp;.asc()<br>
   * &nbsp;&nbsp;.list()
   * </code>
   *
   * <p>will return the following totally ordered result set</p>
   *
   * <code>
   * [<br>
   * &nbsp;&nbsp;HistoricVariableUpdate[id: "myVariableInstId", variableName: "myVariable", value: 1000],<br>
   * &nbsp;&nbsp;HistoricVariableUpdate[id: "myVariableInstId", variableName: "myVariable", value: 5000],<br>
   * &nbsp;&nbsp;HistoricVariableUpdate[id: "myVariableInstId", variableName: "myVariable", value: 2500]<br>
   * &nbsp;&nbsp;HistoricVariableUpdate[id: "myVariableInstId", variableName: "myVariable", value: null]<br>
   * ]
   * </code>
   *
   * <p><strong>Note:</strong><br>
   * Please note that a {@link HistoricFormField historic form field event} can occur only once.</p>
   *
   * @since 7.3
   */
  HistoricDetailQuery orderPartiallyByOccurrence();
}
