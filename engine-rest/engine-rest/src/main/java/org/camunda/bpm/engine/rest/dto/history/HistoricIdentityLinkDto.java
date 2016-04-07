package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricIdentityLink;

public class HistoricIdentityLinkDto {
  protected String identityLinkId;
  protected Date time;
  protected String identityLinkType;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefId;
  protected String operationType;
  protected String assignerId;

  public String getIdentityLinkId() {
    return identityLinkId;
  }

  public Date getTime() {
    return time;
  }

  public String getIdentityLinkType() {
    return identityLinkType;
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

  public String getProcessDefId() {
    return processDefId;
  }

  public String getOperationType() {
    return operationType;
  }

  public String getAssignerId() {
    return assignerId;
  }

  public static HistoricIdentityLinkDto fromHistoricIdentityLink(HistoricIdentityLink historicIdentityLink) {
    HistoricIdentityLinkDto dto = new HistoricIdentityLinkDto();
    dto.assignerId = historicIdentityLink.getAssignerId();
    dto.groupId = historicIdentityLink.getGroupId();
    dto.operationType = historicIdentityLink.getOperationType();
    dto.taskId = historicIdentityLink.getTaskId();
    dto.time = historicIdentityLink.getTime();
    dto.identityLinkType = historicIdentityLink.getType();
    dto.userId = historicIdentityLink.getUserId();
    return dto;
  }
}
