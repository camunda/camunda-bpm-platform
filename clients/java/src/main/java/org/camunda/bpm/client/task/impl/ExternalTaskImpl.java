/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.task.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.camunda.bpm.client.impl.variable.VariableMappers;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.Date;
import java.util.Map;

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
  protected String processInstanceId;
  protected Integer retries;
  protected boolean suspended;
  protected String workerId;
  protected String topicName;
  protected String tenantId;
  protected long priority;
  protected Map<String, TypedValueDto> variables;

  @JsonIgnore
  protected VariableMap localVariableMap;

  @JsonIgnore
  protected VariableMap writtenVariableMap = new VariableMapImpl();

  @JsonIgnore
  protected VariableMappers variableMappers;

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

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setRetries(Integer retries) {
    this.retries = retries;
  }

  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
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

  public void setVariables(Map<String, TypedValueDto> variables) {
    this.variables = variables;
  }

  public Map<String, TypedValueDto> getVariables() {
    return variables;
  }

  @JsonIgnore
  public VariableMap getWrittenVariableMap() {
    return writtenVariableMap;
  }

  @JsonIgnore
  public void setLocalVariableMap(VariableMap localVariableMap) {
    this.localVariableMap = localVariableMap;
  }

  @JsonIgnore
  public void setVariableMappers(VariableMappers variableMappers) {
    this.variableMappers = variableMappers;
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

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
  public Integer getRetries() {
    return retries;
  }

  @Override
  public boolean isSuspended() {
    return suspended;
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

  @JsonIgnore
  @Override
  public Map<String, Object> getAllVariables() {
    return localVariableMap;
  }

  @JsonIgnore
  @Override
  public VariableMap getAllVariablesTyped() {
    return localVariableMap;
  }

  @JsonIgnore
  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName) {
    return localVariableMap.getValueTyped(variableName);
  }

  @JsonIgnore
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getVariable(String variableName) {
    return (T) localVariableMap.get(variableName);
  }

  @JsonIgnore
  @Override
  public void setVariable(String variableName, Object variableValue) {
    TypedValue typedValue = Variables.untypedValue(variableValue);
    setVariableTyped(variableName, typedValue);
  }

  @JsonIgnore
  @Override
  public void setVariableTyped(String variableName, TypedValue variableTypedValue) {
    TypedValue typedValue = convertToTypedValue(variableTypedValue);

    writtenVariableMap.putValueTyped(variableName, typedValue);
    localVariableMap.putValueTyped(variableName, typedValue);
  }

  @JsonIgnore
  @Override
  public void setAllVariables(Map<String, Object> variables) {
    variables.forEach(this::setVariable);
  }

  @JsonIgnore
  @Override
  public void setAllVariablesTyped(Map<String, TypedValue> variables) {
    variables.forEach(this::setVariableTyped);
  }

  protected TypedValue convertToTypedValue(TypedValue typedValue) {
    Object value = null;
    if (typedValue != null) {
      value = typedValue.getValue();
    }

    if (typedValue == null || typedValue instanceof UntypedValueImpl) {
      return variableMappers.convertToTypedValue(value);
    }
    else {
      return typedValue;
    }
  }

}

