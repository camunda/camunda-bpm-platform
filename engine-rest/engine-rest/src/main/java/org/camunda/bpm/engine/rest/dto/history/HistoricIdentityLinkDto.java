package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricIdentityLink;

public class HistoricIdentityLinkDto {
  protected String id;
  protected Date time;
  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefinitionId;
  protected String operationType;
  protected String assignerId;

  public String getId() {
    return id;
  }

  public Date getTime() {
    return time;
  }

  public String getType() {
    return type;
  }

  public String getUserId() {
    return userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getOperationType() {
    return operationType;
  }

  public String getAssignerId() {
    return assignerId;
  }

  public static HistoricIdentityLinkDto fromHistoricIdentityLink(HistoricIdentityLink historicIdentityLink) {
    HistoricIdentityLinkDto dto = new HistoricIdentityLinkDto();
    dto.id = historicIdentityLink.getId();
    dto.assignerId = historicIdentityLink.getAssignerId();
    dto.groupId = historicIdentityLink.getGroupId();
    dto.operationType = historicIdentityLink.getOperationType();
    dto.taskId = historicIdentityLink.getTaskId();
    dto.time = historicIdentityLink.getTime();
    dto.type = historicIdentityLink.getType();
    dto.processDefinitionId = historicIdentityLink.getProcessDefinitionId();
    dto.userId = historicIdentityLink.getUserId();
    return dto;
  }
}
