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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.*;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACT_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROC_DEF_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROC_INST_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXEC_ID);
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
  }

  protected String taskId;
  protected String taskParentTaskId;
  protected String processInstanceId;
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
  protected String taskDeleteReason;
  protected String taskDeleteReasonLike;
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

  protected List<VariableQueryParameterDto> taskVariables;
  protected List<VariableQueryParameterDto> processVariables;

  public HistoricTaskInstanceQueryDto() {}

  public HistoricTaskInstanceQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
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

  @CamundaQueryParam("taskDeleteReason")
  public void setTaskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
  }

  @CamundaQueryParam("taskDeleteReasonLike")
  public void setTaskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
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
    if (taskDeleteReason != null) {
      query.taskDeleteReason(taskDeleteReason);
    }
    if (taskDeleteReasonLike != null) {
      query.taskDeleteReasonLike(taskDeleteReasonLike);
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

    if (taskVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : taskVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getValue();

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
        Object variableValue = variableQueryParam.getValue();

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.processVariableValueEquals(variableName, variableValue);
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid variable comparator specified: " + op);
        }
      }
    }
  }

  @Override
  protected void applySortingOptions(HistoricTaskInstanceQuery query) {
    if (sortBy != null) {
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

