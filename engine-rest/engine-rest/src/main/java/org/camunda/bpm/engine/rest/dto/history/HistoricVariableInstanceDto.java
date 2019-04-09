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
package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

public class HistoricVariableInstanceDto extends VariableValueDto {

  private String id;
  private String name;
  private String processDefinitionKey;
  private String processDefinitionId;
  private String processInstanceId;
  private String executionId;
  private String activityInstanceId;
  private String caseDefinitionKey;
  private String caseDefinitionId;
  private String caseInstanceId;
  private String caseExecutionId;
  private String taskId;
  private String errorMessage;
  private String tenantId;
  private String state;
  private Date createTime;
  private Date removalTime;
  private String rootProcessInstanceId;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getState() {
    return state;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public static HistoricVariableInstanceDto fromHistoricVariableInstance(HistoricVariableInstance historicVariableInstance) {

    HistoricVariableInstanceDto dto = new HistoricVariableInstanceDto();

    dto.id = historicVariableInstance.getId();
    dto.name = historicVariableInstance.getName();
    dto.processDefinitionKey = historicVariableInstance.getProcessDefinitionKey();
    dto.processDefinitionId = historicVariableInstance.getProcessDefinitionId();
    dto.processInstanceId = historicVariableInstance.getProcessInstanceId();
    dto.executionId = historicVariableInstance.getExecutionId();
    dto.activityInstanceId = historicVariableInstance.getActivityInstanceId();
    dto.caseDefinitionKey = historicVariableInstance.getCaseDefinitionKey();
    dto.caseDefinitionId = historicVariableInstance.getCaseDefinitionId();
    dto.caseInstanceId = historicVariableInstance.getCaseInstanceId();
    dto.caseExecutionId = historicVariableInstance.getCaseExecutionId();
    dto.taskId = historicVariableInstance.getTaskId();
    dto.tenantId = historicVariableInstance.getTenantId();
    dto.state = historicVariableInstance.getState();
    dto.createTime = historicVariableInstance.getCreateTime();
    dto.removalTime = historicVariableInstance.getRemovalTime();
    dto.rootProcessInstanceId = historicVariableInstance.getRootProcessInstanceId();

    if(historicVariableInstance.getErrorMessage() == null) {
      VariableValueDto.fromTypedValue(dto, historicVariableInstance.getTypedValue());
    }
    else {
      dto.errorMessage = historicVariableInstance.getErrorMessage();
      dto.type = VariableValueDto.toRestApiTypeName(historicVariableInstance.getTypeName());
    }

    return dto;
  }

}
