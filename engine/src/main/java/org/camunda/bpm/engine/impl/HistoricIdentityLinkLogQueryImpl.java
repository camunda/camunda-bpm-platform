/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

 /**
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogQueryImpl extends AbstractVariableQueryImpl<HistoricIdentityLinkLogQuery, HistoricIdentityLinkLog>
    implements HistoricIdentityLinkLogQuery {

  private static final long serialVersionUID = 1L;
  protected Date dateBefore;
  protected Date dateAfter;
  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String operationType;
  protected String assignerId;
  protected String[] tenantIds;
  protected boolean isTenantIdSet;

   public HistoricIdentityLinkLogQueryImpl() {
  }

  public HistoricIdentityLinkLogQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
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

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

   public HistoricIdentityLinkLogQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    this.isTenantIdSet = true;
    return this;
  }

   @Override
   public HistoricIdentityLinkLogQuery withoutTenantId() {
     this.tenantIds = null;
     this.isTenantIdSet = true;
     return this;
   }

   public Date getDateBefore() {
    return dateBefore;
  }

  public Date getDateAfter() {
    return dateAfter;
  }

  @Override
  public HistoricIdentityLinkLogQuery type(String type) {
    ensureNotNull("type", type);
    this.type = type;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery dateBefore(Date dateBefore) {
    ensureNotNull("dateBefore", dateBefore);
    this.dateBefore = dateBefore;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery dateAfter(Date dateAfter) {
    ensureNotNull("dateAfter", dateAfter);
    this.dateAfter = dateAfter;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery userId(String userId) {
    ensureNotNull("userId", userId);
    this.userId = userId;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery groupId(String groupId) {
    ensureNotNull("groupId", groupId);
    this.groupId = groupId;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery taskId(String taskId) {
    ensureNotNull("taskId", taskId);
    this.taskId = taskId;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull("processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery operationType(String operationType) {
    ensureNotNull("operationType", operationType);
    this.operationType = operationType;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery assignerId(String assignerId) {
    ensureNotNull("assignerId", assignerId);
    this.assignerId = assignerId;
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByTime() {
    orderBy(HistoricIdentityLinkLogQueryProperty.TIME);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByType() {
    orderBy(HistoricIdentityLinkLogQueryProperty.TYPE);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByUserId() {
    orderBy(HistoricIdentityLinkLogQueryProperty.USER_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByGroupId() {
    orderBy(HistoricIdentityLinkLogQueryProperty.GROUP_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByTaskId() {
    orderBy(HistoricIdentityLinkLogQueryProperty.TASK_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByProcessDefinitionId() {
    orderBy(HistoricIdentityLinkLogQueryProperty.PROC_DEFINITION_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByProcessDefinitionKey() {
    orderBy(HistoricIdentityLinkLogQueryProperty.PROC_DEFINITION_KEY);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByOperationType() {
    orderBy(HistoricIdentityLinkLogQueryProperty.OPERATION_TYPE);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByAssignerId() {
    orderBy(HistoricIdentityLinkLogQueryProperty.ASSIGNER_ID);
    return this;
  }

  @Override
  public HistoricIdentityLinkLogQuery orderByTenantId() {
    orderBy(HistoricIdentityLinkLogQueryProperty.TENANT_ID);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricIdentityLinkManager()
      .findHistoricIdentityLinkLogCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricIdentityLinkLog> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricIdentityLinkManager()
      .findHistoricIdentityLinkLogByQueryCriteria(this, page);
  }
}
