package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;

public class HistoricIdentityLinkLogDto {
  protected String id;
  protected Date time;
  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String operationType;
  protected String assignerId;
  protected String tenantId;

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

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  
  public String getOperationType() {
    return operationType;
  }

  public String getAssignerId() {
    return assignerId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static HistoricIdentityLinkLogDto fromHistoricIdentityLink(HistoricIdentityLinkLog historicIdentityLink) {
    HistoricIdentityLinkLogDto dto = new HistoricIdentityLinkLogDto();
    dto.id = historicIdentityLink.getId();
    dto.assignerId = historicIdentityLink.getAssignerId();
    dto.groupId = historicIdentityLink.getGroupId();
    dto.operationType = historicIdentityLink.getOperationType();
    dto.taskId = historicIdentityLink.getTaskId();
    dto.time = historicIdentityLink.getTime();
    dto.type = historicIdentityLink.getType();
    dto.processDefinitionId = historicIdentityLink.getProcessDefinitionId();
    dto.processDefinitionKey = historicIdentityLink.getProcessDefinitionKey();
    dto.userId = historicIdentityLink.getUserId();
    dto.tenantId = historicIdentityLink.getTenantId();
    return dto;
  }
}
