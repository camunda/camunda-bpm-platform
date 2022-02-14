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
package org.camunda.bpm.engine.impl.history.event;

import java.util.Date;

import org.camunda.bpm.engine.history.IncidentState;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentEventEntity extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  protected Date createTime;
  protected Date endTime;
  protected String incidentType;
  protected String activityId;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected String incidentMessage;
  protected int incidentState;
  protected String tenantId;
  protected String jobDefinitionId;
  protected String historyConfiguration;
  protected String failedActivityId;
  protected String annotation;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getCauseIncidentId() {
    return causeIncidentId;
  }

  public void setCauseIncidentId(String causeIncidentId) {
    this.causeIncidentId = causeIncidentId;
  }

  public String getRootCauseIncidentId() {
    return rootCauseIncidentId;
  }

  public void setRootCauseIncidentId(String rootCauseIncidentId) {
    this.rootCauseIncidentId = rootCauseIncidentId;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getHistoryConfiguration() {
    return historyConfiguration;
  }

  public void setHistoryConfiguration(String historyConfiguration) {
    this.historyConfiguration = historyConfiguration;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  public void setIncidentState(int incidentState) {
    this.incidentState = incidentState;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public boolean isOpen() {
    return IncidentState.DEFAULT.getStateCode() == incidentState;
  }

  public boolean isDeleted() {
    return IncidentState.DELETED.getStateCode() == incidentState;
  }

  public boolean isResolved() {
    return IncidentState.RESOLVED.getStateCode() == incidentState;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  public String getFailedActivityId() {
    return failedActivityId;
  }

  public void setFailedActivityId(String failedActivityId) {
    this.failedActivityId = failedActivityId;
  }

  public String getAnnotation() {
    return annotation;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

}
