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
package org.camunda.bpm.engine.impl.externaltask;

import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class TopicFetchInstruction implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String topicName;
  protected String businessKey;
  protected String processDefinitionId;
  protected String[] processDefinitionIds;
  protected String processDefinitionKey;
  protected String[] processDefinitionKeys;
  protected String processDefinitionVersionTag;
  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected List<String> variablesToFetch;

  protected List<QueryVariableValue> filterVariables;
  protected long lockDuration;
  protected boolean deserializeVariables = false;
  protected boolean localVariables = false;
  protected boolean includeExtensionProperties = false;

  public TopicFetchInstruction(String topicName, long lockDuration) {
    this.topicName = topicName;
    this.lockDuration = lockDuration;
    this.filterVariables = new ArrayList<QueryVariableValue>();
  }

  public List<String> getVariablesToFetch() {
    return variablesToFetch;
  }

  public void setVariablesToFetch(List<String> variablesToFetch) {
    this.variablesToFetch = variablesToFetch;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionIds(String[] processDefinitionIds) {
    this.processDefinitionIds = processDefinitionIds;
  }

  public String[] getProcessDefinitionIds() {
    return processDefinitionIds;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKeys(String[] processDefinitionKeys) {
    this.processDefinitionKeys = processDefinitionKeys;
  }

  public String[] getProcessDefinitionKeys() {
    return processDefinitionKeys;
  }

  public void setProcessDefinitionVersionTag(String processDefinitionVersionTag) {
    this.processDefinitionVersionTag = processDefinitionVersionTag;
  }

  public String getProcessDefinitionVersionTag() {
    return processDefinitionVersionTag;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public void setTenantIdSet(boolean isTenantIdSet) {
    this.isTenantIdSet = isTenantIdSet;
  }

  public String[] getTenantIds() {
    return tenantIds;
  }

  public void setTenantIds(String[] tenantIds) {
    isTenantIdSet = true;
    this.tenantIds = tenantIds;
  }

  public List<QueryVariableValue> getFilterVariables() {
    return filterVariables;
  }

  public void setFilterVariables(Map<String, Object> filterVariables) {
    QueryVariableValue variableValue;
    for (Map.Entry<String, Object> filter : filterVariables.entrySet()) {
      variableValue = new QueryVariableValue(filter.getKey(), filter.getValue(), null, false);
      this.filterVariables.add(variableValue);
    }
  }

  public void addFilterVariable(String name, Object value) {
    QueryVariableValue variableValue = new QueryVariableValue(name, value, QueryOperator.EQUALS, true);
    this.filterVariables.add(variableValue);
  }

  public Long getLockDuration() {
    return lockDuration;
  }

  public String getTopicName() {
    return topicName;
  }

  public boolean isDeserializeVariables() {
    return deserializeVariables;
  }

  public void setDeserializeVariables(boolean deserializeVariables) {
    this.deserializeVariables = deserializeVariables;
  }

  public void ensureVariablesInitialized() {
    if (!filterVariables.isEmpty()) {
      ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
      String dbType = processEngineConfiguration.getDatabaseType();
      for(QueryVariableValue queryVariableValue : filterVariables) {
        queryVariableValue.initialize(variableSerializers, dbType);
      }
    }
  }

  public boolean isLocalVariables() {
    return localVariables;
  }

  public void setLocalVariables(boolean localVariables) {
    this.localVariables = localVariables;
  }

  public boolean isIncludeExtensionProperties() {
    return includeExtensionProperties;
  }

  public void setIncludeExtensionProperties(boolean includeExtensionProperties) {
    this.includeExtensionProperties = includeExtensionProperties;
  }

}
