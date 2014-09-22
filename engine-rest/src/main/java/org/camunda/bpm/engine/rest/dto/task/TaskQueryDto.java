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
package org.camunda.bpm.engine.rest.dto.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.DelegationStateConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;

public class TaskQueryDto extends AbstractQueryDto<TaskQuery> {

  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_CASE_INSTANCE_ID_VALUE = "caseInstanceId";
  private static final String SORT_BY_DUE_DATE_VALUE = "dueDate";
  private static final String SORT_BY_FOLLOW_UP_VALUE = "followUpDate";
  private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  private static final String SORT_BY_CASE_EXECUTION_ID_VALUE = "caseExecutionId";
  private static final String SORT_BY_ASSIGNEE_VALUE = "assignee";
  private static final String SORT_BY_CREATE_TIME_VALUE = "created";
  private static final String SORT_BY_DESCRIPTION_VALUE = "description";
  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_PRIORITY_VALUE = "priority";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DUE_DATE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_FOLLOW_UP_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNEE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DESCRIPTION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY_VALUE);
  }

  private String processInstanceBusinessKey;
  private String processInstanceBusinessKeyLike;
  private String processDefinitionKey;
  private String processDefinitionId;
  private String executionId;
  private String[] activityInstanceIdIn;
  private String processDefinitionName;
  private String processDefinitionNameLike;
  private String processInstanceId;
  private String assignee;
  private String assigneeExpression;
  private String assigneeLike;
  private String assigneeLikeExpression;
  private String candidateGroup;
  private String candidateGroupExpression;
  private String candidateUser;
  private String candidateUserExpression;
  private String taskDefinitionKey;
  private String taskDefinitionKeyLike;
  private String description;
  private String descriptionLike;
  private String involvedUser;
  private String involvedUserExpression;
  private Integer maxPriority;
  private Integer minPriority;
  private String name;
  private String nameLike;
  private String owner;
  private String ownerExpression;
  private Integer priority;
  private Boolean unassigned;
  private Boolean active;
  private Boolean suspended;

  private String caseDefinitionKey;
  private String caseDefinitionId;
  private String caseDefinitionName;
  private String caseDefinitionNameLike;
  private String caseInstanceId;
  private String caseInstanceBusinessKey;
  private String caseInstanceBusinessKeyLike;
  private String caseExecutionId;

  private Date dueAfter;
  private String dueAfterExpression;
  private Date dueBefore;
  private String dueBeforeExpression;
  private Date dueDate;
  private String dueDateExpression;
  private Date followUpAfter;
  private String followUpAfterExpression;
  private Date followUpBefore;
  private String followUpBeforeExpression;
  private Date followUpDate;
  private String followUpDateExpression;
  private Date createdAfter;
  private String createdAfterExpression;
  private Date createdBefore;
  private String createdBeforeExpression;
  private Date createdOn;
  private String createdOnExpression;

  private String delegationState;

  private List<String> candidateGroups;
  private String candidateGroupsExpression;

  private List<VariableQueryParameterDto> taskVariables;
  private List<VariableQueryParameterDto> processVariables;
  private List<VariableQueryParameterDto> caseInstanceVariables;

  public TaskQueryDto() {

  }

  public TaskQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  @CamundaQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String businessKey) {
    this.processInstanceBusinessKey = businessKey;
  }

  @CamundaQueryParam("processInstanceBusinessKeyLike")
  public void setProcessInstanceBusinessKeyLike(String businessKeyLike) {
    this.processInstanceBusinessKeyLike = businessKeyLike;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam("processDefinitionName")
  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @CamundaQueryParam("processDefinitionNameLike")
  public void setProcessDefinitionNameLike(String processDefinitionNameLike) {
    this.processDefinitionNameLike = processDefinitionNameLike;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("assignee")
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  @CamundaQueryParam("assigneeExpression")
  public void setAssigneeExpression(String assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }

  @CamundaQueryParam("assigneeLike")
  public void setAssigneeLike(String assigneeLike) {
    this.assigneeLike = assigneeLike;
  }

  @CamundaQueryParam("assigneeLikeExpression")
  public void setAssigneeLikeExpression(String assigneeLikeExpression) {
    this.assigneeLikeExpression = assigneeLikeExpression;
  }

  @CamundaQueryParam("candidateGroup")
  public void setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
  }

  @CamundaQueryParam("candidateGroupExpression")
  public void setCandidateGroupExpression(String candidateGroupExpression) {
    this.candidateGroupExpression = candidateGroupExpression;
  }

  @CamundaQueryParam("candidateUser")
  public void setCandidateUser(String candidateUser) {
    this.candidateUser = candidateUser;
  }

  @CamundaQueryParam("candidateUserExpression")
  public void setCandidateUserExpression(String candidateUserExpression) {
    this.candidateUserExpression = candidateUserExpression;
  }

  @CamundaQueryParam("taskDefinitionKey")
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  @CamundaQueryParam("taskDefinitionKeyLike")
  public void setTaskDefinitionKeyLike(String taskDefinitionKeyLike) {
    this.taskDefinitionKeyLike = taskDefinitionKeyLike;
  }

  @CamundaQueryParam("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @CamundaQueryParam("descriptionLike")
  public void setDescriptionLike(String descriptionLike) {
    this.descriptionLike = descriptionLike;
  }

  @CamundaQueryParam("involvedUser")
  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }

  @CamundaQueryParam("involvedUserExpression")
  public void setInvolvedUserExpression(String involvedUserExpression) {
    this.involvedUserExpression = involvedUserExpression;
  }

  @CamundaQueryParam(value = "maxPriority", converter = IntegerConverter.class)
  public void setMaxPriority(Integer maxPriority) {
    this.maxPriority = maxPriority;
  }

  @CamundaQueryParam(value = "minPriority", converter = IntegerConverter.class)
  public void setMinPriority(Integer minPriority) {
    this.minPriority = minPriority;
  }

  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @CamundaQueryParam("owner")
  public void setOwner(String owner) {
    this.owner = owner;
  }

  @CamundaQueryParam("ownerExpression")
  public void setOwnerExpression(String ownerExpression) {
    this.ownerExpression = ownerExpression;
  }

  @CamundaQueryParam(value = "priority", converter = IntegerConverter.class)
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  @CamundaQueryParam(value = "unassigned", converter = BooleanConverter.class)
  public void setUnassigned(Boolean unassigned) {
    this.unassigned = unassigned;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "dueAfter", converter = DateConverter.class)
  public void setDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
  }

  @CamundaQueryParam(value = "dueAfterExpression")
  public void setDueAfterExpression(String dueAfterExpression) {
    this.dueAfterExpression = dueAfterExpression;
  }

  @CamundaQueryParam(value = "dueBefore", converter = DateConverter.class)
  public void setDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
  }

  @CamundaQueryParam(value = "dueBeforeExpression")
  public void setDueBeforeExpression(String dueBeforeExpression) {
    this.dueBeforeExpression = dueBeforeExpression;
  }

  @CamundaQueryParam(value = "due", converter = DateConverter.class)
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  @CamundaQueryParam(value = "dueDateExpression")
  public void setDueDateExpression(String dueDateExpression) {
    this.dueDateExpression = dueDateExpression;
  }

  @CamundaQueryParam(value = "followUpAfter", converter = DateConverter.class)
  public void setFollowUpAfter(Date followUpAfter) {
    this.followUpAfter = followUpAfter;
  }

  @CamundaQueryParam(value = "followUpAfterExpression")
  public void setFollowUpAfterExpression(String followUpAfterExpression) {
    this.followUpAfterExpression = followUpAfterExpression;
  }

  @CamundaQueryParam(value = "followUpBefore", converter = DateConverter.class)
  public void setFollowUpBefore(Date followUpBefore) {
    this.followUpBefore = followUpBefore;
  }

  @CamundaQueryParam(value = "followUpBeforeExpression")
  public void setFollowUpBeforeExpression(String followUpBeforeExpression) {
    this.followUpBeforeExpression = followUpBeforeExpression;
  }

  @CamundaQueryParam(value = "followUp", converter = DateConverter.class)
  public void setFollowUpDate(Date followUp) {
    this.followUpDate = followUp;
  }

  @CamundaQueryParam(value = "followUpDateExpression")
  public void setFollowUpDateExpression(String followUpDateExpression) {
    this.followUpDateExpression = followUpDateExpression;
  }

  @CamundaQueryParam(value = "createdAfter", converter = DateConverter.class)
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @CamundaQueryParam(value = "createdAfterExpression")
  public void setCreatedAfterExpression(String createdAfterExpression) {
    this.createdAfterExpression = createdAfterExpression;
  }

  @CamundaQueryParam(value = "createdBefore", converter = DateConverter.class)
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @CamundaQueryParam(value = "createdBeforeExpression")
  public void setCreatedBeforeExpression(String createdBeforeExpression) {
    this.createdBeforeExpression = createdBeforeExpression;
  }

  @CamundaQueryParam(value = "created", converter = DateConverter.class)
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @CamundaQueryParam(value = "createdOnExpression")
  public void setCreatedOnExpression(String createdOnExpression) {
    this.createdOnExpression = createdOnExpression;
  }

  @CamundaQueryParam(value = "delegationState")
  public void setDelegationState(String taskDelegationState) {
    this.delegationState = taskDelegationState;
  }

  @CamundaQueryParam(value = "candidateGroups", converter = StringListConverter.class)
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  @CamundaQueryParam(value = "candidateGroupsExpression")
  public void setCandidateGroupsExpression(String candidateGroupsExpression) {
    this.candidateGroupsExpression = candidateGroupsExpression;
  }

  @CamundaQueryParam(value = "taskVariables", converter = VariableListConverter.class)
  public void setTaskVariables(List<VariableQueryParameterDto> taskVariables) {
    this.taskVariables = taskVariables;
  }

  @CamundaQueryParam(value = "processVariables", converter = VariableListConverter.class)
  public void setProcessVariables(List<VariableQueryParameterDto> processVariables) {
    this.processVariables = processVariables;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseDefinitionKey")
  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  @CamundaQueryParam("caseDefinitionName")
  public void setCaseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
  }

  @CamundaQueryParam("caseDefinitionNameLike")
  public void setCaseDefinitionNameLike(String caseDefinitionNameLike) {
    this.caseDefinitionNameLike = caseDefinitionNameLike;
  }

  @CamundaQueryParam("caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam("caseInstanceBusinessKey")
  public void setCaseInstanceBusinessKey(String caseInstanceBusinessKey) {
    this.caseInstanceBusinessKey = caseInstanceBusinessKey;
  }

  @CamundaQueryParam("caseInstanceBusinessKeyLike")
  public void setCaseInstanceBusinessKeyLike(String caseInstanceBusinessKeyLike) {
    this.caseInstanceBusinessKeyLike = caseInstanceBusinessKeyLike;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value = "caseInstanceVariables", converter = VariableListConverter.class)
  public void setCaseInstanceVariables(List<VariableQueryParameterDto> caseInstanceVariables) {
    this.caseInstanceVariables = caseInstanceVariables;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected TaskQuery createNewQuery(ProcessEngine engine) {
    return engine.getTaskService().createTaskQuery();
  }

  @Override
  protected void applyFilters(TaskQuery query) {
    if (processInstanceBusinessKey != null) {
      query.processInstanceBusinessKey(processInstanceBusinessKey);
    }
    if (processInstanceBusinessKeyLike != null) {
      query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (processDefinitionName != null) {
      query.processDefinitionName(processDefinitionName);
    }
    if (processDefinitionNameLike != null) {
      query.processDefinitionNameLike(processDefinitionNameLike);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (assignee != null) {
      query.taskAssignee(assignee);
    }
    if (assigneeExpression != null) {
      query.taskAssigneeExpression(assigneeExpression);
    }
    if (assigneeLike != null) {
      query.taskAssigneeLike(assigneeLike);
    }
    if (assigneeLikeExpression != null) {
      query.taskAssigneeLikeExpression(assigneeLikeExpression);
    }
    if (candidateGroup != null) {
      query.taskCandidateGroup(candidateGroup);
    }
    if (candidateGroupExpression != null) {
      query.taskCandidateGroupExpression(candidateGroupExpression);
    }
    if (candidateUser != null) {
      query.taskCandidateUser(candidateUser);
    }
    if (candidateUserExpression != null) {
      query.taskCandidateUserExpression(candidateUserExpression);
    }
    if (taskDefinitionKey != null) {
      query.taskDefinitionKey(taskDefinitionKey);
    }
    if (taskDefinitionKeyLike != null) {
      query.taskDefinitionKeyLike(taskDefinitionKeyLike);
    }
    if (description != null) {
      query.taskDescription(description);
    }
    if (descriptionLike != null) {
      query.taskDescriptionLike(descriptionLike);
    }
    if (involvedUser != null) {
      query.taskInvolvedUser(involvedUser);
    }
    if (involvedUserExpression != null) {
      query.taskInvolvedUserExpression(involvedUserExpression);
    }
    if (maxPriority != null) {
      query.taskMaxPriority(maxPriority);
    }
    if (minPriority != null) {
      query.taskMinPriority(minPriority);
    }
    if (name != null) {
      query.taskName(name);
    }
    if (nameLike != null) {
      query.taskNameLike(nameLike);
    }
    if (owner != null) {
      query.taskOwner(owner);
    }
    if (ownerExpression != null) {
      query.taskOwnerExpression(ownerExpression);
    }
    if (priority != null) {
      query.taskPriority(priority);
    }
    if (unassigned != null && unassigned == true) {
      query.taskUnassigned();
    }
    if (dueAfter != null) {
      query.dueAfter(dueAfter);
    }
    if (dueAfterExpression != null) {
      query.dueAfterExpression(dueAfterExpression);
    }
    if (dueBefore != null) {
      query.dueBefore(dueBefore);
    }
    if (dueBeforeExpression != null) {
      query.dueBeforeExpression(dueBeforeExpression);
    }
    if (dueDate != null) {
      query.dueDate(dueDate);
    }
    if (dueDateExpression != null) {
      query.dueDateExpression(dueDateExpression);
    }
    if (followUpAfter != null) {
      query.followUpAfter(followUpAfter);
    }
    if (followUpAfterExpression != null) {
      query.followUpAfterExpression(followUpAfterExpression);
    }
    if (followUpBefore != null) {
      query.followUpBefore(followUpBefore);
    }
    if (followUpBeforeExpression != null) {
      query.followUpBeforeExpression(followUpBeforeExpression);
    }
    if (followUpDate != null) {
      query.followUpDate(followUpDate);
    }
    if (followUpDateExpression != null) {
      query.followUpDateExpression(followUpDateExpression);
    }
    if (createdAfter != null) {
      query.taskCreatedAfter(createdAfter);
    }
    if (createdAfterExpression != null) {
      query.taskCreatedAfterExpression(createdAfterExpression);
    }
    if (createdBefore != null) {
      query.taskCreatedBefore(createdBefore);
    }
    if (createdBeforeExpression != null) {
      query.taskCreatedBeforeExpression(createdBeforeExpression);
    }
    if (createdOn != null) {
      query.taskCreatedOn(createdOn);
    }
    if (createdOnExpression != null) {
      query.taskCreatedOnExpression(createdOnExpression);
    }
    if (delegationState != null) {
      DelegationStateConverter converter = new DelegationStateConverter();
      DelegationState state = converter.convertQueryParameterToType(delegationState);
      query.taskDelegationState(state);
    }
    if (candidateGroups != null) {
      query.taskCandidateGroupIn(candidateGroups);
    }
    if (candidateGroupsExpression != null) {
      query.taskCandidateGroupInExpression(candidateGroupsExpression);
    }
    if (active != null && active == true) {
      query.active();
    }
    if (suspended != null && suspended == true) {
      query.suspended();
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseDefinitionKey != null) {
      query.caseDefinitionKey(caseDefinitionKey);
    }
    if (caseDefinitionName != null) {
      query.caseDefinitionName(caseDefinitionName);
    }
    if (caseDefinitionNameLike != null) {
      query.caseDefinitionNameLike(caseDefinitionNameLike);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (caseInstanceBusinessKey != null) {
      query.caseInstanceBusinessKey(caseInstanceBusinessKey);
    }
    if (caseInstanceBusinessKeyLike != null) {
      query.caseInstanceBusinessKeyLike(caseInstanceBusinessKeyLike);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }

    if (taskVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : taskVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.taskVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.taskVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid task variable comparator specified: " + op);
        }

      }
    }

    if (processVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : processVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.processVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.processVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.processVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.processVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid process variable comparator specified: " + op);
        }

      }
    }

    if (caseInstanceVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : caseInstanceVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.caseInstanceVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid case variable comparator specified: " + op);
        }

      }
    }
  }

  @Override
  protected void applySortingOptions(TaskQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
        query.orderByProcessInstanceId();
      } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_ID_VALUE)) {
        query.orderByCaseInstanceId();
      } else if (sortBy.equals(SORT_BY_DUE_DATE_VALUE)) {
        query.orderByDueDate();
      } else if (sortBy.equals(SORT_BY_FOLLOW_UP_VALUE)) {
        query.orderByFollowUpDate();
      } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
        query.orderByExecutionId();
      } else if (sortBy.equals(SORT_BY_CASE_EXECUTION_ID_VALUE)) {
        query.orderByCaseExecutionId();
      } else if (sortBy.equals(SORT_BY_ASSIGNEE_VALUE)) {
        query.orderByTaskAssignee();
      } else if (sortBy.equals(SORT_BY_CREATE_TIME_VALUE)) {
        query.orderByTaskCreateTime();
      } else if (sortBy.equals(SORT_BY_DESCRIPTION_VALUE)) {
        query.orderByTaskDescription();
      } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
        query.orderByTaskId();
      } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
        query.orderByTaskName();
      } else if (sortBy.equals(SORT_BY_PRIORITY_VALUE)) {
        query.orderByTaskPriority();
      }
    }

    if (sortOrder != null) {
      if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
        query.asc();
      } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
        query.desc();
      }
    }
  }

}
