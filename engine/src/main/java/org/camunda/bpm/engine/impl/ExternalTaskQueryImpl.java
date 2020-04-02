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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public class ExternalTaskQueryImpl extends AbstractQuery<ExternalTaskQuery, ExternalTask> implements ExternalTaskQuery   {

  private static final long serialVersionUID = 1L;

  protected String externalTaskId;
  protected Set<String> externalTaskIds;
  protected String workerId;
  protected Date lockExpirationBefore;
  protected Date lockExpirationAfter;
  protected String topicName;
  protected Boolean locked;
  protected Boolean notLocked;
  protected String executionId;
  protected String processInstanceId;
  protected String[] processInstanceIdIn;
  protected String processDefinitionId;
  protected String activityId;
  protected String[] activityIdIn;
  protected SuspensionState suspensionState;
  protected Long priorityHigherThanOrEquals;
  protected Long priorityLowerThanOrEquals;
  protected Boolean retriesLeft;
  protected String[] tenantIds;

  public ExternalTaskQueryImpl() {
  }

  public ExternalTaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ExternalTaskQuery externalTaskId(String externalTaskId) {
    ensureNotNull("externalTaskId", externalTaskId);
    this.externalTaskId = externalTaskId;
    return this;
  }

  @Override
  public ExternalTaskQuery externalTaskIdIn(Set<String> externalTaskIds) {
    ensureNotEmpty("Set of external task ids", externalTaskIds);
    this.externalTaskIds = externalTaskIds;
    return this;
  }

  public ExternalTaskQuery workerId(String workerId) {
    ensureNotNull("workerId", workerId);
    this.workerId = workerId;
    return this;
  }

  public ExternalTaskQuery lockExpirationBefore(Date lockExpirationDate) {
    ensureNotNull("lockExpirationBefore", lockExpirationDate);
    this.lockExpirationBefore = lockExpirationDate;
    return this;
  }

  public ExternalTaskQuery lockExpirationAfter(Date lockExpirationDate) {
    ensureNotNull("lockExpirationAfter", lockExpirationDate);
    this.lockExpirationAfter = lockExpirationDate;
    return this;
  }

  public ExternalTaskQuery topicName(String topicName) {
    ensureNotNull("topicName", topicName);
    this.topicName = topicName;
    return this;
  }

  public ExternalTaskQuery locked() {
    this.locked = Boolean.TRUE;
    return this;
  }

  public ExternalTaskQuery notLocked() {
    this.notLocked = Boolean.TRUE;
    return this;
  }

  public ExternalTaskQuery executionId(String executionId) {
    ensureNotNull("executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  public ExternalTaskQuery processInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ExternalTaskQuery processInstanceIdIn(String... processInstanceIdIn) {
    ensureNotNull("processInstanceIdIn", (Object[]) processInstanceIdIn);
    this.processInstanceIdIn = processInstanceIdIn;
    return this;
  }

  public ExternalTaskQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ExternalTaskQuery activityId(String activityId) {
    ensureNotNull("activityId", activityId);
    this.activityId = activityId;
    return this;
  }

  public ExternalTaskQuery activityIdIn(String... activityIdIn) {
    ensureNotNull("activityIdIn", (Object[]) activityIdIn);
    this.activityIdIn = activityIdIn;
    return this;
  }
  @Override
  public ExternalTaskQuery priorityHigherThanOrEquals(long priority) {
    this.priorityHigherThanOrEquals = priority;
    return this;
  }

  @Override
  public ExternalTaskQuery priorityLowerThanOrEquals(long priority) {
    this.priorityLowerThanOrEquals = priority;
    return this;
  }


  public ExternalTaskQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public ExternalTaskQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public ExternalTaskQuery withRetriesLeft() {
    this.retriesLeft = Boolean.TRUE;
    return this;
  }

  public ExternalTaskQuery noRetriesLeft() {
    this.retriesLeft = Boolean.FALSE;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(priorityHigherThanOrEquals, priorityLowerThanOrEquals);
  }

  public ExternalTaskQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    return this;
  }

  public ExternalTaskQuery orderById() {
    return orderBy(ExternalTaskQueryProperty.ID);
  }

  public ExternalTaskQuery orderByLockExpirationTime() {
    return orderBy(ExternalTaskQueryProperty.LOCK_EXPIRATION_TIME);
  }

  public ExternalTaskQuery orderByProcessInstanceId() {
    return orderBy(ExternalTaskQueryProperty.PROCESS_INSTANCE_ID);
  }

  public ExternalTaskQuery orderByProcessDefinitionId() {
    return orderBy(ExternalTaskQueryProperty.PROCESS_DEFINITION_ID);
  }

  public ExternalTaskQuery orderByProcessDefinitionKey() {
    return orderBy(ExternalTaskQueryProperty.PROCESS_DEFINITION_KEY);
  }

  public ExternalTaskQuery orderByTenantId() {
    return orderBy(ExternalTaskQueryProperty.TENANT_ID);
  }

  @Override
  public ExternalTaskQuery orderByPriority() {
    return orderBy(ExternalTaskQueryProperty.PRIORITY);
  }
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getExternalTaskManager()
      .findExternalTaskCountByQueryCriteria(this);
  }

  @Override
  public List<ExternalTask> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getExternalTaskManager()
      .findExternalTasksByQueryCriteria(this);
  }

  public List<String> executeIdsList(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getExternalTaskManager()
      .findExternalTaskIdsByQueryCriteria(this);
  }

  @Override
  public List<ImmutablePair<String, String>> executeDeploymentIdMappingsList(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
        .getExternalTaskManager()
        .findDeploymentIdMappingsByQueryCriteria(this);
  }

  public String getExternalTaskId() {
    return externalTaskId;
  }

  public String getWorkerId() {
    return workerId;
  }

  public Date getLockExpirationBefore() {
    return lockExpirationBefore;
  }

  public Date getLockExpirationAfter() {
    return lockExpirationAfter;
  }

  public String getTopicName() {
    return topicName;
  }

  public Boolean getLocked() {
    return locked;
  }

  public Boolean getNotLocked() {
    return notLocked;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public Boolean getRetriesLeft() {
    return retriesLeft;
  }

  public Date getNow() {
    return ClockUtil.getCurrentTime();
  }

}
