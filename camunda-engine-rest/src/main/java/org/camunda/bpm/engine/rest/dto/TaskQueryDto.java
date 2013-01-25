package org.camunda.bpm.engine.rest.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.joda.time.DateTime;

public class TaskQueryDto extends SortableParameterizedQueryDto {
  
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
  
  private Date dueAfter;
  private Date dueBefore;
  private Date dueDate;
  private Date createdAfter;
  private Date createdBefore;
  private Date createdOn;
  
  private DelegationState delegationState;
  
  private List<String> candidateGroups;
  
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

  @CamundaQueryParam("candidate")
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

  @CamundaQueryParam("involved")
  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }

  @CamundaQueryParam("maxPriority")
  public void setMaxPriority(Integer maxPriority) {
    this.maxPriority = maxPriority;
  }

  @CamundaQueryParam("minPriority")
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

  @CamundaQueryParam("priority")
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  @CamundaQueryParam("unassigned")
  public void setUnassigned(Boolean unassigned) {
    this.unassigned = unassigned;
  }

  @CamundaQueryParam("dueAfter")
  public void setDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
  }

  @CamundaQueryParam("dueBefore")
  public void setDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
  }

  @CamundaQueryParam("due")
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  @CamundaQueryParam("createdAfter")
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @CamundaQueryParam("createdBefore")
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @CamundaQueryParam("created")
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @CamundaQueryParam("delegationState")
  public void setDelegationState(DelegationState taskDelegationState) {
    this.delegationState = taskDelegationState;
  }

  @CamundaQueryParam("candidateGroups")
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }
  
  public TaskQuery toQuery(TaskService taskService) {
    TaskQuery query = taskService.createTaskQuery();
    
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

    if (!sortOptionsValid()) {
      throw new InvalidRequestException("You may not specify a single sorting parameter.");
    }
    
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
    
    return query;
  }

  @Override
  public void setPropertyFromParameterPair(String key, String value) {
    try {
      if (key.equals("maxPriority") || key.equals("minPriority") || key.equals("priority")) {
        Integer intValue = new Integer(value);
        setValueBasedOnAnnotation(key, intValue);
      } else if (key.equals("unassigned")) {
        Boolean booleanValue = new Boolean(value);
        setValueBasedOnAnnotation(key, booleanValue);
      } else if (key.startsWith("due") || key.startsWith("created")) {
        Date dateValue = DateTime.parse(value).toDate();
        setValueBasedOnAnnotation(key, dateValue);
      } else if (key.equals("delegationState")) {
        DelegationState state = DelegationState.valueOf(value.toUpperCase());
        setValueBasedOnAnnotation(key, state);
      } else if (key.equals("candidateGroups")) {
        List<String> candidateGroups = Arrays.asList(value.split(","));
        setValueBasedOnAnnotation(key, candidateGroups);
      } else {
        setValueBasedOnAnnotation(key, value);
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Cannot set parameter.");
    } catch (IllegalAccessException e) {
      throw new RestException("Cannot set parameter.");
    } catch (InvocationTargetException e) {
      throw new InvalidRequestException(e.getTargetException().getMessage());
    }
  }
}
