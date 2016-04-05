package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricIdentityLink;
import org.camunda.bpm.engine.history.HistoricIdentityLinkQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

 /**
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkQueryImpl extends AbstractVariableQueryImpl<HistoricIdentityLinkQuery, HistoricIdentityLink>
    implements HistoricIdentityLinkQuery {

  private static final long serialVersionUID = 1L;
  protected String identityLinkId;
  protected Date dateBefore;
  protected Date dateAfter;
  protected String identityLinkType;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefId;
  protected String operationType;
  protected String assignerId;

  public HistoricIdentityLinkQueryImpl() {
  }

  public HistoricIdentityLinkQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public String getIdentityLinkId() {
    return identityLinkId;
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

  public Date getDateBefore() {
    return dateBefore;
  }

  public Date getDateAfter() {
    return dateAfter;
  }

  @Override
  public HistoricIdentityLinkQuery identityLinkId(String identityLinkId) {
    ensureNotNull("id", identityLinkId);
    this.identityLinkId = identityLinkId;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery identityLinkType(String identityLinkType) {
    ensureNotNull("type", identityLinkType);
    this.identityLinkType = identityLinkType;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery dateBefore(Date dateBefore) {
    this.dateBefore = dateBefore;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery dateAfter(Date dateAfter) {
    this.dateAfter = dateAfter;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery userId(String userId) {
    ensureNotNull("userId", userId);
    this.userId = userId;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery groupId(String groupId) {
    ensureNotNull("groupId", groupId);
    this.groupId = groupId;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery taskId(String taskId) {
    ensureNotNull("taskId", taskId);
    this.taskId = taskId;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery processDefId(String processDefId) {
    ensureNotNull("processDefId", processDefId);
    this.processDefId = processDefId;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery operationType(String operationType) {
    ensureNotNull("operationType", operationType);
    this.operationType = operationType;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery assignerId(String assignerId) {
    ensureNotNull("assignerId", assignerId);
    this.assignerId = assignerId;
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByIdentityLinkId() {
    orderBy(HistoricIdentityLinkQueryProperty.ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByIdentityLinkType() {
    orderBy(HistoricIdentityLinkQueryProperty.TYPE);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByUserId() {
    orderBy(HistoricIdentityLinkQueryProperty.USER_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByGroupId() {
    orderBy(HistoricIdentityLinkQueryProperty.GROUP_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByTaskId() {
    orderBy(HistoricIdentityLinkQueryProperty.TASK_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByProcessDefId() {
    orderBy(HistoricIdentityLinkQueryProperty.PROC_DEF_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByOperationType() {
    orderBy(HistoricIdentityLinkQueryProperty.OPERATION_TYPE);
    return this;
  }

  @Override
  public HistoricIdentityLinkQuery orderByAssignerId() {
    orderBy(HistoricIdentityLinkQueryProperty.ASSIGNER_ID);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricIdentityLinkManager()
      .findHistoricIdentityLinkCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricIdentityLink> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricIdentityLinkManager()
      .findHistoricIdentityLinkByQueryCriteria(this, page);
  }

}
