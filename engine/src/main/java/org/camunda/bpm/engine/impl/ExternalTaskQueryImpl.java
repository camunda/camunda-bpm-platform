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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

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
  protected String processDefinitionKey;
  protected String[] processDefinitionKeys;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String processDefinitionNameLike;
  protected String activityId;
  protected String[] activityIdIn;
  protected SuspensionState suspensionState;
  protected Long priorityHigherThanOrEquals;
  protected Long priorityLowerThanOrEquals;
  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;
  protected List<QueryVariableValue> variables = new ArrayList<>();
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
    this.locked = TRUE;
    return this;
  }

  public ExternalTaskQuery notLocked() {
    this.notLocked = TRUE;
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

  @Override
  public ExternalTaskQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public ExternalTaskQuery processDefinitionKeyIn(String... processDefinitionKeys) {
    this.processDefinitionKeys = processDefinitionKeys;
    return this;
  }

  public ExternalTaskQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public ExternalTaskQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  @Override
  public ExternalTaskQuery processDefinitionNameLike(String processDefinitionName) {
    this.processDefinitionNameLike = processDefinitionName;
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

  @Override
  public ExternalTaskQuery processVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LIKE);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueNotLike(String variableName, String variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_LIKE);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueGreaterThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueGreaterThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.GREATER_THAN_OR_EQUAL);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueLessThan(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN);
    return this;
  }

  @Override
  public ExternalTaskQuery processVariableValueLessThanOrEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.LESS_THAN_OR_EQUAL);
    return this;
  }

  @Override
  public ExternalTaskQuery matchVariableNamesIgnoreCase() {
    this.variableNamesIgnoreCase = true;
    for (QueryVariableValue variable : this.variables) {
        variable.setVariableNameIgnoreCase(true);
    }
    return this;
  }

  @Override
  public ExternalTaskQuery matchVariableValuesIgnoreCase() {
    this.variableValuesIgnoreCase = true;
    for (QueryVariableValue variable : this.variables) {
        variable.setVariableValueIgnoreCase(true);
    }
    return this;
  }

  public void addVariable(String name, Object value, QueryOperator operator) {
    ensureNotNull("name", name);

    if(value == null || isBoolean(value)) {
        // Null-values and booleans can only be used in EQUALS and NOT_EQUALS
        switch(operator) {
            case GREATER_THAN:
                throw new ProcessEngineException("Booleans and null cannot be used in 'greater than' condition");
            case LESS_THAN:
                throw new ProcessEngineException("Booleans and null cannot be used in 'less than' condition");
            case GREATER_THAN_OR_EQUAL:
                throw new ProcessEngineException("Booleans and null cannot be used in 'greater than or equal' condition");
            case LESS_THAN_OR_EQUAL:
                throw new ProcessEngineException("Booleans and null cannot be used in 'less than or equal' condition");
            case LIKE:
                throw new ProcessEngineException("Booleans and null cannot be used in 'like' condition");
            case NOT_LIKE:
                throw new ProcessEngineException("Booleans and null cannot be used in 'not like' condition");
            default:
                break;
        }
    }
    boolean shouldMatchVariableValuesIgnoreCase = TRUE.equals(variableValuesIgnoreCase) && value != null && String.class.isAssignableFrom(value.getClass());
    addVariable(new QueryVariableValue(name, value, operator, false, TRUE.equals(variableNamesIgnoreCase), shouldMatchVariableValuesIgnoreCase));
  }

  protected void addVariable(QueryVariableValue queryVariableValue) {
    variables.add(queryVariableValue);
  }

  private boolean isBoolean(Object value) {
    if (value == null) {
        return false;
    }
    return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
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
    this.retriesLeft = TRUE;
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
  public ExternalTaskQuery orderByCreateTime() {
    return orderBy(ExternalTaskQueryProperty.CREATE_TIME);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getExternalTaskManager()
      .findExternalTaskCountByQueryCriteria(this);
  }

  @Override
  public List<ExternalTask> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getExternalTaskManager()
      .findExternalTasksByQueryCriteria(this);
  }

  public List<String> executeIdsList(CommandContext commandContext) {
    ensureVariablesInitialized();
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

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String[] getProcessDefinitionKeys() {
    return processDefinitionKeys;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public String getProcessDefinitionNameLike() {
    return processDefinitionNameLike;
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

  protected void ensureVariablesInitialized() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
    String dbType = processEngineConfiguration.getDatabaseType();
    for(QueryVariableValue var : variables) {
        var.initialize(variableSerializers, dbType);
    }
  }

  public List<QueryVariableValue> getVariables() {
    return variables;
  }

}
