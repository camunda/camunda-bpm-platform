package org.camunda.bpm.engine.rest.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;

@XmlRootElement(name = "data")
public class TaskDto {

  private String id;
  private String name;
  private String assignee;
  private Date created;
  private Date due;
  private DelegationState delegationState;
  private String description;
  private String executionId;
  private String owner;
  private String parentTaskId;
  private int priority;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionKey;

  @XmlElement
  public String getId() {
    return id;
  }

  @XmlElement
  public String getName() {
    return name;
  }
  
  @XmlElement
  public String getAssignee() {
    return assignee;
  }

  @XmlElement
  public Date getCreated() {
    return created;
  }

  @XmlElement
  public Date getDue() {
    return due;
  }

  @XmlElement
  public DelegationState getDelegationState() {
    return delegationState;
  }

  @XmlElement
  public String getDescription() {
    return description;
  }

  @XmlElement
  public String getExecutionId() {
    return executionId;
  }

  @XmlElement
  public String getOwner() {
    return owner;
  }

  @XmlElement
  public String getParentTaskId() {
    return parentTaskId;
  }

  @XmlElement
  public int getPriority() {
    return priority;
  }

  @XmlElement
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @XmlElement
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  @XmlElement
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public static TaskDto fromTask(Task task) {
    TaskDto dto = new TaskDto();
    dto.id = task.getId();
    dto.name = task.getName();
    dto.assignee = task.getAssignee();
    dto.created = task.getCreateTime();
    dto.due = task.getDueDate();
    dto.delegationState = task.getDelegationState();
    dto.description = task.getDescription();
    dto.executionId = task.getExecutionId();
    dto.owner = task.getOwner();
    dto.parentTaskId = task.getParentTaskId();
    dto.priority = task.getPriority();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.taskDefinitionKey = task.getTaskDefinitionKey();
    return dto;
  }
}
