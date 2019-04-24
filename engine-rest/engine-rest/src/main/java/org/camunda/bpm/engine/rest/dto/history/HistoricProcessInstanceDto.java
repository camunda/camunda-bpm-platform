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
import org.camunda.bpm.engine.history.HistoricProcessInstance;

public class HistoricProcessInstanceDto {

  private String id;
  private String businessKey;
  private String processDefinitionId;
  private String processDefinitionKey;
  private String processDefinitionName;
  private Integer processDefinitionVersion;
  private Date startTime;
  private Date endTime;
  private Date removalTime;
  private Long durationInMillis;
  private String startUserId;
  private String startActivityId;
  private String deleteReason;
  private String rootProcessInstanceId;
  private String superProcessInstanceId;
  private String superCaseInstanceId;
  private String caseInstanceId;
  private String tenantId;
  private String state;

  public String getId() {
    return id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Long getDurationInMillis() {
    return durationInMillis;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public String getStartActivityId() {
    return startActivityId;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public void setRemovalTime(Date removalTime) {
    this.removalTime = removalTime;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  public static HistoricProcessInstanceDto fromHistoricProcessInstance(HistoricProcessInstance historicProcessInstance) {

    HistoricProcessInstanceDto dto = new HistoricProcessInstanceDto();

    dto.id = historicProcessInstance.getId();
    dto.businessKey = historicProcessInstance.getBusinessKey();
    dto.processDefinitionId = historicProcessInstance.getProcessDefinitionId();
    dto.processDefinitionKey = historicProcessInstance.getProcessDefinitionKey();
    dto.processDefinitionName = historicProcessInstance.getProcessDefinitionName();
    dto.processDefinitionVersion = historicProcessInstance.getProcessDefinitionVersion();
    dto.startTime = historicProcessInstance.getStartTime();
    dto.endTime = historicProcessInstance.getEndTime();
    dto.removalTime = historicProcessInstance.getRemovalTime();
    dto.durationInMillis = historicProcessInstance.getDurationInMillis();
    dto.startUserId = historicProcessInstance.getStartUserId();
    dto.startActivityId = historicProcessInstance.getStartActivityId();
    dto.deleteReason = historicProcessInstance.getDeleteReason();
    dto.rootProcessInstanceId = historicProcessInstance.getRootProcessInstanceId();
    dto.superProcessInstanceId = historicProcessInstance.getSuperProcessInstanceId();
    dto.superCaseInstanceId = historicProcessInstance.getSuperCaseInstanceId();
    dto.caseInstanceId = historicProcessInstance.getCaseInstanceId();
    dto.tenantId = historicProcessInstance.getTenantId();
    dto.state = historicProcessInstance.getState();

    return dto;
  }

}
