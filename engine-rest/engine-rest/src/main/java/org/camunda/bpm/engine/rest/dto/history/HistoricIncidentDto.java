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

import org.camunda.bpm.engine.history.HistoricIncident;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentDto {

  protected String id;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String rootProcessInstanceId;
  protected Date createTime;
  protected Date endTime;
  protected Date removalTime;
  protected String incidentType;
  protected String activityId;
  protected String failedActivityId;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected String historyConfiguration;
  protected String incidentMessage;
  protected String tenantId;
  protected String jobDefinitionId;
  protected Boolean open;
  protected Boolean deleted;
  protected Boolean resolved;
  protected String annotation;

  public String getId() {
    return id;
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

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getFailedActivityId() {
    return failedActivityId;
  }

  public String getCauseIncidentId() {
    return causeIncidentId;
  }

  public String getRootCauseIncidentId() {
    return rootCauseIncidentId;
  }

  public String getConfiguration() {
    return configuration;
  }

  public String getHistoryConfiguration() {
    return historyConfiguration;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public Boolean isOpen() {
    return open;
  }

  public Boolean isDeleted() {
    return deleted;
  }

  public Boolean isResolved() {
    return resolved;
  }

  public String getAnnotation() {
    return annotation;
  }

  public static HistoricIncidentDto fromHistoricIncident(HistoricIncident historicIncident) {
    HistoricIncidentDto dto = new HistoricIncidentDto();

    dto.id = historicIncident.getId();
    dto.processDefinitionKey = historicIncident.getProcessDefinitionKey();
    dto.processDefinitionId = historicIncident.getProcessDefinitionId();
    dto.processInstanceId = historicIncident.getProcessInstanceId();
    dto.executionId = historicIncident.getExecutionId();
    dto.createTime = historicIncident.getCreateTime();
    dto.endTime = historicIncident.getEndTime();
    dto.incidentType = historicIncident.getIncidentType();
    dto.failedActivityId = historicIncident.getFailedActivityId();
    dto.activityId = historicIncident.getActivityId();
    dto.causeIncidentId = historicIncident.getCauseIncidentId();
    dto.rootCauseIncidentId = historicIncident.getRootCauseIncidentId();
    dto.configuration = historicIncident.getConfiguration();
    dto.historyConfiguration = historicIncident.getHistoryConfiguration();
    dto.incidentMessage = historicIncident.getIncidentMessage();
    dto.open = historicIncident.isOpen();
    dto.deleted = historicIncident.isDeleted();
    dto.resolved = historicIncident.isResolved();
    dto.tenantId = historicIncident.getTenantId();
    dto.jobDefinitionId = historicIncident.getJobDefinitionId();
    dto.removalTime = historicIncident.getRemovalTime();
    dto.rootProcessInstanceId = historicIncident.getRootProcessInstanceId();
    dto.annotation = historicIncident.getAnnotation();

    return dto;
  }

}
