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
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceQueryImpl extends AbstractQuery<HistoricTaskInstanceQuery, HistoricTaskInstance> implements HistoricTaskInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected String processInstanceId;
  protected String processInstanceBusinessKey;
  protected String[] processInstanceBusinessKeys;
  protected String processInstanceBusinessKeyLike;
  protected String executionId;
  protected String[] activityInstanceIds;
  protected String taskId;
  protected String taskName;
  protected String taskNameLike;
  protected String taskParentTaskId;
  protected String taskDescription;
  protected String taskDescriptionLike;
  protected String taskDeleteReason;
  protected String taskDeleteReasonLike;
  protected String taskOwner;
  protected String taskOwnerLike;
  protected Boolean assigned;
  protected Boolean unassigned;
  protected String taskAssignee;
  protected String taskAssigneeLike;
  protected String[] taskDefinitionKeys;
  protected String taskInvolvedUser;
  protected String taskInvolvedGroup;
  protected String taskHadCandidateUser;
  protected String taskHadCandidateGroup;
  protected Boolean withCandidateGroups;
  protected Boolean withoutCandidateGroups;
  protected Integer taskPriority;
  protected boolean finished;
  protected boolean unfinished;
  protected boolean processFinished;
  protected boolean processUnfinished;
  protected List<TaskQueryVariableValue> variables = new ArrayList<>();
  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;

  protected Date dueDate;
  protected Date dueAfter;
  protected Date dueBefore;
  protected boolean isWithoutTaskDueDate;

  protected Date followUpDate;
  protected Date followUpBefore;
  protected Date followUpAfter;

  protected String[] tenantIds;
  protected boolean isTenantIdSet;

  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionName;
  protected String caseInstanceId;
  protected String caseExecutionId;

  protected Date finishedAfter;
  protected Date finishedBefore;
  protected Date startedAfter;
  protected Date startedBefore;

  protected List<HistoricTaskInstanceQueryImpl> queries = new ArrayList<>(Arrays.asList(this));
  protected boolean isOrQueryActive = false;

  public HistoricTaskInstanceQueryImpl() {
  }

  public HistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricTaskInstance> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstancesByQueryCriteria(this, page);
  }


  public HistoricTaskInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricTaskInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processInstanceBusinessKeyIn(String... processInstanceBusinessKeys) {
    ensureNotNull("processInstanceBusinessKeys", (Object[]) processInstanceBusinessKeys);
    this.processInstanceBusinessKeys = processInstanceBusinessKeys;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processInstanceBusinessKeyLike(String processInstanceBusinessKey) {
    this.processInstanceBusinessKeyLike = processInstanceBusinessKey;
    return this;
  }

  public HistoricTaskInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricTaskInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    ensureNotNull("activityInstanceIds", (Object[]) activityInstanceIds);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public HistoricTaskInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricTaskInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  public HistoricTaskInstanceQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  public HistoricTaskInstanceQueryImpl taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskNameLike(String taskNameLike) {
    this.taskNameLike = taskNameLike;
    return this;
  }

  public HistoricTaskInstanceQuery taskParentTaskId(String parentTaskId) {
    this.taskParentTaskId = parentTaskId;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDescriptionLike(String taskDescriptionLike) {
    this.taskDescriptionLike = taskDescriptionLike;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskAssigned() {
    this.assigned = true;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskUnassigned() {
    this.unassigned = true;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskAssigneeLike(String taskAssigneeLike) {
    this.taskAssigneeLike = taskAssigneeLike;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskOwner(String taskOwner) {
    this.taskOwner = taskOwner;
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskOwnerLike(String taskOwnerLike) {
    this.taskOwnerLike = taskOwnerLike;
    return this;
  }

  public HistoricTaskInstanceQuery caseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public HistoricTaskInstanceQuery caseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  public HistoricTaskInstanceQuery caseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
    return this;
  }

  public HistoricTaskInstanceQuery caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public HistoricTaskInstanceQuery caseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public HistoricTaskInstanceQueryImpl finished() {
    this.finished = true;
    return this;
  }

  public HistoricTaskInstanceQueryImpl unfinished() {
    this.unfinished = true;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery matchVariableNamesIgnoreCase() {
    this.variableNamesIgnoreCase = true;
    for (QueryVariableValue variable : this.variables) {
      variable.setVariableNameIgnoreCase(true);
    }
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery matchVariableValuesIgnoreCase() {
    this.variableValuesIgnoreCase = true;
    for (QueryVariableValue variable : this.variables) {
      variable.setVariableValueIgnoreCase(true);
    }
    return this;
  }

  public HistoricTaskInstanceQueryImpl taskVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, true, false);
    return this;
  }

  public HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, false, true);
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, false, true);
    return this;
  }

  public HistoricTaskInstanceQuery processVariableValueLike(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE, false, true);
    return this;
  }

  public HistoricTaskInstanceQuery processVariableValueNotLike(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_LIKE, false, true);
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN, false, true);
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL, false, true);
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN, false, true);
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery processVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL, false, true);
    return this;
  }

  public HistoricTaskInstanceQuery taskDefinitionKey(String taskDefinitionKey) {
    return taskDefinitionKeyIn(taskDefinitionKey);
  }

  public HistoricTaskInstanceQuery taskDefinitionKeyIn(String... taskDefinitionKeys) {
    ensureNotNull(NotValidException.class, "taskDefinitionKeys", (Object[]) taskDefinitionKeys);
    this.taskDefinitionKeys = taskDefinitionKeys;
    return this;
  }

  public HistoricTaskInstanceQuery taskPriority(Integer taskPriority) {
    this.taskPriority = taskPriority;
    return this;
  }

  public HistoricTaskInstanceQuery processFinished() {
    this.processFinished = true;
    return this;
  }

  public HistoricTaskInstanceQuery taskInvolvedUser(String userId){
    this.taskInvolvedUser = userId;
    return this;
  }

  public HistoricTaskInstanceQuery taskInvolvedGroup(String groupId){
    this.taskInvolvedGroup = groupId;
    return this;
  }

  public HistoricTaskInstanceQuery taskHadCandidateUser(String userId){
    this.taskHadCandidateUser = userId;
    return this;
  }

  public HistoricTaskInstanceQuery taskHadCandidateGroup(String groupId){
    this.taskHadCandidateGroup = groupId;
    return this;
  }

  public HistoricTaskInstanceQuery withCandidateGroups() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set withCandidateGroups() within 'or' query");
    }

    this.withCandidateGroups = true;
    return this;
  }

  public HistoricTaskInstanceQuery withoutCandidateGroups() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set withoutCandidateGroups() within 'or' query");
    }

    this.withoutCandidateGroups = true;
    return this;
  }

  public HistoricTaskInstanceQuery processUnfinished() {
    this.processUnfinished = true;
    return this;
  }

  protected void ensureVariablesInitialized() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
    String dbType = processEngineConfiguration.getDatabaseType();
    for(QueryVariableValue var : variables) {
      var.initialize(variableSerializers, dbType);
    }

    if (!queries.isEmpty()) {
      for (HistoricTaskInstanceQueryImpl orQuery: queries) {
        for (QueryVariableValue var : orQuery.variables) {
          var.initialize(variableSerializers, dbType);
        }
      }
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
    boolean shouldMatchVariableValuesIgnoreCase = Boolean.TRUE.equals(variableValuesIgnoreCase) && value != null && String.class.isAssignableFrom(value.getClass());
    boolean shouldMatchVariableNamesIgnoreCase = Boolean.TRUE.equals(variableNamesIgnoreCase);
    addVariable(new TaskQueryVariableValue(name, value, operator, isTaskVariable, isProcessInstanceVariable, shouldMatchVariableNamesIgnoreCase, shouldMatchVariableValuesIgnoreCase));
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

  public HistoricTaskInstanceQuery taskDueDate(Date dueDate) {
    // The taskDueDate filter can't be used in an AND query with
    // the withoutTaskDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutTaskDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both taskDueDate and withoutTaskDueDate filters.");
      }
    }

    this.dueDate = dueDate;
    return this;
  }

  public HistoricTaskInstanceQuery taskDueAfter(Date dueAfter) {
    // The taskDueAfter filter can't be used in an AND query with
    // the withoutTaskDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutTaskDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both taskDueAfter and withoutTaskDueDate filters.");
      }
    }

    this.dueAfter = dueAfter;
    return this;
  }

  public HistoricTaskInstanceQuery taskDueBefore(Date dueBefore) {
    // The taskDueBefore filter can't be used in an AND query with
    // the withoutTaskDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (TRUE.equals(isWithoutTaskDueDate)) {
        throw new ProcessEngineException("Invalid query usage: cannot set both taskDueBefore and withoutTaskDueDate filters.");
      }
    }

    this.dueBefore = dueBefore;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery withoutTaskDueDate() {
    // The due date filters can't be used in an AND query with
    // the withoutTaskDueDate filter. They can be combined in an OR query
    if (!isOrQueryActive) {
      if (dueAfter != null || dueBefore != null || dueDate != null) {
        throw new ProcessEngineException("Invalid query usage: cannot set both task due date (equal to, before, or after) and withoutTaskDueDate filters.");
      }
    }

    this.isWithoutTaskDueDate = true;
    return this;
  }

  public HistoricTaskInstanceQuery taskFollowUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
    return this;
  }

  public HistoricTaskInstanceQuery taskFollowUpBefore(Date followUpBefore) {
    this.followUpBefore = followUpBefore;
    return this;
  }

  public HistoricTaskInstanceQuery taskFollowUpAfter(Date followUpAfter) {
    this.followUpAfter = followUpAfter;
    return this;
  }

  public HistoricTaskInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery withoutTenantId() {
    this.tenantIds = null;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery finishedAfter(Date date) {
    this.finishedAfter = date;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery finishedBefore(Date date) {
    this.finishedBefore = date;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery startedAfter(Date date) {
    this.startedAfter = date;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery startedBefore(Date date) {
    this.startedBefore = date;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || (finished && unfinished)
      ||(processFinished && processUnfinished)
      || CompareUtil.areNotInAscendingOrder(startedAfter, startedBefore)
      || CompareUtil.areNotInAscendingOrder(finishedAfter, finishedBefore)
      || CompareUtil.areNotInAscendingOrder(dueAfter, dueDate, dueBefore)
      || CompareUtil.areNotInAscendingOrder(followUpAfter, followUpDate, followUpBefore)
      || CompareUtil.elementIsNotContainedInArray(processInstanceBusinessKey, processInstanceBusinessKeys);
  }

  // ordering /////////////////////////////////////////////////////////////////

  public HistoricTaskInstanceQueryImpl orderByTaskId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByHistoricActivityInstanceId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.ACTIVITY_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessDefinitionId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessDefinitionId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessInstanceId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessInstanceId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByExecutionId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByExecutionId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceDuration() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByHistoricTaskInstanceDuration() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceEndTime() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByHistoricTaskInstanceEndTime() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.END);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByHistoricActivityInstanceStartTime() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.START);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskName() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskName() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_NAME);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskDescription() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskDescription() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskAssignee() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskAssignee() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_ASSIGNEE);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskOwner() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskOwner() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_OWNER);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskDueDate() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskDueDate() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_DUE_DATE);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskFollowUpDate() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskFollowUpDate() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_FOLLOW_UP_DATE);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByDeleteReason() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByDeleteReason() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.DELETE_REASON);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskDefinitionKey() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskDefinitionKey() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_DEFINITION_KEY);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskPriority() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTaskPriority() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
    return this;
  }

  public HistoricTaskInstanceQuery orderByCaseDefinitionId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByCaseDefinitionId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.CASE_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQuery orderByCaseInstanceId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByCaseInstanceId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.CASE_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQuery orderByCaseExecutionId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByCaseExecutionId() within 'or' query");
    }

    orderBy(HistoricTaskInstanceQueryProperty.CASE_EXECUTION_ID);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTenantId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTenantId() within 'or' query");
    }

    return orderBy(HistoricTaskInstanceQueryProperty.TENANT_ID);
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
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

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public Boolean isAssigned() {
    return assigned;
  }

  public Boolean isUnassigned() {
    return unassigned;
  }

  public Boolean isWithCandidateGroups() {
    return withCandidateGroups;
  }

  public Boolean isWithoutCandidateGroups() {
    return withoutCandidateGroups;
  }

  public boolean isFinished() {
    return finished;
  }

  public boolean isProcessFinished() {
    return processFinished;
  }

  public boolean isUnfinished() {
    return unfinished;
  }

  public boolean isProcessUnfinished() {
    return processUnfinished;
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

  public boolean isWithoutTaskDueDate() {
    return isWithoutTaskDueDate;
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

  public String getTaskName() {
    return taskName;
  }

  public String getTaskNameLike() {
    return taskNameLike;
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public String getTaskDescriptionLike() {
    return taskDescriptionLike;
  }

  public String getTaskDeleteReason() {
    return taskDeleteReason;
  }

  public String getTaskDeleteReasonLike() {
    return taskDeleteReasonLike;
  }

  public String getTaskAssignee() {
    return taskAssignee;
  }

  public String getTaskAssigneeLike() {
    return taskAssigneeLike;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getTaskInvolvedGroup() {
    return taskInvolvedGroup;
  }

  public String getTaskInvolvedUser() {
    return taskInvolvedUser;
  }

  public String getTaskHadCandidateGroup() {
    return taskHadCandidateGroup;
  }

  public String getTaskHadCandidateUser() {
    return taskHadCandidateUser;
  }

  public String[] getTaskDefinitionKeys() {
    return taskDefinitionKeys;
  }

  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }

  public Boolean getVariableNamesIgnoreCase() {
    return variableNamesIgnoreCase;
  }

  public Boolean getVariableValuesIgnoreCase() {
    return variableValuesIgnoreCase;
  }

  public String getTaskOwnerLike() {
    return taskOwnerLike;
  }

  public String getTaskOwner() {
    return taskOwner;
  }

  public Integer getTaskPriority() {
    return taskPriority;
  }

  public String getTaskParentTaskId() {
    return taskParentTaskId;
  }

  public String[] getTenantIds() {
    return tenantIds;
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

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public Date getFinishedAfter() {
    return finishedAfter;
  }

  public Date getFinishedBefore() {
    return finishedBefore;
  }

  public Date getStartedAfter() {
    return startedAfter;
  }

  public Date getStartedBefore() {
    return startedBefore;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public List<HistoricTaskInstanceQueryImpl> getQueries() {
    return queries;
  }

  public boolean isOrQueryActive() {
    return isOrQueryActive;
  }

  public void addOrQuery(HistoricTaskInstanceQueryImpl orQuery) {
    orQuery.isOrQueryActive = true;
    this.queries.add(orQuery);
  }

  public void setOrQueryActive() {
    isOrQueryActive = true;
  }

  @Override
  public HistoricTaskInstanceQuery or() {
    if (this != queries.get(0)) {
      throw new ProcessEngineException("Invalid query usage: cannot set or() within 'or' query");
    }

    HistoricTaskInstanceQueryImpl orQuery = new HistoricTaskInstanceQueryImpl();
    orQuery.isOrQueryActive = true;
    orQuery.queries = queries;
    queries.add(orQuery);
    return orQuery;
  }

  @Override
  public HistoricTaskInstanceQuery endOr() {
    if (!queries.isEmpty() && this != queries.get(queries.size()-1)) {
      throw new ProcessEngineException("Invalid query usage: cannot set endOr() before or()");
    }

    return queries.get(0);
  }

}