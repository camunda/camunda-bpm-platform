package org.camunda.bpm.engine.impl.history.event;

import java.util.Date;
/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogEventEntity extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  protected Date time;

  protected String type;

  protected String userId;

  protected String groupId;

  protected String taskId;

  protected String operationType;

  protected String assignerId;

  protected String tenantId;

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getOperationType() {
    return operationType;
  }

  public void setOperationType(String operationType) {
    this.operationType = operationType;
  }

  public String getAssignerId() {
    return assignerId;
  }

  public void setAssignerId(String assignerId) {
    this.assignerId = assignerId;
  }
  
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
