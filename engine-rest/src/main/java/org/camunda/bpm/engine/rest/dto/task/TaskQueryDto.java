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
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
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
  private static final String SORT_BY_DUE_DATE_VALUE = "dueDate";
  private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
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
    VALID_SORT_BY_VALUES.add(SORT_BY_DUE_DATE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNEE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DESCRIPTION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY_VALUE);
  }

  private String processInstanceBusinessKey;
  private String processDefinitionKey;
  private String processDefinitionId;
  private String executionId;
  private String[] activityInstanceIdIn;
  private String processDefinitionName;
  private String processInstanceId;
  private String assignee;
  private String candidateGroup;
  private String candidateUser;
  private String taskDefinitionKey;
  private String taskDefinitionKeyLike;
  private String description;
  private String descriptionLike;
  private String involvedUser;
  private Integer maxPriority;
  private Integer minPriority;
  private String name;
  private String nameLike;
  private String owner;
  private Integer priority;
  private Boolean unassigned;
  private Boolean active;
  private Boolean suspended;

  private Date dueAfter;
  private Date dueBefore;
  private Date dueDate;
  private Date createdAfter;
  private Date createdBefore;
  private Date createdOn;

  private DelegationState delegationState;

  private List<String> candidateGroups;

  private List<VariableQueryParameterDto> taskVariables;
  private List<VariableQueryParameterDto> processVariables;
  
  public TaskQueryDto() {
    
  }
  
  public TaskQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  @CamundaQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String businessKey) {
    this.processInstanceBusinessKey = businessKey;
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

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("assignee")
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  @CamundaQueryParam("candidateGroup")
  public void setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
  }

  @CamundaQueryParam("candidateUser")
  public void setCandidateUser(String candidateUser) {
    this.candidateUser = candidateUser;
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

  @CamundaQueryParam(value = "dueBefore", converter = DateConverter.class)
  public void setDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
  }

  @CamundaQueryParam(value = "due", converter = DateConverter.class)
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  @CamundaQueryParam(value = "createdAfter", converter = DateConverter.class)
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @CamundaQueryParam(value = "createdBefore", converter = DateConverter.class)
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @CamundaQueryParam(value = "created", converter = DateConverter.class)
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @CamundaQueryParam(value = "delegationState", converter = DelegationStateConverter.class)
  public void setDelegationState(DelegationState taskDelegationState) {
    this.delegationState = taskDelegationState;
  }

  @CamundaQueryParam(value = "candidateGroups", converter = StringListConverter.class)
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  @CamundaQueryParam(value = "taskVariables", converter = VariableListConverter.class)
  public void setTaskVariables(List<VariableQueryParameterDto> taskVariables) {
    this.taskVariables = taskVariables;
  }

  @CamundaQueryParam(value = "processVariables", converter = VariableListConverter.class)
  public void setProcessVariables(List<VariableQueryParameterDto> processVariables) {
    this.processVariables = processVariables;
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
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (assignee != null) {
      query.taskAssignee(assignee);
    }
    if (candidateGroup != null) {
      query.taskCandidateGroup(candidateGroup);
    }
    if (candidateUser != null) {
      query.taskCandidateUser(candidateUser);
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
    if (priority != null) {
      query.taskPriority(priority);
    }
    if (unassigned != null && unassigned == true) {
      query.taskUnassigned();
    }
    if (dueAfter != null) {
      query.dueAfter(dueAfter);
    }
    if (dueBefore != null) {
      query.dueBefore(dueBefore);
    }
    if (dueDate != null) {
      query.dueDate(dueDate);
    }
    if (createdAfter != null) {
      query.taskCreatedAfter(createdAfter);
    }
    if (createdBefore != null) {
      query.taskCreatedBefore(createdBefore);
    }
    if (createdOn != null) {
      query.taskCreatedOn(createdOn);
    }
    if (delegationState != null) {
      query.taskDelegationState(delegationState);
    }
    if (candidateGroups != null) {
      query.taskCandidateGroupIn(candidateGroups);
    }
    if (active != null && active == true) {
      query.active();
    }
    if (suspended != null && suspended == true) {
      query.suspended();
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
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid process variable comparator specified: " + op);
        }
      }
    }
  }

  @Override
  protected void applySortingOptions(TaskQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
        query.orderByProcessInstanceId();
      } else if (sortBy.equals(SORT_BY_DUE_DATE_VALUE)) {
        query.orderByDueDate();
      } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
        query.orderByExecutionId();
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
