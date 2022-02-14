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
package org.camunda.bpm.cockpit.impl.plugin.base.dto;

import java.util.Date;

/**
 * @author roman.smirnov
 */
public class IncidentDto {

  protected String id;
  protected Date incidentTimestamp;
  protected String incidentMessage;
  protected String incidentType;
  protected String executionId;
  protected String activityId;
  protected String failedActivityId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected String annotation;

  // additional properties
  protected String causeIncidentProcessInstanceId;
  protected String causeIncidentProcessDefinitionId;
  protected String causeIncidentActivityId;
  protected String causeIncidentFailedActivityId;
  protected String rootCauseIncidentProcessInstanceId;
  protected String rootCauseIncidentProcessDefinitionId;
  protected String rootCauseIncidentActivityId;
  protected String rootCauseIncidentFailedActivityId;
  protected String rootCauseIncidentConfiguration;
  protected String rootCauseIncidentMessage;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getIncidentTimestamp() {
    return incidentTimestamp;
  }

  public void setIncidentTimestamp(Date incidentTimestamp) {
    this.incidentTimestamp = incidentTimestamp;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getFailedActivityId() {
    return failedActivityId;
  }

  public void setFailedActivityId(String failedActivityId) {
    this.failedActivityId = failedActivityId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
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

  public String getAnnotation() {
    return annotation;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }
  
  public String getCauseIncidentProcessInstanceId() {
    return causeIncidentProcessInstanceId;
  }

  public void setCauseIncidentProcessInstanceId(String causeIncidentProcessInstanceId) {
    this.causeIncidentProcessInstanceId = causeIncidentProcessInstanceId;
  }

  public String getCauseIncidentProcessDefinitionId() {
    return causeIncidentProcessDefinitionId;
  }

  public String getCauseIncidentActivityId() {
    return causeIncidentActivityId;
  }

  public void setCauseIncidentActivityId(String causeIncidentActivityId) {
    this.causeIncidentActivityId = causeIncidentActivityId;
  }

  public String getCauseIncidentFailedActivityId() {
    return causeIncidentFailedActivityId;
  }

  public void setCauseIncidentFailedActivityId(String causeIncidentFailedActivityId) {
    this.causeIncidentFailedActivityId = causeIncidentFailedActivityId;
  }

  public void setCauseIncidentProcessDefinitionId(String causeIncidentProcessDefinitionId) {
    this.causeIncidentProcessDefinitionId = causeIncidentProcessDefinitionId;
  }

  public String getRootCauseIncidentProcessInstanceId() {
    return rootCauseIncidentProcessInstanceId;
  }

  public void setRootCauseIncidentProcessInstanceId(String rootCauseIncidentProcessInstanceId) {
    this.rootCauseIncidentProcessInstanceId = rootCauseIncidentProcessInstanceId;
  }

  public String getRootCauseIncidentProcessDefinitionId() {
    return rootCauseIncidentProcessDefinitionId;
  }

  public void setRootCauseIncidentProcessDefinitionId(String rootCauseIncidentProcessDefinitionId) {
    this.rootCauseIncidentProcessDefinitionId = rootCauseIncidentProcessDefinitionId;
  }

  public String getRootCauseIncidentActivityId() {
    return rootCauseIncidentActivityId;
  }

  public void setRootCauseIncidentActivityId(String rootCauseIncidentActivityId) {
    this.rootCauseIncidentActivityId = rootCauseIncidentActivityId;
  }

  public String getRootCauseIncidentFailedActivityId() {
    return rootCauseIncidentFailedActivityId;
  }

  public void setRootCauseIncidentFailedActivityId(String rootCauseIncidentFailedActivityId) {
    this.rootCauseIncidentFailedActivityId = rootCauseIncidentFailedActivityId;
  }

  public String getRootCauseIncidentConfiguration() {
    return rootCauseIncidentConfiguration;
  }

  public void setRootCauseIncidentConfiguration(String rootCauseIncidentConfiguration) {
    this.rootCauseIncidentConfiguration = rootCauseIncidentConfiguration;
  }

  public String getRootCauseIncidentMessage() {
    return rootCauseIncidentMessage;
  }

  public void setRootCauseIncidentMessage(String rootCauseIncidentMessage) {
    this.rootCauseIncidentMessage = rootCauseIncidentMessage;
  }

}
