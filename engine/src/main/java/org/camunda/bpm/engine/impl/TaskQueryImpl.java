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
package org.camunda.bpm.engine.impl;

import static java.lang.Boolean.TRUE;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TaskQueryImpl extends AbstractQuery<TaskQuery, Task> implements TaskQuery {

  /*
   * When adding a property filter that supports Tasklist filters,
   * the following classes need to be modified:
   *
   * <ol>
   *   <li>
   *     Update the {@code TaskQuery} interface;
   *   </li>
   *   <li>
   *     Implement the new property filter and getters/setters in {@code TaskQueryImpl};
   *   </li>
   *   <li>
   *     Add the new property filter in the engine-rest {@code TaskQueryDto} class;
   *   </li>
   *   <li>
   *     Use the new filter in the engine-rest {@code TaskQueryDto#applyFilters} method.
   *     The method is used to provide Task filtering through the Rest API endpoint;
   *   </li>
   *   <li>
   *     Initialize the new property filter in the engine-rest
   *     {@code TaskQueryDto#fromQuery} method; The method is used to create a {@code TaskQueryDto}
   *     from a serialized ("saved") Task query. This is used in Tasklist filters;
   *   </li>
   *   <li>
   *     Add the property to the {@code JsonTaskQueryConverter} class, and make sure
   *     it is included in the {@code JsonTaskQueryConverter#toJsonObject} and
   *     {@code JsonTaskQueryConverter#toObject} methods. This is used to serialize/deserialize
   *     Task queries for Tasklist filter usage.
   *   </li>
   *   <li>
   *     Tests need to be added in: {@code TaskQueryTest} for Java API coverage,
   *     {@code TaskRestServiceQueryTest} for Rest API coverage and
   *     {@code FilterTaskQueryTest} for Tasklist filter coverage.
   *   </li>
   * </ol>
   */

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String[] taskIdIn;
  protected String name;
  protected String nameNotEqual;
  protected String nameLike;
  protected String nameNotLike;
  protected String description;
  protected String descriptionLike;
  protected Integer priority;
  protected Integer minPriority;
  protected Integer maxPriority;
  protected String assignee;
  protected String assigneeLike;
  protected Set<String> assigneeIn;
  protected Set<String> assigneeNotIn;
  protected String involvedUser;
  protected String owner;
  protected Boolean unassigned;
  protected Boolean assigned;
  protected boolean noDelegationState = false;
  protected DelegationState delegationState;
  protected String candidateUser;
  protected String candidateGroup;
  protected List<String> candidateGroups;
  protected Boolean withCandidateGroups;
  protected Boolean withoutCandidateGroups;
  protected Boolean withCandidateUsers;
  protected Boolean withoutCandidateUsers;
  protected Boolean includeAssignedTasks;
  protected String processInstanceId;
  protected String[] processInstanceIdIn;
  protected String executionId;
  protected String[] activityInstanceIdIn;
  protected Date createTime;
  protected Date createTimeBefore;
  protected Date createTimeAfter;
  protected Date updatedAfter;
  protected String key;
  protected String keyLike;
  protected String[] taskDefinitionKeys;
  protected String processDefinitionKey;
  protected String[] processDefinitionKeys;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String processDefinitionNameLike;
  protected String processInstanceBusinessKey;
  protected String[] processInstanceBusinessKeys;
  protected String processInstanceBusinessKeyLike;
  protected List<TaskQueryVariableValue> variables = new ArrayList<>();
  protected Date dueDate;
  protected Date dueBefore;
  protected Date dueAfter;
  protected Date followUpDate;
  protected Date followUpBefore;
  protected boolean followUpNullAccepted=false;
  protected Date followUpAfter;
  protected boolean excludeSubtasks = false;
  protected SuspensionState suspensionState;
  protected boolean initializeFormKeys = false;
  protected boolean taskNameCaseInsensitive = false;

  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;

  protected String parentTaskId;
  protected boolean isWithoutTenantId = false;
  protected boolean isWithoutDueDate = false;

  protected String[] tenantIds;
  // case management /////////////////////////////
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseDefinitionName;
  protected String caseDefinitionNameLike;
  protected String caseInstanceId;
  protected String caseInstanceBusinessKey;
  protected String caseInstanceBusinessKeyLike;
  protected String caseExecutionId;

  protected List<String> cachedCandidateGroups;
  protected Map<String, List<String>> cachedUserGroups;

  // or query /////////////////////////////
  protected List<TaskQueryImpl> queries = new ArrayList<>(Arrays.asList(this));
  protected boolean isOrQueryActive = false;

  public TaskQueryImpl() {
  }

  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public TaskQueryImpl taskId(String taskId) {
    ensureNotNull("Task id", taskId);
    this.taskId = taskId;
    return this;
  }

  @Override
  public TaskQueryImpl taskIdIn(String... taskIds) {
    ensureNotNull("taskIds", (Object[]) taskIds);
    this.taskIdIn = taskIds;
    return this;
  }

  @Override
  public TaskQueryImpl taskName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public TaskQueryImpl taskNameLike(String nameLike) {
    ensureNotNull("Task nameLike", nameLike);
    this.nameLike = nameLike;
    return this;
  }

  @Override
  public TaskQueryImpl taskDescription(String description) {
    ensureNotNull("Description", description);
    this.description = description;
    return this;
  }

  @Override
  public TaskQuery taskDescriptionLike(String descriptionLike) {
    ensureNotNull("Task descriptionLike", descriptionLike);
    this.descriptionLike = descriptionLike;
    return this;
  }

  @Override
  public TaskQuery taskPriority(Integer priority) {
    ensureNotNull("Priority", priority);
    this.priority = priority;
    return this;
  }

  @Override
  public TaskQuery taskMinPriority(Integer minPriority) {
    ensureNotNull("Min Priority", minPriority);
    this.minPriority = minPriority;
    return this;
  }

  @Override
  public TaskQuery taskMaxPriority(Integer maxPriority) {
    ensureNotNull("Max Priority", maxPriority);
    this.maxPriority = maxPriority;
    return this;
  }

  @Override
  public TaskQueryImpl taskAssignee(String assignee) {
    ensureNotNull("Assignee", assignee);
    this.assignee = assignee;
    expressions.remove("taskAssignee");
    return this;
  }

  @Override
  public TaskQuery taskAssigneeExpression(String assigneeExpression) {
    ensureNotNull("Assignee expression", assigneeExpression);
    expressions.put("taskAssignee", assigneeExpression);
    return this;
  }

  @Override
  public TaskQuery taskAssigneeLike(String assignee) {
    ensureNotNull("Assignee", assignee);
    this.assigneeLike = assignee;
    expressions.remove("taskAssigneeLike");
    return this;
  }

  @Override
  public TaskQuery taskAssigneeLikeExpression(String assigneeLikeExpression) {
    ensureNotNull("Assignee like expression", assigneeLikeExpression);
    expressions.put("taskAssigneeLike", assigneeLikeExpression);
    return this;
  }

  @Override
  public TaskQuery taskAssigneeIn(String... assignees) {
    ensureNotNull("Assignees", assignees);

    Set<String> assigneeIn = new HashSet<>(assignees.length);
    assigneeIn.addAll(Arrays.asList(assignees));

    this.assigneeIn = assigneeIn;
    expressions.remove("taskAssigneeIn");

    return this;
  }

  @Override
  public TaskQuery taskAssigneeNotIn(String... assignees) {
    ensureNotNull("Assignees", assignees);

    Set<String> assigneeNotIn = new HashSet<>(assignees.length);
    assigneeNotIn.addAll(Arrays.asList(assignees));

    this.assigneeNotIn = assigneeNotIn;
    expressions.remove("taskAssigneeNotIn");

    return this;
  }

  @Override
  public TaskQueryImpl taskOwner(String owner) {
    ensureNotNull("Owner", owner);
    this.owner = owner;
    expressions.remove("taskOwner");
    return this;
  }

  @Override
  public TaskQuery taskOwnerExpression(String ownerExpression) {
    ensureNotNull("Owner expression", ownerExpression);
    expressions.put("taskOwner", ownerExpression);
    return this;
  }

  /** @see {@link #taskUnassigned} */
  @Override
  @Deprecated
  public TaskQuery taskUnnassigned() {
    return taskUnassigned();
  }

  @Override
  public TaskQuery taskUnassigned() {
    this.unassigned = true;
    return this;
  }

  @Override
  public TaskQuery taskAssigned() {
    this.assigned = true;
    return this;
  }

  @Override
  public TaskQuery taskDelegationState(DelegationState delegationState) {
    if (delegationState == null) {
      this.noDelegationState = true;
    } else {
      this.delegationState = delegationState;
    }
    return this;
  }

  @Override
  public TaskQueryImpl taskCandidateUser(String candidateUser) {
    ensureNotNull("Candidate user", candidateUser);
    if (!isOrQueryActive) {
      if (candidateGroup != null || expressions.containsKey("taskCandidateGroup")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroup");
      }
      if (candidateGroups != null || expressions.containsKey("taskCandidateGroupIn")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroupIn");
      }
    }

    this.candidateUser = candidateUser;
    expressions.remove("taskCandidateUser");
    return this;
  }

  @Override
  public TaskQuery taskCandidateUserExpression(String candidateUserExpression) {
    ensureNotNull("Candidate user expression", candidateUserExpression);

    if (candidateGroup != null || expressions.containsKey("taskCandidateGroup")) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    if (candidateGroups != null || expressions.containsKey("taskCandidateGroupIn")) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroupIn");
    }

    expressions.put("taskCandidateUser", candidateUserExpression);
    return this;
  }

  @Override
  public TaskQueryImpl taskInvolvedUser(String involvedUser) {
    ensureNotNull("Involved user", involvedUser);
    this.involvedUser = involvedUser;
    expressions.remove("taskInvolvedUser");
    return this;
  }

  @Override
  public TaskQuery taskInvolvedUserExpression(String involvedUserExpression) {
    ensureNotNull("Involved user expression", involvedUserExpression);
    expressions.put("taskInvolvedUser", involvedUserExpression);
    return this;
  }

  @Override
  public TaskQuery withCandidateGroups() {
    ensureNotInOrQuery("withCandidateGroups()");
    this.withCandidateGroups = true;
    return this;
  }

  @Override
  public TaskQuery withoutCandidateGroups() {
    ensureNotInOrQuery("withoutCandidateGroups()");
    this.withoutCandidateGroups = true;
    return this;
  }

  @Override
  public TaskQuery withCandidateUsers() {
    ensureNotInOrQuery("withCandidateUsers()");
    this.withCandidateUsers = true;
    return this;
  }

  @Override
  public TaskQuery withoutCandidateUsers() {
    ensureNotInOrQuery("withoutCandidateUsers()");
    this.withoutCandidateUsers = true;
    return this;
  }

  @Override
  public TaskQueryImpl taskCandidateGroup(String candidateGroup) {
    ensureNotNull("Candidate group", candidateGroup);

    if (!isOrQueryActive) {
      if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroup and candidateUser");
      }
    }

    this.candidateGroup = candidateGroup;
    expressions.remove("taskCandidateGroup");
    return this;
  }

  @Override
  public TaskQuery taskCandidateGroupExpression(String candidateGroupExpression) {
    ensureNotNull("Candidate group expression", candidateGroupExpression);

    if (!isOrQueryActive) {
      if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroup and candidateUser");
      }
    }

    expressions.put("taskCandidateGroup", candidateGroupExpression);
    return this;
  }

  @Override
  public TaskQuery taskCandidateGroupIn(List<String> candidateGroups) {
    ensureNotEmpty("Candidate group list", candidateGroups);

    if (!isOrQueryActive) {
      if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroupIn and candidateUser");
      }
    }

    this.candidateGroups = candidateGroups;
    expressions.remove("taskCandidateGroupIn");
    return this;
  }

  @Override
  public TaskQuery taskCandidateGroupInExpression(String candidateGroupsExpression) {
    ensureNotEmpty("Candidate group list expression", candidateGroupsExpression);

    if (!isOrQueryActive) {
      if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroupIn and candidateUser");
      }
    }

    expressions.put("taskCandidateGroupIn", candidateGroupsExpression);
    return this;
  }

  @Override
  public TaskQuery includeAssignedTasks() {
    if (candidateUser == null && candidateGroup == null && candidateGroups == null && !isWithCandidateGroups() && !isWithoutCandidateGroups() && !isWithCandidateUsers() && !isWithoutCandidateUsers()
        && !expressions.containsKey("taskCandidateUser") && !expressions.containsKey("taskCandidateGroup")
        && !expressions.containsKey("taskCandidateGroupIn")) {
      throw new ProcessEngineException("Invalid query usage: candidateUser, candidateGroup, candidateGroupIn, withCandidateGroups, withoutCandidateGroups, withCandidateUsers, withoutCandidateUsers has to be called before 'includeAssignedTasks'.");
    }

    includeAssignedTasks = true;
    return this;
  }

  public TaskQuery includeAssignedTasksInternal() {
    includeAssignedTasks = true;
    return this;
  }

  @Override
  public TaskQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public TaskQuery processInstanceIdIn(String... processInstanceIds) {
    this.processInstanceIdIn = processInstanceIds;
    return this;
  }

  @Override
  public TaskQueryImpl processInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
    expressions.remove("processInstanceBusinessKey");
    return this;
  }

  @Override
  public TaskQuery processInstanceBusinessKeyExpression(String processInstanceBusinessKeyExpression) {
    ensureNotNull("processInstanceBusinessKey expression", processInstanceBusinessKeyExpression);
    expressions.put("processInstanceBusinessKey", processInstanceBusinessKeyExpression);
    return this;
  }

  @Override
  public TaskQuery processInstanceBusinessKeyIn(String... processInstanceBusinessKeys) {
    this.processInstanceBusinessKeys = processInstanceBusinessKeys;
    return this;
  }

  @Override
  public TaskQuery processInstanceBusinessKeyLike(String processInstanceBusinessKey) {
    this.processInstanceBusinessKeyLike = processInstanceBusinessKey;
    expressions.remove("processInstanceBusinessKeyLike");
  	return this;
  }

  @Override
  public TaskQuery processInstanceBusinessKeyLikeExpression(String processInstanceBusinessKeyLikeExpression) {
    ensureNotNull("processInstanceBusinessKeyLike expression", processInstanceBusinessKeyLikeExpression);
    expressions.put("processInstanceBusinessKeyLike", processInstanceBusinessKeyLikeExpression);
    return this;
  }

  @Override
  public TaskQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  @Override
  public TaskQuery activityInstanceIdIn(String... activityInstanceIds) {
    this.activityInstanceIdIn = activityInstanceIds;
    return this;
  }

  @Override
  public TaskQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);

    // The tenantIdIn filter can't be used in an AND query with
    // the withoutTenantId filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutTenantId)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both tenantIdIn and withoutTenantId filters.");
      }
    }

    this.tenantIds = tenantIds;
    return this;
  }

  @Override
  public TaskQuery withoutTenantId() {

    // The tenantIdIn filter can't be used in an AND query with
    // the withoutTenantId filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (tenantIds != null && tenantIds.length > 0) {
        throw new ProcessEngineException("Invalid query usage: cannot set both tenantIdIn and withoutTenantId filters.");
      }
    }

    this.isWithoutTenantId = true;
    return this;
  }

  @Override
  public TaskQueryImpl taskCreatedOn(Date createTime) {
    this.createTime = createTime;
    expressions.remove("taskCreatedOn");
    return this;
  }

  @Override
  public TaskQuery taskCreatedOnExpression(String createTimeExpression) {
    expressions.put("taskCreatedOn", createTimeExpression);
    return this;
  }

  @Override
  public TaskQuery taskCreatedBefore(Date before) {
    this.createTimeBefore = before;
    expressions.remove("taskCreatedBefore");
    return this;
  }

  @Override
  public TaskQuery taskCreatedBeforeExpression(String beforeExpression) {
    expressions.put("taskCreatedBefore", beforeExpression);
    return this;
  }

  @Override
  public TaskQuery taskCreatedAfter(Date after) {
    this.createTimeAfter = after;
    expressions.remove("taskCreatedAfter");
    return this;
  }

  @Override
  public TaskQuery taskCreatedAfterExpression(String afterExpression) {
    expressions.put("taskCreatedAfter", afterExpression);
    return this;
  }

  @Override
  public TaskQuery taskUpdatedAfter(Date after) {
    this.updatedAfter = after;
    expressions.remove("taskUpdatedAfter");
    return this;
  }

  @Override
  public TaskQuery taskUpdatedAfterExpression(String afterExpression) {
    expressions.put("taskUpdatedAfter", afterExpression);
    return this;
  }

  @Override
  public TaskQuery taskDefinitionKey(String key) {
    this.key = key;
    return this;
  }

  @Override
  public TaskQuery taskDefinitionKeyLike(String keyLike) {
    this.keyLike = keyLike;
    return this;
  }

  @Override
  public TaskQuery taskDefinitionKeyIn(String... taskDefinitionKeys) {
    this.taskDefinitionKeys = taskDefinitionKeys;
  	return this;
  }

  @Override
  public TaskQuery taskParentTaskId(String taskParentTaskId) {
    this.parentTaskId = taskParentTaskId;
    return this;
  }

  @Override
  public TaskQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull("caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  @Override
  public TaskQuery caseInstanceBusinessKey(String caseInstanceBusinessKey) {
    ensureNotNull("caseInstanceBusinessKey", caseInstanceBusinessKey);
    this.caseInstanceBusinessKey = caseInstanceBusinessKey;
    return this;
  }

  @Override
  public TaskQuery caseInstanceBusinessKeyLike(String caseInstanceBusinessKeyLike) {
    ensureNotNull("caseInstanceBusinessKeyLike", caseInstanceBusinessKeyLike);
    this.caseInstanceBusinessKeyLike = caseInstanceBusinessKeyLike;
    return this;
  }

  @Override
  public TaskQuery caseExecutionId(String caseExecutionId) {
    ensureNotNull("caseExecutionId", caseExecutionId);
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  @Override
  public TaskQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull("caseDefinitionId", caseDefinitionId);
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  @Override
  public TaskQuery caseDefinitionKey(String caseDefinitionKey) {
    ensureNotNull("caseDefinitionKey", caseDefinitionKey);
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  @Override
  public TaskQuery caseDefinitionName(String caseDefinitionName) {
    ensureNotNull("caseDefinitionName", caseDefinitionName);
    this.caseDefinitionName = caseDefinitionName;
    return this;
  }

  @Override
  public TaskQuery caseDefinitionNameLike(String caseDefinitionNameLike) {
    ensureNotNull("caseDefinitionNameLike", caseDefinitionNameLike);
    this.caseDefinitionNameLike = caseDefinitionNameLike;
    return this;
  }

  @Override
  public TaskQuery taskVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, true, false);
    return this;
  }

  @Override
  public TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, true, false);
    return this;
  }

  @Override
  public TaskQuery taskVariableValueLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE, true, false);
  	return this;
  }

  @Override
  public TaskQuery taskVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN, true, false);
  	return this;
  }

  @Override
  public TaskQuery taskVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL, true, false);
  	return this;
  }

  @Override
  public TaskQuery taskVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN, true, false);
  	return this;
  }

  @Override
  public TaskQuery taskVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL, true, false);
  	return this;
  }

  @Override
  public TaskQuery processVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, false, true);
    return this;
  }

  @Override
  public TaskQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, false, true);
    return this;
  }

  @Override
  public TaskQuery processVariableValueLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE, false, true);
  	return this;
  }

  @Override
  public TaskQuery processVariableValueNotLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_LIKE, false, true);
    return this;
  }

  @Override
  public TaskQuery processVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN, false, true);
  	return this;
  }

  @Override
  public TaskQuery processVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL, false, true);
  	return this;
  }

  @Override
  public TaskQuery processVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN, false, true);
  	return this;
  }

  @Override
  public TaskQuery processVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL, false, true);
  	return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueNotLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_LIKE, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN, false, false);
    return this;
  }

  @Override
  public TaskQuery caseInstanceVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL, false, false);
    return this;
  }

  @Override
  public TaskQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public TaskQuery processDefinitionKeyIn(String... processDefinitionKeys) {
    this.processDefinitionKeys = processDefinitionKeys;
    return this;
  }

  @Override
  public TaskQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public TaskQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  @Override
  public TaskQuery processDefinitionNameLike(String processDefinitionName) {
    this.processDefinitionNameLike = processDefinitionName;
  	return this;
  }

  @Override
  public TaskQuery dueDate(Date dueDate) {
    // The dueDate filter can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both dueDate and withoutDueDate filters.");
      }
    }

    this.dueDate = dueDate;
    expressions.remove("dueDate");
    return this;
  }

  @Override
  public TaskQuery dueDateExpression(String dueDateExpression) {
    // The dueDateExpression filter can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both dueDateExpression and withoutDueDate filters.");
      }
    }

    expressions.put("dueDate", dueDateExpression);
    return this;
  }

  @Override
  public TaskQuery dueBefore(Date dueBefore) {
    // The dueBefore filter can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both dueBefore and withoutDueDate filters.");
      }
    }

    this.dueBefore = dueBefore;
    expressions.remove("dueBefore");
    return this;
  }

  @Override
  public TaskQuery dueBeforeExpression(String dueDate) {
    // The dueBeforeExpression filter can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both dueBeforeExpression and withoutDueDate filters.");
      }
    }

    expressions.put("dueBefore", dueDate);
    return this;
  }

  @Override
  public TaskQuery dueAfter(Date dueAfter) {
    // The dueAfter filter can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both dueAfter and withoutDueDate filters.");
      }
    }

    this.dueAfter = dueAfter;
    expressions.remove("dueAfter");
    return this;
  }

  @Override
  public TaskQuery dueAfterExpression(String dueDateExpression) {
    // The dueAfterExpression filter can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both dueAfterExpression and withoutDueDate filters.");
      }
    }

    expressions.put("dueAfter", dueDateExpression);
    return this;
  }

  @Override
  public TaskQuery withoutDueDate() {
    // The due date filters can't be used in an AND query with
    // the withoutDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (dueAfter != null || dueBefore != null || dueDate != null || expressions.containsKey("dueDate")
          || expressions.containsKey("dueBefore") || expressions.containsKey("dueAfter")) {
        throw new ProcessEngineException("Invalid query usage: cannot set both due date (equal to, before, or after) and withoutDueDate filters.");
      }
    }

    this.isWithoutDueDate = true;
    return this;
  }

  @Override
  public TaskQuery followUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
    expressions.remove("followUpDate");
    return this;
  }

  @Override
  public TaskQuery followUpDateExpression(String followUpDateExpression) {
    expressions.put("followUpDate", followUpDateExpression);
    return this;
  }

  @Override
  public TaskQuery followUpBefore(Date followUpBefore) {
    this.followUpBefore = followUpBefore;
    this.followUpNullAccepted = false;
    expressions.remove("followUpBefore");
    return this;
  }

  @Override
  public TaskQuery followUpBeforeExpression(String followUpBeforeExpression) {
    this.followUpNullAccepted = false;
    expressions.put("followUpBefore", followUpBeforeExpression);
    return this;
  }

  @Override
  public TaskQuery followUpBeforeOrNotExistent(Date followUpDate) {
    this.followUpBefore = followUpDate;
    this.followUpNullAccepted = true;
    expressions.remove("followUpBeforeOrNotExistent");
    return this;
  }

  @Override
  public TaskQuery followUpBeforeOrNotExistentExpression(String followUpDateExpression) {
    expressions.put("followUpBeforeOrNotExistent", followUpDateExpression);
    this.followUpNullAccepted = true;
    return this;
  }

  public void setFollowUpNullAccepted(boolean followUpNullAccepted) {
    this.followUpNullAccepted = followUpNullAccepted;
  }

  @Override
  public TaskQuery followUpAfter(Date followUpAfter) {
    this.followUpAfter = followUpAfter;
    expressions.remove("followUpAfter");
    return this;
  }

  @Override
  public TaskQuery followUpAfterExpression(String followUpAfterExpression) {
    expressions.put("followUpAfter", followUpAfterExpression);
    return this;
  }

  @Override
  public TaskQuery excludeSubtasks() {
    this.excludeSubtasks = true;
    return this;
  }

  @Override
  public TaskQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  @Override
  public TaskQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  @Override
  public TaskQuery initializeFormKeys() {
    ensureNotInOrQuery("initializeFormKeys()");
    this.initializeFormKeys = true;
    return this;
  }

  public TaskQuery taskNameCaseInsensitive() {
    this.taskNameCaseInsensitive = true;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(minPriority, priority, maxPriority)
      || CompareUtil.areNotInAscendingOrder(dueAfter, dueDate, dueBefore)
      || CompareUtil.areNotInAscendingOrder(followUpAfter, followUpDate, followUpBefore)
      || CompareUtil.areNotInAscendingOrder(createTimeAfter, createTime, createTimeBefore)
      || CompareUtil.elementIsNotContainedInArray(key, taskDefinitionKeys)
      || CompareUtil.elementIsNotContainedInArray(processDefinitionKey, processDefinitionKeys)
      || CompareUtil.elementIsNotContainedInArray(processInstanceBusinessKey, processInstanceBusinessKeys);
  }

  public List<String> getCandidateGroups() {
    if (cachedCandidateGroups != null) {
      return cachedCandidateGroups;
    }

    if (candidateGroup != null && candidateGroups != null) {
      cachedCandidateGroups = new ArrayList<>(candidateGroups);
      if (!isOrQueryActive) {
        // get intersection of candidateGroups and candidateGroup
        cachedCandidateGroups.retainAll(Collections.singletonList(candidateGroup));
      } else {
        // get union of candidateGroups and candidateGroup
        if (!candidateGroups.contains(candidateGroup)) {
          cachedCandidateGroups.add(candidateGroup);
        }
      }
    } else if (candidateGroup != null) {
      cachedCandidateGroups = Collections.singletonList(candidateGroup);
    } else if (candidateGroups != null) {
      cachedCandidateGroups = candidateGroups;
    }

    if (candidateUser != null) {
      List<String> groupsForCandidateUser = getGroupsForCandidateUser(candidateUser);

      if (cachedCandidateGroups == null) {
        cachedCandidateGroups = groupsForCandidateUser;
      } else {
        for (String group : groupsForCandidateUser) {
          if (!cachedCandidateGroups.contains(group)) {
            cachedCandidateGroups.add(group);
          }
        }
      }
    }

    return cachedCandidateGroups;
  }

  public Boolean isWithCandidateGroups() {
    if (withCandidateGroups == null) {
      return false;
    } else {
      return withCandidateGroups;
    }
  }

  public Boolean isWithCandidateUsers() {
    if (withCandidateUsers == null) {
      return false;
    } else {
      return withCandidateUsers;
    }
  }

  public Boolean isWithCandidateGroupsInternal() {
    return withCandidateGroups;
  }

  public Boolean isWithoutCandidateGroups() {
    if (withoutCandidateGroups == null) {
      return false;
    } else {
      return withoutCandidateGroups;
    }
  }

  public Boolean isWithoutCandidateUsers() {
    if (withoutCandidateUsers == null) {
      return false;
    } else {
      return withoutCandidateUsers;
    }
  }

  public Boolean isWithoutCandidateGroupsInternal() {
    return withoutCandidateGroups;
  }

  public List<String> getCandidateGroupsInternal() {
    return candidateGroups;
  }

  protected List<String> getGroupsForCandidateUser(String candidateUser) {
    Map<String, List<String>> cachedUserGroups = getCachedUserGroups();
    if (cachedUserGroups.containsKey(candidateUser)) {
      return cachedUserGroups.get(candidateUser);
    }

    List<Group> groups = Context.getCommandContext()
        .getReadOnlyIdentityProvider()
        .createGroupQuery()
        .groupMember(candidateUser)
        .list();

    List<String> groupIds = new ArrayList<>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }

    cachedUserGroups.put(candidateUser, groupIds);

    return groupIds;
  }

  protected Map<String, List<String>> getCachedUserGroups() {
    // store and retrieve cached user groups always from the first query
    if (queries.get(0).cachedUserGroups == null) {
      queries.get(0).cachedUserGroups = new HashMap<>();
    }
    return queries.get(0).cachedUserGroups;
  }

  protected void ensureOrExpressionsEvaluated() {
    // skips first query as it has already been evaluated
    for (int i = 1; i < queries.size(); i++) {
      queries.get(i).validate();
      queries.get(i).evaluateExpressions();
    }
  }

  protected void ensureVariablesInitialized() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
    String dbType = processEngineConfiguration.getDatabaseType();
    for(QueryVariableValue var : variables) {
      var.initialize(variableSerializers, dbType);
    }

    if (!queries.isEmpty()) {
      for (TaskQueryImpl orQuery: queries) {
        for (QueryVariableValue var : orQuery.variables) {
          var.initialize(variableSerializers, dbType);
        }
      }
    }
  }

  protected void ensureNotInOrQuery(String methodName) {
    if (isOrQueryActive) {
      throw new ProcessEngineException(String.format("Invalid query usage: cannot set %s within 'or' query", methodName));
    }
  }

  public void addVariable(String name, Object value, QueryOperator operator, boolean isTaskVariable, boolean isProcessInstanceVariable) {
    ensureNotNull("name", name);

    if(value == null || isBoolean(value)) {
      // Null-values and booleans can only be used in EQUALS and NOT_EQUALS
      switch(operator) {
      case GREATER_THAN:
        throw new ProcessEngineException("Booleans and null cannot be used in 'greater than' condition");
      case LESS_THAN:
        throw new ProcessEngineException("Booleans and null cannot be used in 'less than' condition");
      case GREATER_THAN_OR_EQUAL:
        throw new ProcessEngineException("Booleans and null cannot be used in 'greater than or equal' condition");
      case LESS_THAN_OR_EQUAL:
        throw new ProcessEngineException("Booleans and null cannot be used in 'less than or equal' condition");
      case LIKE:
        throw new ProcessEngineException("Booleans and null cannot be used in 'like' condition");
      case NOT_LIKE:
        throw new ProcessEngineException("Booleans and null cannot be used in 'not like' condition");
      default:
        break;
      }
    }

    boolean shouldMatchVariableValuesIgnoreCase = TRUE.equals(variableValuesIgnoreCase) && value != null && String.class.isAssignableFrom(value.getClass());
    addVariable(new TaskQueryVariableValue(name, value, operator, isTaskVariable, isProcessInstanceVariable, TRUE.equals(variableNamesIgnoreCase), shouldMatchVariableValuesIgnoreCase));
  }

  protected void addVariable(TaskQueryVariableValue taskQueryVariableValue) {
    variables.add(taskQueryVariableValue);
  }

  private boolean isBoolean(Object value) {
  	if (value == null) {
  	  return false;
  	}
  	return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
	}

  //ordering ////////////////////////////////////////////////////////////////

  @Override
  public TaskQuery orderByTaskId() {
    ensureNotInOrQuery("orderByTaskId()");
    return orderBy(TaskQueryProperty.TASK_ID);
  }

  @Override
  public TaskQuery orderByTaskName() {
    ensureNotInOrQuery("orderByTaskName()");
    return orderBy(TaskQueryProperty.NAME);
  }

  @Override
  public TaskQuery orderByTaskNameCaseInsensitive() {
    ensureNotInOrQuery("orderByTaskNameCaseInsensitive()");
    taskNameCaseInsensitive();
    return orderBy(TaskQueryProperty.NAME_CASE_INSENSITIVE);
  }

  @Override
  public TaskQuery orderByTaskDescription() {
    ensureNotInOrQuery("orderByTaskDescription()");
    return orderBy(TaskQueryProperty.DESCRIPTION);
  }

  @Override
  public TaskQuery orderByTaskPriority() {
    ensureNotInOrQuery("orderByTaskPriority()");
    return orderBy(TaskQueryProperty.PRIORITY);
  }

  @Override
  public TaskQuery orderByProcessInstanceId() {
    ensureNotInOrQuery("orderByProcessInstanceId()");
    return orderBy(TaskQueryProperty.PROCESS_INSTANCE_ID);
  }

  @Override
  public TaskQuery orderByCaseInstanceId() {
    ensureNotInOrQuery("orderByCaseInstanceId()");
    return orderBy(TaskQueryProperty.CASE_INSTANCE_ID);
  }

  @Override
  public TaskQuery orderByExecutionId() {
    ensureNotInOrQuery("orderByExecutionId()");
    return orderBy(TaskQueryProperty.EXECUTION_ID);
  }

  @Override
  public TaskQuery orderByTenantId() {
    ensureNotInOrQuery("orderByTenantId()");
    return orderBy(TaskQueryProperty.TENANT_ID);
  }

  @Override
  public TaskQuery orderByCaseExecutionId() {
    ensureNotInOrQuery("orderByCaseExecutionId()");
    return orderBy(TaskQueryProperty.CASE_EXECUTION_ID);
  }

  @Override
  public TaskQuery orderByTaskAssignee() {
    ensureNotInOrQuery("orderByTaskAssignee()");
    return orderBy(TaskQueryProperty.ASSIGNEE);
  }

  @Override
  public TaskQuery orderByTaskCreateTime() {
    ensureNotInOrQuery("orderByTaskCreateTime()");
    return orderBy(TaskQueryProperty.CREATE_TIME);
  }

  @Override
  public TaskQuery orderByLastUpdated() {
    ensureNotInOrQuery("orderByLastUpdated()");
    return orderBy(TaskQueryProperty.LAST_UPDATED);
  }

  @Override
  public TaskQuery orderByDueDate() {
    ensureNotInOrQuery("orderByDueDate()");
    return orderBy(TaskQueryProperty.DUE_DATE);
  }

  @Override
  public TaskQuery orderByFollowUpDate() {
    ensureNotInOrQuery("orderByFollowUpDate()");
    return orderBy(TaskQueryProperty.FOLLOW_UP_DATE);
  }

  @Override
  public TaskQuery orderByProcessVariable(String variableName, ValueType valueType) {
    ensureNotInOrQuery("orderByProcessVariable()");
    ensureNotNull("variableName", variableName);
    ensureNotNull("valueType", valueType);

    orderBy(VariableOrderProperty.forProcessInstanceVariable(variableName, valueType));
    return this;
  }

  @Override
  public TaskQuery orderByExecutionVariable(String variableName, ValueType valueType) {
    ensureNotInOrQuery("orderByExecutionVariable()");
    ensureNotNull("variableName", variableName);
    ensureNotNull("valueType", valueType);

    orderBy(VariableOrderProperty.forExecutionVariable(variableName, valueType));
    return this;
  }

  @Override
  public TaskQuery orderByTaskVariable(String variableName, ValueType valueType) {
    ensureNotInOrQuery("orderByTaskVariable()");
    ensureNotNull("variableName", variableName);
    ensureNotNull("valueType", valueType);

    orderBy(VariableOrderProperty.forTaskVariable(variableName, valueType));
    return this;
  }

  @Override
  public TaskQuery orderByCaseExecutionVariable(String variableName, ValueType valueType) {
    ensureNotInOrQuery("orderByCaseExecutionVariable()");
    ensureNotNull("variableName", variableName);
    ensureNotNull("valueType", valueType);

    orderBy(VariableOrderProperty.forCaseExecutionVariable(variableName, valueType));
    return this;
  }

  @Override
  public TaskQuery orderByCaseInstanceVariable(String variableName, ValueType valueType) {
    ensureNotInOrQuery("orderByCaseInstanceVariable()");
    ensureNotNull("variableName", variableName);
    ensureNotNull("valueType", valueType);

    orderBy(VariableOrderProperty.forCaseInstanceVariable(variableName, valueType));
    return this;
  }

  //results ////////////////////////////////////////////////////////////////

  @Override
  public List<Task> executeList(CommandContext commandContext, Page page) {
    ensureOrExpressionsEvaluated();
    ensureVariablesInitialized();
    checkQueryOk();

    resetCachedCandidateGroups();

    //check if candidateGroup and candidateGroups intersect
    if (getCandidateGroup() != null && getCandidateGroupsInternal() != null && getCandidateGroups().isEmpty()) {
      return Collections.emptyList();
    }

    decideAuthorizationJoinType(commandContext);

    List<Task> taskList = commandContext
      .getTaskManager()
      .findTasksByQueryCriteria(this);

    if (initializeFormKeys) {
      for (Task task : taskList) {
        // initialize the form keys of the tasks
        ((TaskEntity) task).initializeFormKey();
      }
    }

    return taskList;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    ensureOrExpressionsEvaluated();
    ensureVariablesInitialized();
    checkQueryOk();

    resetCachedCandidateGroups();

    //check if candidateGroup and candidateGroups intersect
    if (getCandidateGroup() != null && getCandidateGroupsInternal() != null && getCandidateGroups().isEmpty()) {
      return 0;
    }

    decideAuthorizationJoinType(commandContext);

    return commandContext
      .getTaskManager()
      .findTaskCountByQueryCriteria(this);
  }

  protected void decideAuthorizationJoinType(CommandContext commandContext) {
    boolean cmmnEnabled = commandContext.getProcessEngineConfiguration().isCmmnEnabled();
    authCheck.setUseLeftJoin(cmmnEnabled);
  }

  protected void resetCachedCandidateGroups() {
    cachedCandidateGroups = null;
    for (int i = 1; i < queries.size(); i++) {
      queries.get(i).cachedCandidateGroups = null;
    }
  }

  //getters ////////////////////////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public String getNameNotEqual() {
    return nameNotEqual;
  }

  public String getNameLike() {
    return nameLike;
  }

  public String getNameNotLike() {
    return nameNotLike;
  }

  public String getAssignee() {
    return assignee;
  }

  public String getAssigneeLike() {
    return assigneeLike;
  }

  public Set<String> getAssigneeIn() {
    return assigneeIn;
  }

  public Set<String> getAssigneeNotIn() {
    return assigneeNotIn;
  }

  public String getInvolvedUser() {
    return involvedUser;
  }

  public String getOwner() {
    return owner;
  }

  public Boolean isAssigned() {
    if (assigned == null) {
      return false;
    } else {
      return assigned;
    }
  }

  public Boolean isAssignedInternal() {
    return assigned;
  }

  public boolean isUnassigned() {
    if (unassigned == null) {
      return false;
    }
    else {
      return unassigned;
    }
  }

  public Boolean isUnassignedInternal() {
    return unassigned;
  }

  public DelegationState getDelegationState() {
    return delegationState;
  }

  public boolean isNoDelegationState() {
    return noDelegationState;
  }

  public String getDelegationStateString() {
    return (delegationState!=null ? delegationState.toString() : null);
  }

  public String getCandidateUser() {
    return candidateUser;
  }

  public String getCandidateGroup() {
    return candidateGroup;
  }

  public boolean isIncludeAssignedTasks() {
    return includeAssignedTasks != null ? includeAssignedTasks : false;
  }

  public Boolean isIncludeAssignedTasksInternal() {
    return includeAssignedTasks;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String[] getProcessInstanceIdIn() {
    return processInstanceIdIn;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String[] getActivityInstanceIdIn() {
    return activityInstanceIdIn;
  }

  public String[] getTenantIds() {
    return tenantIds;
  }

  public String getTaskId() {
    return taskId;
  }

  public String[] getTaskIdIn() {
	return taskIdIn;
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptionLike() {
    return descriptionLike;
  }

  public Integer getPriority() {
    return priority;
  }

  public Integer getMinPriority() {
    return minPriority;
  }

  public Integer getMaxPriority() {
    return maxPriority;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getCreateTimeBefore() {
    return createTimeBefore;
  }

  public Date getCreateTimeAfter() {
    return createTimeAfter;
  }

  public Date getUpdatedAfter() {
    return updatedAfter;
  }

  public String getKey() {
    return key;
  }

  public String[] getKeys() {
    return taskDefinitionKeys;
  }

  public String getKeyLike() {
    return keyLike;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String[] getProcessDefinitionKeys() {
    return processDefinitionKeys;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public String getProcessDefinitionNameLike() {
    return processDefinitionNameLike;
  }

  public String getProcessInstanceBusinessKey() {
    return processInstanceBusinessKey;
  }

  public String[] getProcessInstanceBusinessKeys() {
    return processInstanceBusinessKeys;
  }

  public String getProcessInstanceBusinessKeyLike() {
    return processInstanceBusinessKeyLike;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public Date getDueBefore() {
    return dueBefore;
  }

  public Date getDueAfter() {
    return dueAfter;
  }

  public Date getFollowUpDate() {
    return followUpDate;
  }

  public Date getFollowUpBefore() {
    return followUpBefore;
  }

  public Date getFollowUpAfter() {
    return followUpAfter;
  }

  public boolean isExcludeSubtasks() {
    return excludeSubtasks;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseInstanceBusinessKey() {
    return caseInstanceBusinessKey;
  }

  public String getCaseInstanceBusinessKeyLike() {
    return caseInstanceBusinessKeyLike;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionName() {
    return caseDefinitionName;
  }

  public String getCaseDefinitionNameLike() {
    return caseDefinitionNameLike;
  }

  public boolean isInitializeFormKeys() {
    return initializeFormKeys;
  }

  public boolean isTaskNameCaseInsensitive() {
    return taskNameCaseInsensitive;
  }

  public boolean isWithoutTenantId() {
    return isWithoutTenantId;
  }

  public boolean isWithoutDueDate() {
    return isWithoutDueDate;
  }

  public String[] getTaskDefinitionKeys() {
    return taskDefinitionKeys;
  }

  public boolean getIsTenantIdSet() {
    return isWithoutTenantId;
  }

  public Boolean isVariableNamesIgnoreCase() {
    return variableNamesIgnoreCase;
  }

  public Boolean isVariableValuesIgnoreCase() {
    return variableValuesIgnoreCase;
  }

  public List<TaskQueryImpl> getQueries() {
    return queries;
  }

  public boolean isOrQueryActive() {
    return isOrQueryActive;
  }

  public void addOrQuery(TaskQueryImpl orQuery) {
    orQuery.isOrQueryActive = true;
    this.queries.add(orQuery);
  }

  public void setOrQueryActive() {
    isOrQueryActive = true;
  }

  @Override
  public TaskQuery extend(TaskQuery extending) {
    TaskQueryImpl extendingQuery = (TaskQueryImpl) extending;
    TaskQueryImpl extendedQuery = new TaskQueryImpl();

    // only add the base query's validators to the new query;
    // this is because the extending query's validators may not be applicable to the base
    // query and should therefore be executed before extending the query
    extendedQuery.validators = new HashSet<>(validators);

    if (extendingQuery.getName() != null) {
      extendedQuery.taskName(extendingQuery.getName());
    }
    else if (this.getName() != null) {
      extendedQuery.taskName(this.getName());
    }

    if (extendingQuery.getNameLike() != null) {
      extendedQuery.taskNameLike(extendingQuery.getNameLike());
    }
    else if (this.getNameLike() != null) {
      extendedQuery.taskNameLike(this.getNameLike());
    }

    if (extendingQuery.getNameNotEqual() != null) {
      extendedQuery.taskNameNotEqual(extendingQuery.getNameNotEqual());
    }
    else if (this.getNameNotEqual() != null) {
      extendedQuery.taskNameNotEqual(this.getNameNotEqual());
    }

    if (extendingQuery.getNameNotLike() != null) {
      extendedQuery.taskNameNotLike(extendingQuery.getNameNotLike());
    }
    else if (this.getNameNotLike() != null) {
      extendedQuery.taskNameNotLike(this.getNameNotLike());
    }

    if (extendingQuery.getAssignee() != null) {
      extendedQuery.taskAssignee(extendingQuery.getAssignee());
    }
    else if (this.getAssignee() != null) {
      extendedQuery.taskAssignee(this.getAssignee());
    }

    if (extendingQuery.getAssigneeLike() != null) {
      extendedQuery.taskAssigneeLike(extendingQuery.getAssigneeLike());
    }
    else if (this.getAssigneeLike() != null) {
      extendedQuery.taskAssigneeLike(this.getAssigneeLike());
    }

    if (extendingQuery.getAssigneeIn() != null) {
      extendedQuery.taskAssigneeIn(extendingQuery
          .getAssigneeIn()
          .toArray(new String[extendingQuery.getAssigneeIn().size()]));
    }
    else if (this.getAssigneeIn() != null) {
      extendedQuery.taskAssigneeIn(this.getAssigneeIn()
          .toArray(new String[this.getAssigneeIn().size()]));
    }
    if (extendingQuery.getAssigneeNotIn() != null) {
      extendedQuery.taskAssigneeNotIn(extendingQuery
          .getAssigneeNotIn()
          .toArray(new String[extendingQuery.getAssigneeNotIn().size()]));
    }
    else if (this.getAssigneeNotIn() != null) {
      extendedQuery.taskAssigneeNotIn(this.getAssigneeNotIn()
          .toArray(new String[this.getAssigneeNotIn().size()]));
    }

    if (extendingQuery.getInvolvedUser() != null) {
      extendedQuery.taskInvolvedUser(extendingQuery.getInvolvedUser());
    }
    else if (this.getInvolvedUser() != null) {
      extendedQuery.taskInvolvedUser(this.getInvolvedUser());
    }

    if (extendingQuery.getOwner() != null) {
      extendedQuery.taskOwner(extendingQuery.getOwner());
    }
    else if (this.getOwner() != null) {
      extendedQuery.taskOwner(this.getOwner());
    }

    if (extendingQuery.isAssigned() || this.isAssigned()) {
      extendedQuery.taskAssigned();
    }

    if (extendingQuery.isUnassigned() || this.isUnassigned()) {
      extendedQuery.taskUnassigned();
    }

    if (extendingQuery.getDelegationState() != null) {
      extendedQuery.taskDelegationState(extendingQuery.getDelegationState());
    }
    else if (this.getDelegationState() != null) {
      extendedQuery.taskDelegationState(this.getDelegationState());
    }

    if (extendingQuery.getCandidateUser() != null) {
      extendedQuery.taskCandidateUser(extendingQuery.getCandidateUser());
    }
    else if (this.getCandidateUser() != null) {
      extendedQuery.taskCandidateUser(this.getCandidateUser());
    }

    if (extendingQuery.getCandidateGroup() != null) {
      extendedQuery.taskCandidateGroup(extendingQuery.getCandidateGroup());
    }
    else if (this.getCandidateGroup() != null) {
      extendedQuery.taskCandidateGroup(this.getCandidateGroup());
    }

    if (extendingQuery.isWithCandidateGroups() || this.isWithCandidateGroups()) {
      extendedQuery.withCandidateGroups();
    }

    if (extendingQuery.isWithCandidateUsers() || this.isWithCandidateUsers()) {
      extendedQuery.withCandidateUsers();
    }

    if (extendingQuery.isWithoutCandidateGroups() || this.isWithoutCandidateGroups()) {
      extendedQuery.withoutCandidateGroups();
    }

    if (extendingQuery.isWithoutCandidateUsers() || this.isWithoutCandidateUsers()) {
      extendedQuery.withoutCandidateUsers();
    }

    if (extendingQuery.getCandidateGroupsInternal() != null) {
      extendedQuery.taskCandidateGroupIn(extendingQuery.getCandidateGroupsInternal());
    }
    else if (this.getCandidateGroupsInternal() != null) {
      extendedQuery.taskCandidateGroupIn(this.getCandidateGroupsInternal());
    }

    if (extendingQuery.getProcessInstanceId() != null) {
      extendedQuery.processInstanceId(extendingQuery.getProcessInstanceId());
    }
    else if (this.getProcessInstanceId() != null) {
      extendedQuery.processInstanceId(this.getProcessInstanceId());
    }

    if (extendingQuery.getProcessInstanceIdIn() != null) {
      extendedQuery.processInstanceIdIn(extendingQuery.getProcessInstanceIdIn());
    } else if (this.processInstanceIdIn() != null) {
      extendedQuery.processInstanceIdIn(this.getProcessInstanceIdIn());
    }

    if (extendingQuery.getExecutionId() != null) {
      extendedQuery.executionId(extendingQuery.getExecutionId());
    }
    else if (this.getExecutionId() != null) {
      extendedQuery.executionId(this.getExecutionId());
    }

    if (extendingQuery.getActivityInstanceIdIn() != null) {
      extendedQuery.activityInstanceIdIn(extendingQuery.getActivityInstanceIdIn());
    }
    else if (this.getActivityInstanceIdIn() != null) {
      extendedQuery.activityInstanceIdIn(this.getActivityInstanceIdIn());
    }

    if (extendingQuery.getTaskId() != null) {
      extendedQuery.taskId(extendingQuery.getTaskId());
    }
    else if (this.getTaskId() != null) {
      extendedQuery.taskId(this.getTaskId());
    }

    if (extendingQuery.getTaskIdIn() != null) {
      extendedQuery.taskIdIn(extendingQuery.getTaskIdIn());
    } else if (this.getTaskIdIn() != null) {
      extendedQuery.taskIdIn(this.getTaskIdIn());
    }

    if (extendingQuery.getDescription() != null) {
      extendedQuery.taskDescription(extendingQuery.getDescription());
    }
    else if (this.getDescription() != null) {
      extendedQuery.taskDescription(this.getDescription());
    }

    if (extendingQuery.getDescriptionLike() != null) {
      extendedQuery.taskDescriptionLike(extendingQuery.getDescriptionLike());
    }
    else if (this.getDescriptionLike() != null) {
      extendedQuery.taskDescriptionLike(this.getDescriptionLike());
    }

    if (extendingQuery.getPriority() != null) {
      extendedQuery.taskPriority(extendingQuery.getPriority());
    }
    else if (this.getPriority() != null) {
      extendedQuery.taskPriority(this.getPriority());
    }

    if (extendingQuery.getMinPriority() != null) {
      extendedQuery.taskMinPriority(extendingQuery.getMinPriority());
    }
    else if (this.getMinPriority() != null) {
      extendedQuery.taskMinPriority(this.getMinPriority());
    }

    if (extendingQuery.getMaxPriority() != null) {
      extendedQuery.taskMaxPriority(extendingQuery.getMaxPriority());
    }
    else if (this.getMaxPriority() != null) {
      extendedQuery.taskMaxPriority(this.getMaxPriority());
    }

    if (extendingQuery.getCreateTime() != null) {
      extendedQuery.taskCreatedOn(extendingQuery.getCreateTime());
    }
    else if (this.getCreateTime() != null) {
      extendedQuery.taskCreatedOn(this.getCreateTime());
    }

    if (extendingQuery.getCreateTimeBefore() != null) {
      extendedQuery.taskCreatedBefore(extendingQuery.getCreateTimeBefore());
    }
    else if (this.getCreateTimeBefore() != null) {
      extendedQuery.taskCreatedBefore(this.getCreateTimeBefore());
    }

    if (extendingQuery.getCreateTimeAfter() != null) {
      extendedQuery.taskCreatedAfter(extendingQuery.getCreateTimeAfter());
    }
    else if (this.getCreateTimeAfter() != null) {
      extendedQuery.taskCreatedAfter(this.getCreateTimeAfter());
    }

    if(extendingQuery.getUpdatedAfter() != null) {
      extendedQuery.taskUpdatedAfter(extendingQuery.getUpdatedAfter());
    }
    else if(this.getUpdatedAfter() != null) {
      extendedQuery.taskUpdatedAfter(this.getUpdatedAfter());
    }

    if (extendingQuery.getKey() != null) {
      extendedQuery.taskDefinitionKey(extendingQuery.getKey());
    }
    else if (this.getKey() != null) {
      extendedQuery.taskDefinitionKey(this.getKey());
    }

    if (extendingQuery.getKeyLike() != null) {
      extendedQuery.taskDefinitionKeyLike(extendingQuery.getKeyLike());
    }
    else if (this.getKeyLike() != null) {
      extendedQuery.taskDefinitionKeyLike(this.getKeyLike());
    }

    if (extendingQuery.getKeys() != null) {
      extendedQuery.taskDefinitionKeyIn(extendingQuery.getKeys());
    }
    else if (this.getKeys() != null) {
      extendedQuery.taskDefinitionKeyIn(this.getKeys());
    }

    if (extendingQuery.getParentTaskId() != null) {
      extendedQuery.taskParentTaskId(extendingQuery.getParentTaskId());
    }
    else if (this.getParentTaskId() != null) {
      extendedQuery.taskParentTaskId(this.getParentTaskId());
    }

    if (extendingQuery.getProcessDefinitionKey() != null) {
      extendedQuery.processDefinitionKey(extendingQuery.getProcessDefinitionKey());
    }
    else if (this.getProcessDefinitionKey() != null) {
      extendedQuery.processDefinitionKey(this.getProcessDefinitionKey());
    }

    if (extendingQuery.getProcessDefinitionKeys() != null) {
      extendedQuery.processDefinitionKeyIn(extendingQuery.getProcessDefinitionKeys());
    }
    else if (this.getProcessDefinitionKeys() != null) {
      extendedQuery.processDefinitionKeyIn(this.getProcessDefinitionKeys());
    }

    if (extendingQuery.getProcessDefinitionId() != null) {
      extendedQuery.processDefinitionId(extendingQuery.getProcessDefinitionId());
    }
    else if (this.getProcessDefinitionId() != null) {
      extendedQuery.processDefinitionId(this.getProcessDefinitionId());
    }

    if (extendingQuery.getProcessDefinitionName() != null) {
      extendedQuery.processDefinitionName(extendingQuery.getProcessDefinitionName());
    }
    else if (this.getProcessDefinitionName() != null) {
      extendedQuery.processDefinitionName(this.getProcessDefinitionName());
    }

    if (extendingQuery.getProcessDefinitionNameLike() != null) {
      extendedQuery.processDefinitionNameLike(extendingQuery.getProcessDefinitionNameLike());
    }
    else if (this.getProcessDefinitionNameLike() != null) {
      extendedQuery.processDefinitionNameLike(this.getProcessDefinitionNameLike());
    }

    if (extendingQuery.getProcessInstanceBusinessKey() != null) {
      extendedQuery.processInstanceBusinessKey(extendingQuery.getProcessInstanceBusinessKey());
    }
    else if (this.getProcessInstanceBusinessKey() != null) {
      extendedQuery.processInstanceBusinessKey(this.getProcessInstanceBusinessKey());
    }

    if (extendingQuery.getProcessInstanceBusinessKeyLike() != null) {
      extendedQuery.processInstanceBusinessKeyLike(extendingQuery.getProcessInstanceBusinessKeyLike());
    }
    else if (this.getProcessInstanceBusinessKeyLike() != null) {
      extendedQuery.processInstanceBusinessKeyLike(this.getProcessInstanceBusinessKeyLike());
    }

    if (extendingQuery.getDueDate() != null) {
      extendedQuery.dueDate(extendingQuery.getDueDate());
    }
    else if (this.getDueDate() != null) {
      extendedQuery.dueDate(this.getDueDate());
    }

    if (extendingQuery.getDueBefore() != null) {
      extendedQuery.dueBefore(extendingQuery.getDueBefore());
    }
    else if (this.getDueBefore() != null) {
      extendedQuery.dueBefore(this.getDueBefore());
    }

    if (extendingQuery.getDueAfter() != null) {
      extendedQuery.dueAfter(extendingQuery.getDueAfter());
    }
    else if (this.getDueAfter() != null) {
      extendedQuery.dueAfter(this.getDueAfter());
    }

    if (extendingQuery.isWithoutDueDate() || this.isWithoutDueDate()) {
      extendedQuery.withoutDueDate();
    }

    if (extendingQuery.getFollowUpDate() != null) {
      extendedQuery.followUpDate(extendingQuery.getFollowUpDate());
    }
    else if (this.getFollowUpDate() != null) {
      extendedQuery.followUpDate(this.getFollowUpDate());
    }

    if (extendingQuery.getFollowUpBefore() != null) {
      extendedQuery.followUpBefore(extendingQuery.getFollowUpBefore());
    }
    else if (this.getFollowUpBefore() != null) {
      extendedQuery.followUpBefore(this.getFollowUpBefore());
    }

    if (extendingQuery.getFollowUpAfter() != null) {
      extendedQuery.followUpAfter(extendingQuery.getFollowUpAfter());
    }
    else if (this.getFollowUpAfter() != null) {
      extendedQuery.followUpAfter(this.getFollowUpAfter());
    }

    if (extendingQuery.isFollowUpNullAccepted() || this.isFollowUpNullAccepted()) {
      extendedQuery.setFollowUpNullAccepted(true);
    }

    if (extendingQuery.isExcludeSubtasks() || this.isExcludeSubtasks()) {
      extendedQuery.excludeSubtasks();
    }

    if (extendingQuery.getSuspensionState() != null) {
      if (extendingQuery.getSuspensionState().equals(SuspensionState.ACTIVE)) {
        extendedQuery.active();
      }
      else if (extendingQuery.getSuspensionState().equals(SuspensionState.SUSPENDED)) {
        extendedQuery.suspended();
      }
    }
    else if (this.getSuspensionState() != null) {
      if (this.getSuspensionState().equals(SuspensionState.ACTIVE)) {
        extendedQuery.active();
      }
      else if (this.getSuspensionState().equals(SuspensionState.SUSPENDED)) {
        extendedQuery.suspended();
      }
    }

    if (extendingQuery.getCaseInstanceId() != null) {
      extendedQuery.caseInstanceId(extendingQuery.getCaseInstanceId());
    }
    else if (this.getCaseInstanceId() != null) {
      extendedQuery.caseInstanceId(this.getCaseInstanceId());
    }

    if (extendingQuery.getCaseInstanceBusinessKey() != null) {
      extendedQuery.caseInstanceBusinessKey(extendingQuery.getCaseInstanceBusinessKey());
    }
    else if (this.getCaseInstanceBusinessKey() != null) {
      extendedQuery.caseInstanceBusinessKey(this.getCaseInstanceBusinessKey());
    }

    if (extendingQuery.getCaseInstanceBusinessKeyLike() != null) {
      extendedQuery.caseInstanceBusinessKeyLike(extendingQuery.getCaseInstanceBusinessKeyLike());
    }
    else if (this.getCaseInstanceBusinessKeyLike() != null) {
      extendedQuery.caseInstanceBusinessKeyLike(this.getCaseInstanceBusinessKeyLike());
    }

    if (extendingQuery.getCaseExecutionId() != null) {
      extendedQuery.caseExecutionId(extendingQuery.getCaseExecutionId());
    }
    else if (this.getCaseExecutionId() != null) {
      extendedQuery.caseExecutionId(this.getCaseExecutionId());
    }

    if (extendingQuery.getCaseDefinitionId() != null) {
      extendedQuery.caseDefinitionId(extendingQuery.getCaseDefinitionId());
    }
    else if (this.getCaseDefinitionId() != null) {
      extendedQuery.caseDefinitionId(this.getCaseDefinitionId());
    }

    if (extendingQuery.getCaseDefinitionKey() != null) {
      extendedQuery.caseDefinitionKey(extendingQuery.getCaseDefinitionKey());
    }
    else if (this.getCaseDefinitionKey() != null) {
      extendedQuery.caseDefinitionKey(this.getCaseDefinitionKey());
    }

    if (extendingQuery.getCaseDefinitionName() != null) {
      extendedQuery.caseDefinitionName(extendingQuery.getCaseDefinitionName());
    }
    else if (this.getCaseDefinitionName() != null) {
      extendedQuery.caseDefinitionName(this.getCaseDefinitionName());
    }

    if (extendingQuery.getCaseDefinitionNameLike() != null) {
      extendedQuery.caseDefinitionNameLike(extendingQuery.getCaseDefinitionNameLike());
    }
    else if (this.getCaseDefinitionNameLike() != null) {
      extendedQuery.caseDefinitionNameLike(this.getCaseDefinitionNameLike());
    }

    if (extendingQuery.isInitializeFormKeys() || this.isInitializeFormKeys()) {
      extendedQuery.initializeFormKeys();
    }

    if (extendingQuery.isTaskNameCaseInsensitive() || this.isTaskNameCaseInsensitive()) {
      extendedQuery.taskNameCaseInsensitive();
    }

    if (extendingQuery.getTenantIds() != null) {
      extendedQuery.tenantIdIn(extendingQuery.getTenantIds());
    } else if (this.getTenantIds() != null) {
      extendedQuery.tenantIdIn(this.getTenantIds());
    }

    if (extendingQuery.isWithoutTenantId() || this.isWithoutTenantId()) {
      extendedQuery.withoutTenantId();
    }

    // merge variables
    mergeVariables(extendedQuery, extendingQuery);

    // merge expressions
    mergeExpressions(extendedQuery, extendingQuery);

    // include taskAssigned tasks has to be set after expression as it asserts on already set
    // candidate properties which could be expressions
    if (extendingQuery.isIncludeAssignedTasks() || this.isIncludeAssignedTasks()) {
      extendedQuery.includeAssignedTasks();
    }

    mergeOrdering(extendedQuery, extendingQuery);

    extendedQuery.queries = new ArrayList<>(Arrays.asList(extendedQuery));

    if (queries.size() > 1) {
      queries.remove(0);
      extendedQuery.queries.addAll(queries);
    }

    if (extendingQuery.queries.size() > 1) {
      extendingQuery.queries.remove(0);
      extendedQuery.queries.addAll(extendingQuery.queries);
    }

    return extendedQuery;
  }

  /**
   * Simple implementation of variable merging. Variables are only overridden if they have the same name and are
   * in the same scope (ie are process instance, task or case execution variables).
   */
  protected void mergeVariables(TaskQueryImpl extendedQuery, TaskQueryImpl extendingQuery) {
    List<TaskQueryVariableValue> extendingVariables = extendingQuery.getVariables();

    Set<TaskQueryVariableValueComparable> extendingVariablesComparable = new HashSet<>();

    // set extending variables and save names for comparison of original variables
    for (TaskQueryVariableValue extendingVariable : extendingVariables) {
      extendedQuery.addVariable(extendingVariable);
      extendingVariablesComparable.add(new TaskQueryVariableValueComparable(extendingVariable));
    }

    for (TaskQueryVariableValue originalVariable : this.getVariables()) {
      if (!extendingVariablesComparable.contains(new TaskQueryVariableValueComparable(originalVariable))) {
        extendedQuery.addVariable(originalVariable);
      }
    }

  }

  protected class TaskQueryVariableValueComparable {

    protected TaskQueryVariableValue variableValue;

    public TaskQueryVariableValueComparable(TaskQueryVariableValue variableValue) {
      this.variableValue = variableValue;
    }

    public TaskQueryVariableValue getVariableValue() {
      return variableValue;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      TaskQueryVariableValue other = ((TaskQueryVariableValueComparable) o).getVariableValue();

      return variableValue.getName().equals(other.getName())
             && variableValue.isProcessInstanceVariable() == other.isProcessInstanceVariable()
             && variableValue.isLocal() == other.isLocal();
    }

    @Override
    public int hashCode() {
      int result = variableValue.getName() != null ? variableValue.getName().hashCode() : 0;
      result = 31 * result + (variableValue.isProcessInstanceVariable() ? 1 : 0);
      result = 31 * result + (variableValue.isLocal() ? 1 : 0);
      return result;
    }

  }

  public boolean isFollowUpNullAccepted() {
    return followUpNullAccepted;
  }

  @Override
  public TaskQuery taskNameNotEqual(String name) {
    this.nameNotEqual = name;
    return this;
  }

  @Override
  public TaskQuery taskNameNotLike(String nameNotLike) {
    ensureNotNull("Task nameNotLike", nameNotLike);
    this.nameNotLike = nameNotLike;
    return this;
  }

  /**
   * @return true if the query is not supposed to find CMMN or standalone tasks
   */
  public boolean isQueryForProcessTasksOnly() {
    ProcessEngineConfigurationImpl engineConfiguration = Context.getProcessEngineConfiguration();

    return !engineConfiguration.isCmmnEnabled() && !engineConfiguration.isStandaloneTasksEnabled();
  }

  @Override
  public TaskQuery or() {
    if (this != queries.get(0)) {
      throw new ProcessEngineException("Invalid query usage: cannot set or() within 'or' query");
    }

    TaskQueryImpl orQuery = new TaskQueryImpl();
    orQuery.isOrQueryActive = true;
    orQuery.queries = queries;
    queries.add(orQuery);
    return orQuery;
  }

  @Override
  public TaskQuery endOr() {
    if (!queries.isEmpty() && this != queries.get(queries.size()-1)) {
      throw new ProcessEngineException("Invalid query usage: cannot set endOr() before or()");
    }

    return queries.get(0);
  }

  @Override
  public TaskQuery matchVariableNamesIgnoreCase() {
    this.variableNamesIgnoreCase = true;
    for (TaskQueryVariableValue variable : this.variables) {
      variable.setVariableNameIgnoreCase(true);
    }
    return this;
  }

  @Override
  public TaskQuery matchVariableValuesIgnoreCase() {
    this.variableValuesIgnoreCase = true;
    for (TaskQueryVariableValue variable : this.variables) {
      variable.setVariableValueIgnoreCase(true);
    }
    return this;
  }
}
