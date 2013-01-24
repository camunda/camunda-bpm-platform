package org.camunda.bpm.engine.rest.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.joda.time.DateTime;

public class TaskQueryDto extends SortableParameterizedQueryDto {
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

  @Override
  protected boolean isValidSortByValue(String value) {
    // TODO Auto-generated method stub
    return false;
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
