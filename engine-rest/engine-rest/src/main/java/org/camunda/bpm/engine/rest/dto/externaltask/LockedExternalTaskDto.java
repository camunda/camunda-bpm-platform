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
package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

/**
 * @author Thorben Lindhauer
 * @author Askar Akhmerov
 */
public class LockedExternalTaskDto {

  protected String activityId;
  protected String activityInstanceId;
  protected String errorMessage;
  protected String errorDetails;
  protected String executionId;
  protected String id;
  protected Date lockExpirationTime;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionVersionTag;
  protected String processInstanceId;
  protected Integer retries;
  protected boolean suspended;
  protected String workerId;
  protected String topicName;
  protected String tenantId;
  protected Map<String, VariableValueDto> variables;
  protected long priority;
  protected String businessKey;
  protected Map<String, String> extensionProperties;

  public String getActivityId() {
    return activityId;
  }
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getId() {
    return id;
  }
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getProcessDefinitionVersionTag() {
    return processDefinitionVersionTag;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public Integer getRetries() {
    return retries;
  }
  public boolean isSuspended() {
    return suspended;
  }
  public String getWorkerId() {
    return workerId;
  }
  public String getTopicName() {
    return topicName;
  }
  public String getTenantId() {
    return tenantId;
  }
  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }

  public long getPriority() {
    return priority;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public Map<String, String> getExtensionProperties(){
    return extensionProperties;
  }

  public static LockedExternalTaskDto fromLockedExternalTask(LockedExternalTask task) {
    LockedExternalTaskDto dto = new LockedExternalTaskDto();
    dto.activityId = task.getActivityId();
    dto.activityInstanceId = task.getActivityInstanceId();
    dto.errorMessage = task.getErrorMessage();
    dto.errorDetails = task.getErrorDetails();
    dto.executionId = task.getExecutionId();
    dto.id = task.getId();
    dto.lockExpirationTime = task.getLockExpirationTime();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processDefinitionKey = task.getProcessDefinitionKey();
    dto.processDefinitionVersionTag = task.getProcessDefinitionVersionTag();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.retries = task.getRetries();
    dto.topicName = task.getTopicName();
    dto.workerId = task.getWorkerId();
    dto.tenantId = task.getTenantId();
    dto.variables = VariableValueDto.fromMap(task.getVariables());
    dto.priority = task.getPriority();
    dto.businessKey = task.getBusinessKey();
    dto.extensionProperties = task.getExtensionProperties();

    return dto;
  }

  public static List<LockedExternalTaskDto> fromLockedExternalTasks(List<LockedExternalTask> tasks) {
    List<LockedExternalTaskDto> dtos = new ArrayList<>();

    for (LockedExternalTask task : tasks) {
      dtos.add(LockedExternalTaskDto.fromLockedExternalTask(task));
    }

    return dtos;
  }
  @Override
  public String toString() {
    return 
        "LockedExternalTaskDto [activityId=" + activityId
        + ", activityInstanceId=" + activityInstanceId
        + ", errorMessage=" + errorMessage
        + ", errorDetails=" + errorDetails
        + ", executionId=" + executionId
        + ", id=" + id
        + ", lockExpirationTime=" + lockExpirationTime
        + ", processDefinitionId=" + processDefinitionId
        + ", processDefinitionKey=" + processDefinitionKey
        + ", processDefinitionVersionTag=" + processDefinitionVersionTag
        + ", processInstanceId=" + processInstanceId
        + ", retries=" + retries
        + ", suspended=" + suspended
        + ", workerId=" + workerId
        + ", topicName=" + topicName
        + ", tenantId=" + tenantId
        + ", variables=" + variables
        + ", priority=" + priority
        + ", businessKey=" + businessKey + "]";
  }

}
