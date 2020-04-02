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

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.ExternalTaskState;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.*;

public class HistoricExternalTaskLogQueryImpl extends AbstractQuery<HistoricExternalTaskLogQuery, HistoricExternalTaskLog> implements HistoricExternalTaskLogQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String externalTaskId;
  protected String topicName;
  protected String workerId;
  protected String errorMessage;
  protected String[] activityIds;
  protected String[] activityInstanceIds;
  protected String[] executionIds;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected Long priorityHigherThanOrEqual;
  protected Long priorityLowerThanOrEqual;
  protected String[] tenantIds;
  protected boolean isTenantIdSet;
  protected ExternalTaskState state;

  public HistoricExternalTaskLogQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // query parameter ////////////////////////////////////////////

  @Override
  public HistoricExternalTaskLogQuery logId(String historicExternalTaskLogId) {
    ensureNotNull(NotValidException.class, "historicExternalTaskLogId", historicExternalTaskLogId);
    this.id = historicExternalTaskLogId;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery externalTaskId(String externalTaskId) {
    ensureNotNull(NotValidException.class, "externalTaskId", externalTaskId);
    this.externalTaskId = externalTaskId;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery topicName(String topicName) {
    ensureNotNull(NotValidException.class, "topicName", topicName);
    this.topicName = topicName;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery workerId(String workerId) {
    ensureNotNull(NotValidException.class, "workerId", workerId);
    this.workerId = workerId;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery errorMessage(String errorMessage) {
    ensureNotNull(NotValidException.class, "errorMessage", errorMessage);
    this.errorMessage = errorMessage;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery activityIdIn(String... activityIds) {
    ensureNotNull(NotValidException.class, "activityIds", (Object[]) activityIds);
    List<String> activityIdList = CollectionUtil.asArrayList(activityIds);
    ensureNotContainsNull("activityIds", activityIdList);
    ensureNotContainsEmptyString("activityIds", activityIdList);
    this.activityIds = activityIds;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery activityInstanceIdIn(String... activityInstanceIds) {
    ensureNotNull(NotValidException.class, "activityIds", (Object[]) activityInstanceIds);
    List<String> activityInstanceIdList = CollectionUtil.asArrayList(activityInstanceIds);
    ensureNotContainsNull("activityInstanceIds", activityInstanceIdList);
    ensureNotContainsEmptyString("activityInstanceIds", activityInstanceIdList);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery executionIdIn(String... executionIds) {
    ensureNotNull(NotValidException.class, "activityIds", (Object[]) executionIds);
    List<String> executionIdList = CollectionUtil.asArrayList(executionIds);
    ensureNotContainsNull("executionIds", executionIdList);
    ensureNotContainsEmptyString("executionIds", executionIdList);
    this.executionIds = executionIds;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery processInstanceId(String processInstanceId) {
    ensureNotNull(NotValidException.class, "processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull(NotValidException.class, "processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull(NotValidException.class, "processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery withoutTenantId() {
    this.tenantIds = null;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery priorityHigherThanOrEquals(long priority) {
    this.priorityHigherThanOrEqual = priority;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery priorityLowerThanOrEquals(long priority) {
    this.priorityLowerThanOrEqual = priority;
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery creationLog() {
    setState(ExternalTaskState.CREATED);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery failureLog() {
    setState(ExternalTaskState.FAILED);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery successLog() {
    setState(ExternalTaskState.SUCCESSFUL);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery deletionLog() {
    setState(ExternalTaskState.DELETED);
    return this;
  }

  // order by //////////////////////////////////////////////


  @Override
  public HistoricExternalTaskLogQuery orderByTimestamp() {
    orderBy(HistoricExternalTaskLogQueryProperty.TIMESTAMP);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByExternalTaskId() {
    orderBy(HistoricExternalTaskLogQueryProperty.EXTERNAL_TASK_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByRetries() {
    orderBy(HistoricExternalTaskLogQueryProperty.RETRIES);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByPriority() {
    orderBy(HistoricExternalTaskLogQueryProperty.PRIORITY);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByTopicName() {
    orderBy(HistoricExternalTaskLogQueryProperty.TOPIC_NAME);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByWorkerId() {
    orderBy(HistoricExternalTaskLogQueryProperty.WORKER_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByActivityId() {
    orderBy(HistoricExternalTaskLogQueryProperty.ACTIVITY_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByActivityInstanceId() {
    orderBy(HistoricExternalTaskLogQueryProperty.ACTIVITY_INSTANCE_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByExecutionId() {
    orderBy(HistoricExternalTaskLogQueryProperty.EXECUTION_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByProcessInstanceId() {
    orderBy(HistoricExternalTaskLogQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByProcessDefinitionId() {
    orderBy(HistoricExternalTaskLogQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByProcessDefinitionKey() {
    orderBy(HistoricExternalTaskLogQueryProperty.PROCESS_DEFINITION_KEY);
    return this;
  }

  @Override
  public HistoricExternalTaskLogQuery orderByTenantId() {
    orderBy(HistoricExternalTaskLogQueryProperty.TENANT_ID);
    return this;
  }

  // results //////////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
        .getHistoricExternalTaskLogManager()
        .findHistoricExternalTaskLogsCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricExternalTaskLog> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getHistoricExternalTaskLogManager()
        .findHistoricExternalTaskLogsByQueryCriteria(this, page);
  }

  // getters & setters ////////////////////////////////////////////////////////////

  protected void setState(ExternalTaskState state) {
    this.state = state;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }
}
