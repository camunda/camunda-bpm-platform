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

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;

/**
 * @author roman.smirnov
 */
public class VariableInstanceQueryImpl extends AbstractVariableQueryImpl<VariableInstanceQuery, VariableInstance> implements VariableInstanceQuery, Serializable {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  private static final long serialVersionUID = 1L;

  protected String variableId;
  protected String variableName;
  protected String[] variableNames;
  protected String variableNameLike;
  protected String[] executionIds;
  protected String[] processInstanceIds;
  protected String[] caseExecutionIds;
  protected String[] caseInstanceIds;
  protected String[] taskIds;
  protected String[] batchIds;
  protected String[] variableScopeIds;
  protected String[] activityInstanceIds;
  protected String[] tenantIds;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public VariableInstanceQueryImpl() { }

  public VariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public VariableInstanceQuery variableId(String id) {
    ensureNotNull("id", id);
    this.variableId = id;
    return this;
  }

  public VariableInstanceQuery variableName(String variableName) {
    this.variableName = variableName;
    return this;
  }

  public VariableInstanceQuery variableNameIn(String... variableNames) {
    this.variableNames = variableNames;
    return this;
  }

  public VariableInstanceQuery variableNameLike(String variableNameLike) {
    this.variableNameLike = variableNameLike;
    return this;
  }

  public VariableInstanceQuery executionIdIn(String... executionIds) {
    this.executionIds = executionIds;
    return this;
  }

  public VariableInstanceQuery processInstanceIdIn(String... processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public VariableInstanceQuery caseExecutionIdIn(String... caseExecutionIds) {
    this.caseExecutionIds = caseExecutionIds;
    return this;
  }

  public VariableInstanceQuery caseInstanceIdIn(String... caseInstanceIds) {
    this.caseInstanceIds = caseInstanceIds;
    return this;
  }

  public VariableInstanceQuery taskIdIn(String... taskIds) {
    this.taskIds = taskIds;
    return this;
  }

  @Override
  public VariableInstanceQuery batchIdIn(String... batchIds) {
    this.batchIds = batchIds;
    return this;
  }

  public VariableInstanceQuery variableScopeIdIn(String... variableScopeIds) {
    this.variableScopeIds = variableScopeIds;
    return this;
  }

  public VariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public VariableInstanceQuery disableBinaryFetching() {
    this.isByteArrayFetchingEnabled = false;
    return this;
  }

  public VariableInstanceQuery disableCustomObjectDeserialization() {
    this.isCustomObjectDeserializationEnabled = false;
    return this;
  }

  public VariableInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    return this;
  }

  // ordering ////////////////////////////////////////////////////

  public VariableInstanceQuery orderByVariableName() {
    orderBy(VariableInstanceQueryProperty.VARIABLE_NAME);
    return this;
  }

  public VariableInstanceQuery orderByVariableType() {
    orderBy(VariableInstanceQueryProperty.VARIABLE_TYPE);
    return this;
  }

  public VariableInstanceQuery orderByActivityInstanceId() {
    orderBy(VariableInstanceQueryProperty.ACTIVITY_INSTANCE_ID);
    return this;
  }

  public VariableInstanceQuery orderByTenantId() {
    orderBy(VariableInstanceQueryProperty.TENANT_ID);
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions() || CompareUtil.elementIsNotContainedInArray(variableName, variableNames);
  }

  // results ////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getVariableInstanceManager()
      .findVariableInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<VariableInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<VariableInstance> result = commandContext
      .getVariableInstanceManager()
      .findVariableInstanceByQueryCriteria(this, page);

    if (result == null) {
      return result;
    }

    // iterate over the result array to initialize the value and serialized value of the variable
    for (VariableInstance variableInstance : result) {
      VariableInstanceEntity variableInstanceEntity = (VariableInstanceEntity) variableInstance;

      if (shouldFetchValue(variableInstanceEntity)) {
        try {
          variableInstanceEntity.getTypedValue(isCustomObjectDeserializationEnabled);

        } catch(Exception t) {
          // do not fail if one of the variables fails to load
          LOG.exceptionWhileGettingValueForVariable(t);
        }
      }

    }

    return result;
  }

  protected boolean shouldFetchValue(VariableInstanceEntity entity) {
    // do not fetch values for byte arrays eagerly (unless requested by the user)
    return isByteArrayFetchingEnabled
        || !AbstractTypedValueSerializer.BINARY_VALUE_TYPES.contains(entity.getSerializer().getType().getName());
  }

  // getters ////////////////////////////////////////////////////

  public String getVariableId() {
    return variableId;
  }

  public String getVariableName() {
    return variableName;
  }

  public String[] getVariableNames() {
    return variableNames;
  }

  public String getVariableNameLike() {
    return variableNameLike;
  }

  public String[] getExecutionIds() {
    return executionIds;
  }

  public String[] getProcessInstanceIds() {
    return processInstanceIds;
  }

  public String[] getCaseExecutionIds() {
    return caseExecutionIds;
  }

  public String[] getCaseInstanceIds() {
    return caseInstanceIds;
  }

  public String[] getTaskIds() {
    return taskIds;
  }

  public String[] getBatchIds() {
    return batchIds;
  }

  public String[] getVariableScopeIds() {
    return variableScopeIds;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }
}
