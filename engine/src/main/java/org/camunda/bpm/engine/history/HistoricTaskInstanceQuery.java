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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.task.Task;


/**
 * Allows programmatic querying for {@link HistoricTaskInstance}s.
 *
 * @author Tom Baeyens
 */
public interface HistoricTaskInstanceQuery  extends Query<HistoricTaskInstanceQuery, HistoricTaskInstance> {

  /** Only select historic task instances for the given task id. */
  HistoricTaskInstanceQuery taskId(String taskId);

  /** Only select historic task instances for the given process instance. */
  HistoricTaskInstanceQuery processInstanceId(String processInstanceId);

  /** Only select historic tasks for the given process instance business key */
  HistoricTaskInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);

  /**
   * Only select historic tasks for any of the given the given process instance business keys.
   */
  HistoricTaskInstanceQuery processInstanceBusinessKeyIn(String... processInstanceBusinessKeys);

  /** Only select historic tasks matching the given process instance business key.
   *  The syntax is that of SQL: for example usage: nameLike(%camunda%)*/
  HistoricTaskInstanceQuery processInstanceBusinessKeyLike(String processInstanceBusinessKey);

  /** Only select historic task instances for the given execution. */
  HistoricTaskInstanceQuery executionId(String executionId);

  /** Only select historic task instances which have one of the given activity instance ids. **/
  HistoricTaskInstanceQuery activityInstanceIdIn(String... activityInstanceIds);

  /** Only select historic task instances for the given process definition. */
  HistoricTaskInstanceQuery processDefinitionId(String processDefinitionId);

  /**
   * Only select historic task instances which are part of a (historic) process instance
   * which has the given process definition key.
   */
  HistoricTaskInstanceQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Only select historic task instances which are part of a (historic) process instance
   * which has the given definition name.
   */
  HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName);

  /** Only select historic task instances for the given case definition. */
  HistoricTaskInstanceQuery caseDefinitionId(String caseDefinitionId);

  /**
   * Only select historic task instances which are part of a case instance
   * which has the given case definition key.
   */
  HistoricTaskInstanceQuery caseDefinitionKey(String caseDefinitionKey);

  /**
   * Only select historic task instances which are part of a (historic) case instance
   * which has the given case definition name.
   */
  HistoricTaskInstanceQuery caseDefinitionName(String caseDefinitionName);

  /**
   * Only select historic task instances for the given case instance.
   */
  HistoricTaskInstanceQuery caseInstanceId(String caseInstanceId);

  /**
   * Only select historic task instances for the given case execution id.
   */
  HistoricTaskInstanceQuery caseExecutionId(String caseExecutionId);

  /**
   * Only select historic task instances with the given task name.
   * This is the last name given to the task.
   * The query will match the names of historic task instances in a case-insensitive way.
   */
  HistoricTaskInstanceQuery taskName(String taskName);

  /**
   * Only select historic task instances with a task name like the given value.
   * This is the last name given to the task.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   * The query will match the names of historic task instances in a case-insensitive way.
   */
  HistoricTaskInstanceQuery taskNameLike(String taskNameLike);

  /**
   * Only select historic task instances with the given task description.
   * This is the last description given to the task.
   * The query will match the descriptions of historic task instances in a case-insensitive way.
   */
  HistoricTaskInstanceQuery taskDescription(String taskDescription);

  /**
   * Only select historic task instances with a task description like the given value.
   * This is the last description given to the task.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   * The query will match the descriptions of historice task instances in a case-insensitive way.
   */
  HistoricTaskInstanceQuery taskDescriptionLike(String taskDescriptionLike);

  /**
   * Only select historic task instances with the given task definition key.
   * @see Task#getTaskDefinitionKey()
   */
  HistoricTaskInstanceQuery taskDefinitionKey(String taskDefinitionKey);

  /**
   * Only select historic task instances with one of the given task definition keys.
   * @see Task#getTaskDefinitionKey()
   */
  HistoricTaskInstanceQuery taskDefinitionKeyIn(String... taskDefinitionKeys);

  /** Only select historic task instances with the given task delete reason. */
  HistoricTaskInstanceQuery taskDeleteReason(String taskDeleteReason);

  /**
   * Only select historic task instances with a task description like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskDeleteReasonLike(String taskDeleteReasonLike);

  /** Only select historic task instances with an assignee. */
  HistoricTaskInstanceQuery taskAssigned();

  /** Only select historic task instances without an assignee. */
  HistoricTaskInstanceQuery taskUnassigned();

  /**
   * Only select historic task instances which were last taskAssigned to the given assignee.
   */
  HistoricTaskInstanceQuery taskAssignee(String taskAssignee);

  /**
   * Only select historic task instances which were last taskAssigned to an assignee like
   * the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskAssigneeLike(String taskAssigneeLike);

  /**
   * Only select historic task instances which have the given owner.
   */
  HistoricTaskInstanceQuery taskOwner(String taskOwner);

  /**
   * Only select historic task instances which have an owner like the one specified.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskOwnerLike(String taskOwnerLike);

  /**
   * Only select historic task instances with the given priority.
   */
  HistoricTaskInstanceQuery taskPriority(Integer taskPriority);

  /**
   * Only select historic task instances which are finished.
   */
  HistoricTaskInstanceQuery finished();

  /**
   * Only select historic task instances which aren't finished yet.
   */
  HistoricTaskInstanceQuery unfinished();

  /**
   * Only select historic task instances which are part of a process
   * instance which is already finished.
   */
  HistoricTaskInstanceQuery processFinished();

  /**
   * Only select historic task instances which are part of a process
   * instance which is not finished yet.
   */
  HistoricTaskInstanceQuery processUnfinished();

  /**
   * Only select historic task instances which have mapping
   * with Historic identity links based on user id
   *
   * @since 7.5
   */
  HistoricTaskInstanceQuery taskInvolvedUser(String involvedUser);

  /**
   * Only select historic task instances which have mapping
   * with Historic identity links based on group id
   *
   * @since 7.5
   */
  HistoricTaskInstanceQuery taskInvolvedGroup(String involvedGroup);

  /**
   * Only select historic task instances which have mapping
   * with Historic identity links with the condition of user being a candidate
   *
   * @since 7.5
   */
  HistoricTaskInstanceQuery taskHadCandidateUser(String candidateUser);

  /**
   * Only select historic task instances which have mapping
   * with Historic identity links with the condition of group being a candidate
   *
   * @since 7.5
   */
  HistoricTaskInstanceQuery taskHadCandidateGroup(String candidateGroup);

  /** Only select historic task instances which have a candidate group */
  HistoricTaskInstanceQuery withCandidateGroups();

  /** Only select historic task instances which have no candidate group */
  HistoricTaskInstanceQuery withoutCandidateGroups();

  /**
   * The query will match the names of task and process variables in a case-insensitive way.
   */
  HistoricTaskInstanceQuery matchVariableNamesIgnoreCase();

  /**
   * The query will match the values of task and process variables in a case-insensitive way.
   */
  HistoricTaskInstanceQuery matchVariableValuesIgnoreCase();

  /**
   * Only select historic task instances which have a local task variable with the
   * given name set to the given value. Make sure history-level is configured
   * >= AUDIT when this feature is used.
   */
  HistoricTaskInstanceQuery taskVariableValueEquals(String variableName, Object variableValue);

  /** Only select subtasks of the given parent task */
  HistoricTaskInstanceQuery taskParentTaskId(String parentTaskId);

  /**
   * Only select historic task instances which are part of a process instance which have a variable
   * with the given name set to the given value. The last variable value in the variable updates
   * ({@link HistoricDetail}) will be used, so make sure history-level is configured
   * >= AUDIT when this feature is used.
   */
  HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue);

  /**
   * Only select historic task instances which have a variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  HistoricTaskInstanceQuery processVariableValueNotEquals(String variableName, Object variableValue);

  /**
   * Only select historic task instances which are part of a process that have a variable
   * with the given name and matching the given value.
   * The syntax is that of SQL: for example usage: valueLike(%value%)
   * */
  HistoricTaskInstanceQuery processVariableValueLike(String variableName, Object variableValue);

  /**
   * Only select historic task instances which are part of a process that have a variable
   * with the given name and not matching the given value.
   * The syntax is that of SQL: for example usage: valueNotLike(%value%)
   * */
  HistoricTaskInstanceQuery processVariableValueNotLike(String variableName, Object variableValue);

  /**
   * Only select historic task instances which are part of a process that have a variable
   * with the given name and a value greater than the given one.
   */
  HistoricTaskInstanceQuery processVariableValueGreaterThan(String variableName, Object variableValue);

  /**
   * Only select historic task instances which are part of a process that have a variable
   * with the given name and a value greater than or equal to the given one.
   */
  HistoricTaskInstanceQuery processVariableValueGreaterThanOrEquals(String variableName, Object variableValue);

  /**
   * Only select historic task instances which are part of a process that have a variable
   * with the given name and a value less than the given one.
   */
  HistoricTaskInstanceQuery processVariableValueLessThan(String variableName, Object variableValue);

  /**
   * Only select historic task instances which are part of a process that have a variable
   * with the given name and a value less than or equal to the given one.
   */
  HistoricTaskInstanceQuery processVariableValueLessThanOrEquals(String variableName, Object variableValue);

  /**
   * Only select select historic task instances with the given due date.
   */
  HistoricTaskInstanceQuery taskDueDate(Date dueDate);

  /**
   * Only select select historic task instances which have a due date before the given date.
   */
  HistoricTaskInstanceQuery taskDueBefore(Date dueDate);

  /**
   * Only select select historic task instances which have a due date after the given date.
   */
  HistoricTaskInstanceQuery taskDueAfter(Date dueDate);

  /**
   * Only select select historic task instances that have no due date.
   */
  HistoricTaskInstanceQuery withoutTaskDueDate();

  /**
   * Only select select historic task instances with the given follow-up date.
   */
  HistoricTaskInstanceQuery taskFollowUpDate(Date followUpDate);

  /**
   * Only select select historic task instances which have a follow-up date before the given date.
   */
  HistoricTaskInstanceQuery taskFollowUpBefore(Date followUpDate);

  /**
   * Only select select historic task instances which have a follow-up date after the given date.
   */
  HistoricTaskInstanceQuery taskFollowUpAfter(Date followUpDate);

  /** Only select historic task instances with one of the given tenant ids. */
  HistoricTaskInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects historic task instances that have no tenant id. */
  HistoricTaskInstanceQuery withoutTenantId();

  /**
   * Only select tasks where end time is after given date
   */
  HistoricTaskInstanceQuery finishedAfter(Date date);

  /**
   * Only select tasks where end time is before given date
   */
  HistoricTaskInstanceQuery finishedBefore(Date date);

  /**
   * Only select tasks where started after given date
   */
  HistoricTaskInstanceQuery startedAfter(Date date);

  /**
   * Only select tasks where started before given date
   */
  HistoricTaskInstanceQuery startedBefore(Date date);

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic task instances without tenant id is database-specific.
   */
  HistoricTaskInstanceQuery orderByTenantId();

  /** Order by task id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskId();

  /**
   * Order by the historic activity instance id this task was used in
   * (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceId();

  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByProcessDefinitionId();

  /** Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByProcessInstanceId();

  /** Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByExecutionId();

  /** Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceDuration();

  /** Order by end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceEndTime();

  /** Order by start time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceStartTime();

  /** Order by task name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskName();

  /** Order by task description (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDescription();

  /** Order by task assignee (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskAssignee();

  /** Order by task owner (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskOwner();

  /** Order by task due date (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDueDate();

  /** Order by task follow-up date (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskFollowUpDate();

  /** Order by task delete reason (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByDeleteReason();

  /** Order by task definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDefinitionKey();

  /** Order by task priority key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskPriority();

  /** Order by case definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByCaseDefinitionId();

  /** Order by case instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByCaseInstanceId();

  /** Order by case execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByCaseExecutionId();

  /**
   * <p>After calling or(), a chain of several filter criteria could follow. Each filter criterion that follows or()
   * will be linked together with an OR expression until the OR query is terminated. To terminate the OR query right
   * after the last filter criterion was applied, {@link #endOr()} must be invoked.</p>
   *
   * @return an object of the type {@link HistoricTaskInstanceQuery} on which an arbitrary amount of filter criteria could be applied.
   * The several filter criteria will be linked together by an OR expression.
   *
   * @throws ProcessEngineException when or() has been invoked directly after or() or after or() and trailing filter
   * criteria. To prevent throwing this exception, {@link #endOr()} must be invoked after a chain of filter criteria to
   * mark the end of the OR query.
   * */
  HistoricTaskInstanceQuery or();

  /**
   * <p>endOr() terminates an OR query on which an arbitrary amount of filter criteria were applied. To terminate the
   * OR query which has been started by invoking {@link #or()}, endOr() must be invoked. Filter criteria which are
   * applied after calling endOr() are linked together by an AND expression.</p>
   *
   * @return an object of the type {@link HistoricTaskInstanceQuery} on which an arbitrary amount of filter criteria could be applied.
   * The filter criteria will be linked together by an AND expression.
   *
   * @throws ProcessEngineException when endOr() has been invoked before {@link #or()} was invoked. To prevent throwing
   * this exception, {@link #or()} must be invoked first.
   * */
  HistoricTaskInstanceQuery endOr();

}
