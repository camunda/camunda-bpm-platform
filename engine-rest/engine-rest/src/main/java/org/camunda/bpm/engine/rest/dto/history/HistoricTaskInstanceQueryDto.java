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
package org.camunda.bpm.engine.rest.dto.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricTaskInstanceQueryDto extends AbstractQueryDto<HistoricTaskInstanceQuery>{

  private static final String SORT_BY_TASK_ID= "taskId";
  private static final String SORT_BY_ACT_INSTANCE_ID = "activityInstanceId";
  private static final String SORT_BY_PROC_DEF_ID = "processDefinitionId";
  private static final String SORT_BY_PROC_INST_ID = "processInstanceId";
  private static final String SORT_BY_EXEC_ID = "executionId";
  private static final String SORT_BY_CASE_DEF_ID = "caseDefinitionId";
  private static final String SORT_BY_CASE_INST_ID = "caseInstanceId";
  private static final String SORT_BY_CASE_EXEC_ID = "caseExecutionId";
  private static final String SORT_BY_TASK_DURATION = "duration";
  private static final String SORT_BY_END_TIME = "endTime";
  private static final String SORT_BY_START_TIME = "startTime";
  private static final String SORT_BY_TASK_NAME = "taskName";
  private static final String SORT_BY_TASK_DESC = "taskDescription";
  private static final String SORT_BY_ASSIGNEE = "assignee";
  private static final String SORT_BY_OWNER = "owner";
  private static final String SORT_BY_DUE_DATE = "dueDate";
  private static final String SORT_BY_FOLLOW_UP_DATE = "followUpDate";
  private static final String SORT_BY_DELETE_REASON = "deleteReason";
  private static final String SORT_BY_TASK_DEF_KEY = "taskDefinitionKey";
  private static final String SORT_BY_PRIORITY = "priority";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACT_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROC_DEF_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROC_INST_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXEC_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_DEF_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INST_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_EXEC_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_DURATION);
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_DURATION);
    VALID_SORT_BY_VALUES.add(SORT_BY_END_TIME);
    VALID_SORT_BY_VALUES.add(SORT_BY_START_TIME);
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_NAME);
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_DESC);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNEE);
    VALID_SORT_BY_VALUES.add(SORT_BY_OWNER);
    VALID_SORT_BY_VALUES.add(SORT_BY_DUE_DATE);
    VALID_SORT_BY_VALUES.add(SORT_BY_FOLLOW_UP_DATE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DELETE_REASON);
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_DEF_KEY);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String taskId;
  protected String taskParentTaskId;
  protected String processInstanceId;
  protected String processInstanceBusinessKey;
  protected String[] processInstanceBusinessKeyIn;
  protected String processInstanceBusinessKeyLike;
  protected String executionId;
  protected String[] activityInstanceIdIn;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected String taskName;
  protected String taskNameLike;
  protected String taskDescription;
  protected String taskDescriptionLike;
  protected String taskDefinitionKey;
  protected String[] taskDefinitionKeyIn;
  protected String taskDeleteReason;
  protected String taskDeleteReasonLike;
  protected Boolean assigned;
  protected Boolean unassigned;
  protected String taskAssignee;
  protected String taskAssigneeLike;
  protected String taskOwner;
  protected String taskOwnerLike;
  protected Integer taskPriority;
  protected Boolean finished;
  protected Boolean unfinished;
  protected Boolean processFinished;
  protected Boolean processUnfinished;
  protected Date taskDueDate;
  protected Date taskDueDateBefore;
  protected Date taskDueDateAfter;
  protected Date taskFollowUpDate;
  protected Date taskFollowUpDateBefore;
  protected Date taskFollowUpDateAfter;
  private List<String> tenantIds;

  protected Date startedBefore;
  protected Date startedAfter;
  protected Date finishedBefore;
  protected Date finishedAfter;

  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionName;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskInvolvedUser;
  protected String taskInvolvedGroup;
  protected String taskHadCandidateUser;
  protected String taskHadCandidateGroup;
  protected Boolean withCandidateGroups;
  protected Boolean withoutCandidateGroups;
  protected List<VariableQueryParameterDto> taskVariables;
  protected List<VariableQueryParameterDto> processVariables;

  public HistoricTaskInstanceQueryDto() {}

  public HistoricTaskInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("taskId")
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @CamundaQueryParam("taskParentTaskId")
  public void setTaskParentTaskId(String taskParentTaskId) {
    this.taskParentTaskId = taskParentTaskId;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String businessKey) {
    this.processInstanceBusinessKey = businessKey;
  }

  @CamundaQueryParam(value = "processInstanceBusinessKeyIn", converter = StringArrayConverter.class)
  public void setProcessInstanceBusinessKeyIn(String[] processInstanceBusinessKeyIn) {
    this.processInstanceBusinessKeyIn = processInstanceBusinessKeyIn;
  }

  @CamundaQueryParam("processInstanceBusinessKeyLike")
  public void setProcessInstanceBusinessKeyLike(String businessKeyLike) {
    this.processInstanceBusinessKeyLike = businessKeyLike;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter=StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("processDefinitionName")
  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @CamundaQueryParam("taskName")
  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  @CamundaQueryParam("taskNameLike")
  public void setTaskNameLike(String taskNameLike) {
    this.taskNameLike = taskNameLike;
  }

  @CamundaQueryParam("taskDescription")
  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }

  @CamundaQueryParam("taskDescriptionLike")
  public void setTaskDescriptionLike(String taskDescriptionLike) {
    this.taskDescriptionLike = taskDescriptionLike;
  }

  @CamundaQueryParam("taskDefinitionKey")
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  @CamundaQueryParam(value="taskDefinitionKeyIn", converter=StringArrayConverter.class)
  public void setTaskDefinitionKeyIn(String[] taskDefinitionKeyIn) {
    this.taskDefinitionKeyIn = taskDefinitionKeyIn;
  }

  @CamundaQueryParam("taskDeleteReason")
  public void setTaskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
  }

  @CamundaQueryParam("taskDeleteReasonLike")
  public void setTaskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
  }

  @CamundaQueryParam(value="assigned", converter=BooleanConverter.class)
  public void setAssigned(Boolean assigned) {
    this.assigned = assigned;
  }

  @CamundaQueryParam(value="unassigned", converter=BooleanConverter.class)
  public void setUnassigned(Boolean unassigned) {
    this.unassigned = unassigned;
  }

  @CamundaQueryParam("taskAssignee")
  public void setTaskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
  }

  @CamundaQueryParam("taskAssigneeLike")
  public void setTaskAssigneeLike(String taskAssigneeLike) {
    this.taskAssigneeLike = taskAssigneeLike;
  }

  @CamundaQueryParam("taskOwner")
  public void setTaskOwner(String taskOwner) {
    this.taskOwner = taskOwner;
  }

  @CamundaQueryParam("taskOwnerLike")
  public void setTaskOwnerLike(String taskOwnerLike) {
    this.taskOwnerLike = taskOwnerLike;
  }

  @CamundaQueryParam(value="taskPriority", converter=IntegerConverter.class)
  public void setTaskPriority(Integer taskPriority) {
    this.taskPriority = taskPriority;
  }

  @CamundaQueryParam(value="finished", converter=BooleanConverter.class)
  public void setFinished(Boolean finished) {
    this.finished = finished;
  }

  @CamundaQueryParam(value="unfinished", converter=BooleanConverter.class)
  public void setUnfinished(Boolean unfinished) {
    this.unfinished = unfinished;
  }

  @CamundaQueryParam(value="processFinished", converter=BooleanConverter.class)
  public void setProcessFinished(Boolean processFinished) {
    this.processFinished = processFinished;
  }

  @CamundaQueryParam(value="processUnfinished", converter=BooleanConverter.class)
  public void setProcessUnfinished(Boolean processUnfinished) {
    this.processUnfinished = processUnfinished;
  }

  @CamundaQueryParam(value="taskDueDate", converter=DateConverter.class)
  public void setTaskDueDate(Date taskDueDate) {
    this.taskDueDate = taskDueDate;
  }

  @CamundaQueryParam(value="taskDueDateBefore", converter=DateConverter.class)
  public void setTaskDueDateBefore(Date taskDueDateBefore) {
    this.taskDueDateBefore = taskDueDateBefore;
  }

  @CamundaQueryParam(value="taskDueDateAfter", converter=DateConverter.class)
  public void setTaskDueDateAfter(Date taskDueDateAfter) {
    this.taskDueDateAfter = taskDueDateAfter;
  }

  @CamundaQueryParam(value="taskFollowUpDate", converter=DateConverter.class)
  public void setTaskFollowUpDate(Date taskFollowUpDate) {
    this.taskFollowUpDate = taskFollowUpDate;
  }

  @CamundaQueryParam(value="taskFollowUpDateBefore", converter=DateConverter.class)
  public void setTaskFollowUpDateBefore(Date taskFollowUpDateBefore) {
    this.taskFollowUpDateBefore = taskFollowUpDateBefore;
  }

  @CamundaQueryParam(value="taskFollowUpDateAfter", converter=DateConverter.class)
  public void setTaskFollowUpDateAfter(Date taskFollowUpDateAfter) {
    this.taskFollowUpDateAfter = taskFollowUpDateAfter;
  }

  @CamundaQueryParam(value="taskVariables", converter = VariableListConverter.class)
  public void setTaskVariables(List<VariableQueryParameterDto> taskVariables) {
    this.taskVariables = taskVariables;
  }

  @CamundaQueryParam(value="processVariables", converter = VariableListConverter.class)
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

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam("caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam("taskInvolvedUser")
  public void setTaskInvolvedUser(String taskInvolvedUser) {
    this.taskInvolvedUser = taskInvolvedUser;
  }

  @CamundaQueryParam("taskInvolvedGroup")
  public void setTaskInvolvedGroup(String taskInvolvedGroup) {
    this.taskInvolvedGroup = taskInvolvedGroup;
  }

  @CamundaQueryParam("taskHadCandidateUser")
  public void setTaskHadCandidateUser(String taskHadCandidateUser) {
    this.taskHadCandidateUser = taskHadCandidateUser;
  }

  @CamundaQueryParam("taskHadCandidateGroup")
  public void setTaskHadCandidateGroup(String taskHadCandidateGroup) {
    this.taskHadCandidateGroup = taskHadCandidateGroup;
  }

  @CamundaQueryParam(value="withCandidateGroups", converter=BooleanConverter.class)
  public void setWithCandidateGroups(Boolean withCandidateGroups) {
    this.withCandidateGroups = withCandidateGroups;
  }

  @CamundaQueryParam(value="withoutCandidateGroups", converter=BooleanConverter.class)
  public void setWithoutCandidateGroups(Boolean withoutCandidateGroups) {
    this.withoutCandidateGroups = withoutCandidateGroups;
  }

  @CamundaQueryParam(value="startedBefore", converter=DateConverter.class)
  public void setStartedBefore(Date startedBefore) {
    this.startedBefore = startedBefore;
  }

  @CamundaQueryParam(value="startedAfter", converter=DateConverter.class)
  public void setStartedAfter(Date startedAfter) {
    this.startedAfter = startedAfter;
  }

  @CamundaQueryParam(value="finishedBefore", converter=DateConverter.class)
  public void setFinishedBefore(Date finishedBefore) {
    this.finishedBefore = finishedBefore;
  }

  @CamundaQueryParam(value="finishedAfter", converter=DateConverter.class)
  public void setFinishedAfter(Date finishedAfter) {
    this.finishedAfter = finishedAfter;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricTaskInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricTaskInstanceQuery();
  }

  @Override
  protected void applyFilters(HistoricTaskInstanceQuery query) {
    if (taskId != null) {
      query.taskId(taskId);
    }
    if (taskParentTaskId != null) {
      query.taskParentTaskId(taskParentTaskId);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (processInstanceBusinessKey != null) {
      query.processInstanceBusinessKey(processInstanceBusinessKey);
    }
    if (processInstanceBusinessKeyIn != null && processInstanceBusinessKeyIn.length > 0) {
      query.processInstanceBusinessKeyIn(processInstanceBusinessKeyIn);
    }
    if (processInstanceBusinessKeyLike != null) {
      query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0 ) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processDefinitionName != null) {
      query.processDefinitionName(processDefinitionName);
    }
    if (taskName != null) {
      query.taskName(taskName);
    }
    if (taskNameLike != null) {
      query.taskNameLike(taskNameLike);
    }
    if (taskDescription != null) {
      query.taskDescription(taskDescription);
    }
    if (taskDescriptionLike != null) {
      query.taskDescriptionLike(taskDescriptionLike);
    }
    if (taskDefinitionKey != null) {
      query.taskDefinitionKey(taskDefinitionKey);
    }
    if (taskDefinitionKeyIn != null && taskDefinitionKeyIn.length > 0) {
      query.taskDefinitionKeyIn(taskDefinitionKeyIn);
    }
    if (taskDeleteReason != null) {
      query.taskDeleteReason(taskDeleteReason);
    }
    if (taskDeleteReasonLike != null) {
      query.taskDeleteReasonLike(taskDeleteReasonLike);
    }
    if (assigned != null) {
      query.taskAssigned();
    }
    if (unassigned != null) {
      query.taskUnassigned();
    }
    if (taskAssignee != null) {
      query.taskAssignee(taskAssignee);
    }
    if (taskAssigneeLike != null) {
      query.taskAssigneeLike(taskAssigneeLike);
    }
    if (taskOwner != null) {
      query.taskOwner(taskOwner);
    }
    if (taskOwnerLike != null) {
      query.taskOwnerLike(taskOwnerLike);
    }
    if (taskPriority != null) {
      query.taskPriority(taskPriority);
    }
    if (finished != null) {
      query.finished();
    }
    if (unfinished != null) {
      query.unfinished();
    }
    if (processFinished != null) {
      query.processFinished();
    }
    if (processUnfinished != null) {
      query.processUnfinished();
    }
    if (taskDueDate != null) {
      query.taskDueDate(taskDueDate);
    }
    if (taskDueDateBefore != null) {
      query.taskDueBefore(taskDueDateBefore);
    }
    if (taskDueDateAfter != null) {
      query.taskDueAfter(taskDueDateAfter);
    }
    if (taskFollowUpDate != null) {
      query.taskFollowUpDate(taskFollowUpDate);
    }
    if (taskFollowUpDateBefore != null) {
      query.taskFollowUpBefore(taskFollowUpDateBefore);
    }
    if (taskFollowUpDateAfter != null) {
      query.taskFollowUpAfter(taskFollowUpDateAfter);
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
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if(taskInvolvedUser != null){
      query.taskInvolvedUser(taskInvolvedUser);
    }
    if(taskInvolvedGroup != null){
      query.taskInvolvedGroup(taskInvolvedGroup);
    }
    if(taskHadCandidateUser != null){
      query.taskHadCandidateUser(taskHadCandidateUser);
    }
    if(taskHadCandidateGroup != null){
      query.taskHadCandidateGroup(taskHadCandidateGroup);
    }
    if (withCandidateGroups != null) {
      query.withCandidateGroups();
    }
    if (withoutCandidateGroups != null) {
      query.withoutCandidateGroups();
    }

    if (finishedAfter != null) {
      query.finishedAfter(finishedAfter);
    }

    if (finishedBefore != null) {
      query.finishedBefore(finishedBefore);
    }

    if (startedAfter != null) {
      query.startedAfter(startedAfter);
    }

    if (startedBefore != null) {
      query.startedBefore(startedBefore);
    }

    if (taskVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : taskVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueEquals(variableName, variableValue);
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid variable comparator specified: " + op);
        }
      }
    }

    if (processVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : processVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

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
  }

  @Override
  protected void applySortBy(HistoricTaskInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_TASK_ID)) {
      query.orderByTaskId();
    } else if (sortBy.equals(SORT_BY_ACT_INSTANCE_ID)) {
      query.orderByHistoricActivityInstanceId();
    } else if (sortBy.equals(SORT_BY_PROC_DEF_ID)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROC_INST_ID)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_EXEC_ID)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_TASK_DURATION)) {
      query.orderByHistoricTaskInstanceDuration();
    } else if (sortBy.equals(SORT_BY_END_TIME)) {
      query.orderByHistoricTaskInstanceEndTime();
    } else if (sortBy.equals(SORT_BY_START_TIME)) {
      query.orderByHistoricActivityInstanceStartTime();
    } else if (sortBy.equals(SORT_BY_TASK_NAME)) {
      query.orderByTaskName();
    } else if (sortBy.equals(SORT_BY_TASK_DESC)) {
      query.orderByTaskDescription();
    } else if (sortBy.equals(SORT_BY_ASSIGNEE)) {
      query.orderByTaskAssignee();
    } else if (sortBy.equals(SORT_BY_OWNER)) {
      query.orderByTaskOwner();
    } else if (sortBy.equals(SORT_BY_DUE_DATE)) {
      query.orderByTaskDueDate();
    } else if (sortBy.equals(SORT_BY_FOLLOW_UP_DATE)) {
      query.orderByTaskFollowUpDate();
    } else if (sortBy.equals(SORT_BY_DELETE_REASON)) {
      query.orderByDeleteReason();
    } else if (sortBy.equals(SORT_BY_TASK_DEF_KEY)) {
      query.orderByTaskDefinitionKey();
    } else if (sortBy.equals(SORT_BY_PRIORITY)) {
      query.orderByTaskPriority();
    } else if (sortBy.equals(SORT_BY_CASE_DEF_ID)) {
      query.orderByCaseDefinitionId();
    } else if (sortBy.equals(SORT_BY_CASE_INST_ID)) {
      query.orderByCaseInstanceId();
    } else if (sortBy.equals(SORT_BY_CASE_EXEC_ID)) {
      query.orderByCaseExecutionId();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}

