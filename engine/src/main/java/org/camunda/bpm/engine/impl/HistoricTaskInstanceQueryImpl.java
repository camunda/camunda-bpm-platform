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

package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
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
  protected List<TaskQueryVariableValue> variables = new ArrayList<TaskQueryVariableValue>();
  protected Date dueDate;
  protected Date dueAfter;
  protected Date dueBefore;
  protected Date followUpDate;
  protected Date followUpBefore;

  protected Date followUpAfter;
  protected String[] tenantIds;
  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionName;
  protected String caseInstanceId;
  protected String caseExecutionId;

  protected Date finishedAfter;
  protected Date finishedBefore;
  protected Date startedAfter;
  protected Date startedBefore;

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

  public HistoricTaskInstanceQueryImpl taskVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true, false));
    return this;
  }

  public HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, false, true));
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
    this.withCandidateGroups = true;
    return this;
  }

  public HistoricTaskInstanceQuery withoutCandidateGroups() {
    this.withoutCandidateGroups = true;
    return this;
  }

  public HistoricTaskInstanceQuery processUnfinished() {
    this.processUnfinished = true;
    return this;
  }

  protected void ensureVariablesInitialized() {
    VariableSerializers types = Context.getProcessEngineConfiguration().getVariableSerializers();
    for(QueryVariableValue var : variables) {
      var.initialize(types);
    }
  }

  public void addVariable(String name, Object value, QueryOperator operator, boolean isTaskVariable, boolean isProcessInstanceVariable) {
    ensureNotNull("name", name);
    System.out.println("add var");
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
        default:
          break;
      }
    }
    addVariable(new TaskQueryVariableValue(name, value, operator, isTaskVariable, isProcessInstanceVariable));
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
    this.dueDate = dueDate;
    return this;
  }

  public HistoricTaskInstanceQuery taskDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }

  public HistoricTaskInstanceQuery taskDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
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
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery finishedAfter(Date date) {
    finished = true;
    this.finishedAfter = date;
    return this;
  }

  @Override
  public HistoricTaskInstanceQuery finishedBefore(Date date) {
    finished = true;
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
    orderBy(HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.ACTIVITY_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricTaskInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceDuration() {
    orderBy(HistoricTaskInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceEndTime() {
    orderBy(HistoricTaskInstanceQueryProperty.END);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricTaskInstanceQueryProperty.START);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskName() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_NAME);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskDescription() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskAssignee() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_ASSIGNEE);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskOwner() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_OWNER);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskDueDate() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DUE_DATE);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskFollowUpDate() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_FOLLOW_UP_DATE);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByDeleteReason() {
    orderBy(HistoricTaskInstanceQueryProperty.DELETE_REASON);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskDefinitionKey() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DEFINITION_KEY);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskPriority() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
    return this;
  }

  public HistoricTaskInstanceQuery orderByCaseDefinitionId() {
    orderBy(HistoricTaskInstanceQueryProperty.CASE_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQuery orderByCaseInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.CASE_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQuery orderByCaseExecutionId() {
    orderBy(HistoricTaskInstanceQueryProperty.CASE_EXECUTION_ID);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTenantId() {
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

  public boolean isUnfinished() {
    return unfinished;
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

  public String[] getTaskDefinitionKeys() {
    return taskDefinitionKeys;
  }

  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }

  public String getTaskOwnerLike() {
    return taskOwnerLike;
  }

  public String getTaskOwner() {
    return taskOwner;
  }

  public String getTaskParentTaskId() {
    return taskParentTaskId;
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
}
