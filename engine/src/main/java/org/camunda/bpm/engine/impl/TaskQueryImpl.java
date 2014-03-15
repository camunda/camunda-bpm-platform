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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.variable.VariableTypes;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TaskQueryImpl extends AbstractQuery<TaskQuery, Task> implements TaskQuery {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String name;
  protected String nameLike;
  protected String description;
  protected String descriptionLike;
  protected Integer priority;
  protected Integer minPriority;
  protected Integer maxPriority;
  protected String assignee;
  protected String assigneeLike;
  protected String involvedUser;
  protected String owner;
  protected boolean unassigned = false;
  protected boolean noDelegationState = false;
  protected DelegationState delegationState;
  protected String candidateUser;
  protected String candidateGroup;
  private List<String> candidateGroups;
  protected String processInstanceId;
  protected String executionId;
  protected String[] activityInstanceIdIn;
  protected Date createTime;
  protected Date createTimeBefore;
  protected Date createTimeAfter;
  protected String key;
  protected String keyLike;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String processDefinitionNameLike;
  protected String processInstanceBusinessKey;
  protected String processInstanceBusinessKeyLike;
  protected List<TaskQueryVariableValue> variables = new ArrayList<TaskQueryVariableValue>();
  protected Date dueDate;
  protected Date dueBefore;
  protected Date dueAfter;
  protected Date followUpDate;
  protected Date followUpBefore;
  protected Date followUpAfter;
  protected boolean excludeSubtasks = false;
  protected SuspensionState suspensionState;

  public TaskQueryImpl() {
  }

  public TaskQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public TaskQueryImpl taskId(String taskId) {
    if (taskId == null) {
      throw new ProcessEngineException("Task id is null");
    }
    this.taskId = taskId;
    return this;
  }

  public TaskQueryImpl taskName(String name) {
    this.name = name;
    return this;
  }

  public TaskQueryImpl taskNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ProcessEngineException("Task namelike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public TaskQueryImpl taskDescription(String description) {
    if (description == null) {
      throw new ProcessEngineException("Description is null");
    }
    this.description = description;
    return this;
  }

  public TaskQuery taskDescriptionLike(String descriptionLike) {
    if (descriptionLike == null) {
      throw new ProcessEngineException("Task descriptionlike is null");
    }
    this.descriptionLike = descriptionLike;
    return this;
  }

  public TaskQuery taskPriority(Integer priority) {
    if (priority == null) {
      throw new ProcessEngineException("Priority is null");
    }
    this.priority = priority;
    return this;
  }

  public TaskQuery taskMinPriority(Integer minPriority) {
    if (minPriority == null) {
      throw new ProcessEngineException("Min Priority is null");
    }
    this.minPriority = minPriority;
    return this;
  }

  public TaskQuery taskMaxPriority(Integer maxPriority) {
    if (maxPriority == null) {
      throw new ProcessEngineException("Max Priority is null");
    }
    this.maxPriority = maxPriority;
    return this;
  }

  public TaskQueryImpl taskAssignee(String assignee) {
    if (assignee == null) {
      throw new ProcessEngineException("Assignee is null");
    }
    this.assignee = assignee;
    return this;
  }

  public TaskQuery taskAssigneeLike(String assignee) {
    assertParamNotNull("Assignee", assignee);
    this.assigneeLike = assignee;
    return this;
  }

  public TaskQueryImpl taskOwner(String owner) {
    if (owner == null) {
      throw new ProcessEngineException("Owner is null");
    }
    this.owner = owner;
    return this;
  }

  /** @see {@link #taskUnassigned} */
  @Deprecated
  public TaskQuery taskUnnassigned() {
    return taskUnassigned();
  }

  public TaskQuery taskUnassigned() {
    this.unassigned = true;
    return this;
  }

  public TaskQuery taskDelegationState(DelegationState delegationState) {
    if (delegationState == null) {
      this.noDelegationState = true;
    } else {
      this.delegationState = delegationState;
    }
    return this;
  }

  public TaskQueryImpl taskCandidateUser(String candidateUser) {
    if (candidateUser == null) {
      throw new ProcessEngineException("Candidate user is null");
    }
    if (candidateGroup != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    if (candidateGroups != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroupIn");
    }
    this.candidateUser = candidateUser;
    return this;
  }

  public TaskQueryImpl taskInvolvedUser(String involvedUser) {
    if (involvedUser == null) {
      throw new ProcessEngineException("Involved user is null");
    }
    this.involvedUser = involvedUser;
    return this;
  }

  public TaskQueryImpl taskCandidateGroup(String candidateGroup) {
    if (candidateGroup == null) {
      throw new ProcessEngineException("Candidate group is null");
    }
    if (candidateUser != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroup and candidateUser");
    }
    if (candidateGroups != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroup and candidateGroupIn");
    }
    this.candidateGroup = candidateGroup;
    return this;
  }

  public TaskQuery taskCandidateGroupIn(List<String> candidateGroups) {
    if(candidateGroups == null) {
      throw new ProcessEngineException("Candidate group list is null");
    }
    if(candidateGroups.size()== 0) {
      throw new ProcessEngineException("Candidate group list is empty");
    }

    if (candidateUser != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroupIn and candidateUser");
    }
    if (candidateGroup != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both candidateGroupIn and candidateGroup");
    }

    this.candidateGroups = candidateGroups;
    return this;
  }

  public TaskQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public TaskQueryImpl processInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
    return this;
  }

  public TaskQuery processInstanceBusinessKeyLike(String processInstanceBusinessKey) {
  	this.processInstanceBusinessKeyLike = processInstanceBusinessKey;
  	return this;
  }

  public TaskQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public TaskQuery activityInstanceIdIn(String... activityInstanceIds) {
    this.activityInstanceIdIn = activityInstanceIds;
    return this;
  }

  public TaskQueryImpl taskCreatedOn(Date createTime) {
    this.createTime = createTime;
    return this;
  }

  public TaskQuery taskCreatedBefore(Date before) {
    this.createTimeBefore = before;
    return this;
  }

  public TaskQuery taskCreatedAfter(Date after) {
    this.createTimeAfter = after;
    return this;
  }

  public TaskQuery taskDefinitionKey(String key) {
    this.key = key;
    return this;
  }

  public TaskQuery taskDefinitionKeyLike(String keyLike) {
    this.keyLike = keyLike;
    return this;
  }

  public TaskQuery taskVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, true);
    return this;
  }

  public TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, true);
    return this;
  }

  public TaskQuery taskVariableValueLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE, true);
  	return this;
  }

  public TaskQuery taskVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN, true);
  	return this;
  }

  public TaskQuery taskVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL, true);
  	return this;
  }

  public TaskQuery taskVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN, true);
  	return this;
  }

  public TaskQuery taskVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL, true);
  	return this;
  }

  public TaskQuery processVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, false);
    return this;
  }

  public TaskQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, false);
    return this;
  }

  public TaskQuery processVariableValueLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE, false);
  	return this;
  }

  public TaskQuery processVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN, false);
  	return this;
  }

  public TaskQuery processVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL, false);
  	return this;
  }

  public TaskQuery processVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN, false);
  	return this;
  }

  public TaskQuery processVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL, false);
  	return this;
  }

  public TaskQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public TaskQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public TaskQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  public TaskQuery processDefinitionNameLike(String processDefinitionName) {
  	this.processDefinitionNameLike = processDefinitionName;
  	return this;
  }

  public TaskQuery dueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }

  public TaskQuery dueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
    return this;
  }

  public TaskQuery dueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }

  public TaskQuery followUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
    return this;
  }

  public TaskQuery followUpBefore(Date followUpBefore) {
    this.followUpBefore = followUpBefore;
    return this;
  }

  public TaskQuery followUpAfter(Date followUpAfter) {
    this.followUpAfter = followUpAfter;
    return this;
  }

  public TaskQuery excludeSubtasks() {
    this.excludeSubtasks = true;
    return this;
  }

  public TaskQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public TaskQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public List<String> getCandidateGroups() {
    if (candidateGroup!=null) {
      return Collections.singletonList(candidateGroup);
    } else if (candidateUser != null) {
      return getGroupsForCandidateUser(candidateUser);
    } else if(candidateGroups != null) {
      return candidateGroups;
    }
    return null;
  }

  protected List<String> getGroupsForCandidateUser(String candidateUser) {
    // TODO: Discuss about removing this feature? Or document it properly and maybe recommend to not use it
    // and explain alternatives
    List<Group> groups = Context
      .getCommandContext()
      .getReadOnlyIdentityProvider()
      .createGroupQuery()
      .groupMember(candidateUser)
      .list();
    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  protected void ensureVariablesInitialized() {
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for(QueryVariableValue var : variables) {
      var.initialize(types);
    }
  }

  protected void addVariable(String name, Object value, QueryOperator operator, boolean processInstanceScope) {
    if(name == null) {
      throw new ProcessEngineException("name is null");
    }

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
    variables.add(new TaskQueryVariableValue(name, value, operator, processInstanceScope));
  }

  private boolean isBoolean(Object value) {
  	if (value == null) {
  	  return false;
  	}
  	return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
	}

  //ordering ////////////////////////////////////////////////////////////////

  public TaskQuery orderByTaskId() {
    return orderBy(TaskQueryProperty.TASK_ID);
  }

  public TaskQuery orderByTaskName() {
    return orderBy(TaskQueryProperty.NAME);
  }

  public TaskQuery orderByTaskDescription() {
    return orderBy(TaskQueryProperty.DESCRIPTION);
  }

  public TaskQuery orderByTaskPriority() {
    return orderBy(TaskQueryProperty.PRIORITY);
  }

  public TaskQuery orderByProcessInstanceId() {
    return orderBy(TaskQueryProperty.PROCESS_INSTANCE_ID);
  }

  public TaskQuery orderByExecutionId() {
    return orderBy(TaskQueryProperty.EXECUTION_ID);
  }

  public TaskQuery orderByTaskAssignee() {
    return orderBy(TaskQueryProperty.ASSIGNEE);
  }

  public TaskQuery orderByTaskCreateTime() {
    return orderBy(TaskQueryProperty.CREATE_TIME);
  }

  public TaskQuery orderByDueDate() {
    return orderBy(TaskQueryProperty.DUE_DATE);
  }

  public TaskQuery orderByFollowUpDate() {
    return orderBy(TaskQueryProperty.FOLLOW_UP_DATE);
  }

  //results ////////////////////////////////////////////////////////////////

  public List<Task> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getTaskManager()
      .findTasksByQueryCriteria(this);
  }

  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getTaskManager()
      .findTaskCountByQueryCriteria(this);
  }

  //getters ////////////////////////////////////////////////////////////////

  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getAssignee() {
    return assignee;
  }
  public String getAssigneeLike() {
    return assigneeLike;
  }
  public boolean getUnassigned() {
    return unassigned;
  }
  public DelegationState getDelegationState() {
    return delegationState;
  }
  public boolean getNoDelegationState() {
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
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String[] getActivityInstanceIdIn() {
    return activityInstanceIdIn;
  }
  public String getTaskId() {
    return taskId;
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
  public Date getCreateTime() {
    return createTime;
  }
  public Date getCreateTimeBefore() {
    return createTimeBefore;
  }
  public Date getCreateTimeAfter() {
    return createTimeAfter;
  }
  public String getKey() {
    return key;
  }
  public String getKeyLike() {
    return keyLike;
  }
  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
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
  public String getProcessInstanceBusinessKeyLike() {
    return processInstanceBusinessKeyLike;
  }
  public boolean getExcludeSubtasks() {
    return excludeSubtasks;
  }
}
