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

import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author roman.smirnov
 */
public class VariableInstanceDto extends VariableValueDto {

  protected String id;
  protected String name;
  protected String processInstanceId;
  protected String executionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String activityInstanceId;
  protected String errorMessage;

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

  public static VariableInstanceDto fromVariableInstance(VariableInstance variableInstance) {
    VariableInstanceDto dto = new VariableInstanceDto();

    dto.id = variableInstance.getId();
    dto.name = variableInstance.getName();
    dto.processInstanceId = variableInstance.getProcessInstanceId();
    dto.executionId = variableInstance.getExecutionId();

    dto.caseExecutionId = variableInstance.getCaseExecutionId();
    dto.caseInstanceId = variableInstance.getCaseInstanceId();

    dto.taskId = variableInstance.getTaskId();
    dto.activityInstanceId = variableInstance.getActivityInstanceId();

    if(variableInstance.getErrorMessage() == null) {
      VariableValueDto.fromTypedValue(dto, variableInstance.getTypedValue());
    }
    else {
      dto.errorMessage = variableInstance.getErrorMessage();
      dto.type = VariableValueDto.toRestApiTypeName(variableInstance.getTypeName());
    }

    return dto;
  }

}
