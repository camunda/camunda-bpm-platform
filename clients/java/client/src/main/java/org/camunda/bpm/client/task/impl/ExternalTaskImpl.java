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
package org.camunda.bpm.client.task.impl;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.VariableValue;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskImpl implements ExternalTask {

  protected String activityId;
  protected String activityInstanceId;
  protected String errorMessage;
  protected String errorDetails;
  protected String executionId;
  protected String id;
  protected Date lockExpirationTime;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionVersionTag;
  protected String processInstanceId;
  protected Integer retries;
  protected String workerId;
  protected String topicName;
  protected String tenantId;
  protected long priority;
  protected Map<String, TypedValueField> variables;
  protected String businessKey;
  protected Map<String, String> extensionProperties;

  @JsonIgnore
  @SuppressWarnings("rawtypes")
  protected Map<String, VariableValue> receivedVariableMap;

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public void setErrorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void setProcessDefinitionVersionTag(String processDefinitionVersionTag) {
    this.processDefinitionVersionTag = processDefinitionVersionTag;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setRetries(Integer retries) {
    this.retries = retries;
  }

  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public void setPriority(long priority) {
    this.priority = priority;
  }

  public void setVariables(Map<String, TypedValueField> variables) {
    this.variables = variables;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public Map<String, TypedValueField> getVariables() {
    return variables;
  }

  @JsonIgnore
  @SuppressWarnings("rawtypes")
  public void setReceivedVariableMap(Map<String, VariableValue> receivedVariableMap) {
    this.receivedVariableMap = receivedVariableMap;
  }

  @Override
  public String getActivityId() {
    return activityId;
  }

  @Override
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String getErrorDetails() {
    return errorDetails;
  }

  @Override
  public String getExecutionId() {
    return executionId;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Override
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionVersionTag() {
    return processDefinitionVersionTag;
  }

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
  public Integer getRetries() {
    return retries;
  }

  @Override
  public String getWorkerId() {
    return workerId;
  }

  @Override
  public String getTopicName() {
    return topicName;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }

  @Override
  public long getPriority() {
    return priority;
  }

  @Override
  public String getBusinessKey() {
    return businessKey;
  }

  @JsonIgnore
  @Override
  public Map<String, Object> getAllVariables() {
    Map<String, Object> variables = new HashMap<>();

    receivedVariableMap.forEach((variableName, variableValue) -> {
      Object variable = getVariable(variableName);
      variables.put(variableName, variable);
    });

    return variables;
  }

  @JsonIgnore
  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T> T getVariable(String variableName) {
    T value = null;

    VariableValue variableValue = receivedVariableMap.get(variableName);
    if (variableValue != null) {
      value = (T) variableValue.getValue();
    }

    return value;
  }

  @JsonIgnore
  @Override
  public VariableMap getAllVariablesTyped() {
    return getAllVariablesTyped(true);
  }

  public VariableMap getAllVariablesTyped(boolean deserializeObjectValues) {
    VariableMap variables = Variables.createVariables();

    receivedVariableMap.forEach((variableName, variableValue) -> {
      TypedValue typedValue = getVariableTyped(variableName, deserializeObjectValues);
      variables.putValueTyped(variableName, typedValue);
    });

    return variables;
  }

  @JsonIgnore
  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName) {
    return getVariableTyped(variableName, true);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @JsonIgnore
  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeObjectValues) {
    TypedValue typedValue = null;

    VariableValue variableValue = receivedVariableMap.get(variableName);
    if (variableValue != null) {
      typedValue = variableValue.getTypedValue(deserializeObjectValues);
    }

    return (T) typedValue;
  }


  public Map<String, String> getExtensionProperties() {
    return extensionProperties == null ? Collections.emptyMap() : extensionProperties;
  }

  public void setExtensionProperties(Map<String, String> extensionProperties) {
    this.extensionProperties = extensionProperties;
  }

  @JsonIgnore
  @Override
  public String getExtensionProperty(String propertyKey) {
    if(extensionProperties != null) {
      return extensionProperties.get(propertyKey);
    }
    return null;
  }

  @Override
  public String toString() {
    return "ExternalTaskImpl ["
        + "activityId=" + activityId + ", "
        + "activityInstanceId=" + activityInstanceId + ", "
        + "businessKey=" + businessKey + ", "
        + "errorDetails=" + errorDetails + ", "
        + "errorMessage=" + errorMessage + ", "
        + "executionId=" + executionId + ", "
        + "id=" + id + ", "
        + "lockExpirationTime=" + (lockExpirationTime == null ? null : DateFormat.getDateTimeInstance().format(lockExpirationTime)) + ", "
        + "priority=" + priority + ", "
        + "processDefinitionId=" + processDefinitionId + ", "
        + "processDefinitionKey=" + processDefinitionKey + ", "
        + "processDefinitionVersionTag=" + processDefinitionVersionTag + ", "
        + "processInstanceId=" + processInstanceId + ", "
        + "receivedVariableMap=" + receivedVariableMap + ", "
        + "retries=" + retries + ", "
        + "tenantId=" + tenantId + ", "
        + "topicName=" + topicName + ", "
        + "variables=" + variables + ", "
        + "workerId=" + workerId + "]";
  }

}

