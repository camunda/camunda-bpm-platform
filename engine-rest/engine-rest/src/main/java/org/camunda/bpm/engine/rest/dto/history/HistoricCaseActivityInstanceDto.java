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

import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;

import java.util.Date;

public class HistoricCaseActivityInstanceDto {

  private String id;
  private String parentCaseActivityInstanceId;
  private String caseActivityId;
  private String caseActivityName;
  private String caseActivityType;
  private String caseDefinitionId;
  private String caseInstanceId;
  private String caseExecutionId;
  private String taskId;
  private String calledProcessInstanceId;
  private String calledCaseInstanceId;
  private String tenantId;
  private Date createTime;
  private Date endTime;
  private Long durationInMillis;
  private Boolean required;
  private Boolean available;
  private Boolean enabled;
  private Boolean disabled;
  private Boolean active;
  private Boolean completed;
  private Boolean terminated;

  public String getId() {
    return id;
  }

  public String getParentCaseActivityInstanceId() {
    return parentCaseActivityInstanceId;
  }

  public String getCaseActivityId() {
    return caseActivityId;
  }

  public String getCaseActivityName() {
    return caseActivityName;
  }

  public String getCaseActivityType() {
    return caseActivityType;
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

  public String getCalledProcessInstanceId() {
    return calledProcessInstanceId;
  }

  public String getCalledCaseInstanceId() {
    return calledCaseInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Long getDurationInMillis() {
    return durationInMillis;
  }

  public Boolean getRequired() {
    return required;
  }

  public Boolean getAvailable() {
    return available;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public Boolean getDisabled() {
    return disabled;
  }

  public Boolean getActive() {
    return active;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public Boolean getTerminated() {
    return terminated;
  }

  public static HistoricCaseActivityInstanceDto fromHistoricCaseActivityInstance(HistoricCaseActivityInstance historicCaseActivityInstance) {

    HistoricCaseActivityInstanceDto dto = new HistoricCaseActivityInstanceDto();

    dto.id = historicCaseActivityInstance.getId();
    dto.parentCaseActivityInstanceId = historicCaseActivityInstance.getParentCaseActivityInstanceId();
    dto.caseActivityId = historicCaseActivityInstance.getCaseActivityId();
    dto.caseActivityName = historicCaseActivityInstance.getCaseActivityName();
    dto.caseActivityType = historicCaseActivityInstance.getCaseActivityType();
    dto.caseDefinitionId = historicCaseActivityInstance.getCaseDefinitionId();
    dto.caseInstanceId = historicCaseActivityInstance.getCaseInstanceId();
    dto.caseExecutionId = historicCaseActivityInstance.getCaseExecutionId();
    dto.taskId = historicCaseActivityInstance.getTaskId();
    dto.calledProcessInstanceId = historicCaseActivityInstance.getCalledProcessInstanceId();
    dto.calledCaseInstanceId = historicCaseActivityInstance.getCalledCaseInstanceId();
    dto.tenantId = historicCaseActivityInstance.getTenantId();
    dto.createTime = historicCaseActivityInstance.getCreateTime();
    dto.endTime = historicCaseActivityInstance.getEndTime();
    dto.durationInMillis = historicCaseActivityInstance.getDurationInMillis();
    dto.required = historicCaseActivityInstance.isRequired();
    dto.available = historicCaseActivityInstance.isAvailable();
    dto.enabled = historicCaseActivityInstance.isEnabled();
    dto.disabled = historicCaseActivityInstance.isDisabled();
    dto.active = historicCaseActivityInstance.isActive();
    dto.completed = historicCaseActivityInstance.isCompleted();
    dto.terminated = historicCaseActivityInstance.isTerminated();

    return dto;
  }
}
