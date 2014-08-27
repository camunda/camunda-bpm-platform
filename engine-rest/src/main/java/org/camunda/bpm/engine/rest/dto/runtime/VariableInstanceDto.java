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
package org.camunda.bpm.engine.rest.dto.runtime;

import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author roman.smirnov
 */
public class VariableInstanceDto {

  private String id;
  private String name;
  private String type;
  private Object value;
  private String processInstanceId;
  private String executionId;
  private String caseInstanceId;
  private String caseExecutionId;
  private String taskId;
  private String activityInstanceId;
  private String errorMessage;
  protected SerializedValueDto serializedValue;

  public VariableInstanceDto() { }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public SerializedValueDto getSerializedValue() {
    return serializedValue;
  }

  public void setSerializedValue(SerializedValueDto serializedValue) {
    this.serializedValue = serializedValue;
  }

  public static VariableInstanceDto fromVariableInstance(VariableInstance variableInstance) {
    VariableInstanceDto dto = new VariableInstanceDto();

    dto.setId(variableInstance.getId());
    dto.setName(variableInstance.getName());
    dto.setProcessInstanceId(variableInstance.getProcessInstanceId());
    dto.setExecutionId(variableInstance.getExecutionId());

    dto.caseExecutionId = variableInstance.getCaseExecutionId();
    dto.caseInstanceId = variableInstance.getCaseInstanceId();

    dto.setTaskId(variableInstance.getTaskId());
    dto.setActivityInstanceId(variableInstance.getActivityInstanceId());

    if (variableInstance.storesCustomObjects()) {
      if (!ProcessEngineVariableType.SERIALIZABLE.getName().equals(variableInstance.getTypeName())) {
        dto.serializedValue = SerializedValueDto.fromSerializedVariableValue(variableInstance.getSerializedValue());
      }

    } else {
      dto.setValue(variableInstance.getValue());
      dto.setErrorMessage(variableInstance.getErrorMessage());
    }

    dto.setType(variableInstance.getValueTypeName());

    return dto;
  }

}
