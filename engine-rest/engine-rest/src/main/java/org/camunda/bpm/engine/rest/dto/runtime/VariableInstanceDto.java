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
package org.camunda.bpm.engine.rest.dto.runtime;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author roman.smirnov
 */
public class VariableInstanceDto extends VariableValueDto {

  protected String id;
  protected String name;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String batchId;
  protected String activityInstanceId;
  protected String errorMessage;
  protected String tenantId;

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

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
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

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
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

  public String getTenantId() {
    return tenantId;
  }

  public static VariableInstanceDto fromVariableInstance(VariableInstance variableInstance) {
    VariableInstanceDto dto = new VariableInstanceDto();

    dto.id = variableInstance.getId();
    dto.name = variableInstance.getName();
    dto.processDefinitionId = variableInstance.getProcessDefinitionId();
    dto.processInstanceId = variableInstance.getProcessInstanceId();
    dto.executionId = variableInstance.getExecutionId();

    dto.caseExecutionId = variableInstance.getCaseExecutionId();
    dto.caseInstanceId = variableInstance.getCaseInstanceId();

    dto.taskId = variableInstance.getTaskId();
    dto.batchId = variableInstance.getBatchId();
    dto.activityInstanceId = variableInstance.getActivityInstanceId();

    dto.tenantId = variableInstance.getTenantId();

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
