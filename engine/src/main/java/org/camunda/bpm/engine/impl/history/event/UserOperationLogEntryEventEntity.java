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

import org.camunda.bpm.engine.history.UserOperationLogEntry;

/**
 * @author Danny Gräf
 */
public class UserOperationLogEntryEventEntity extends HistoryEvent implements UserOperationLogEntry {

  private static final long serialVersionUID = 1L;

  protected String operationId;
  protected String operationType;
  protected String jobId;
  protected String jobDefinitionId;
  protected String taskId;
  protected String userId;
  protected Date timestamp;
  protected String property;
  protected String orgValue;
  protected String newValue;
  protected String entityType;
  protected String deploymentId;
  protected String tenantId;
  protected String batchId;
  protected String category;
  protected String externalTaskId;
  protected String annotation;

  public String getOperationId() {
    return operationId;
  }

  public String getOperationType() {
    return operationType;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getUserId() {
    return userId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getProperty() {
    return property;
  }

  public String getOrgValue() {
    return orgValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  public void setOperationType(String operationType) {
    this.operationType = operationType;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public void setOrgValue(String orgValue) {
    this.orgValue = orgValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }
  
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }
  
  public String getExternalTaskId() {
    return externalTaskId;
  }
  
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  public String getAnnotation() {
    return annotation;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "[taskId" + taskId
        + ", deploymentId" + deploymentId
        + ", processDefinitionKey =" + processDefinitionKey
        + ", jobId = " + jobId
        + ", jobDefinitionId = " + jobDefinitionId
        + ", batchId = " + batchId
        + ", operationId =" + operationId
        + ", operationType =" + operationType
        + ", userId =" + userId
        + ", timestamp =" + timestamp
        + ", property =" + property
        + ", orgValue =" + orgValue
        + ", newValue =" + newValue
        + ", id=" + id
        + ", eventType=" + eventType
        + ", executionId=" + executionId
        + ", processDefinitionId=" + processDefinitionId
        + ", rootProcessInstanceId=" + rootProcessInstanceId
        + ", processInstanceId=" + processInstanceId
        + ", externalTaskId=" + externalTaskId
        + ", tenantId=" + tenantId
        + ", entityType=" + entityType
        + ", category=" + category
        + ", annotation=" + annotation
        + "]";
  }
}
