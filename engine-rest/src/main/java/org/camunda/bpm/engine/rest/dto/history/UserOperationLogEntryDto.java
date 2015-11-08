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
package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.UserOperationLogEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Danny Gräf
 */
public class UserOperationLogEntryDto {

  protected String id;
  protected String deploymentId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String executionId;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String jobId;
  protected String jobDefinitionId;
  protected String userId;
  protected Date timestamp;
  protected String operationId;
  protected String operationType;
  protected String entityType;
  protected String property;
  protected String orgValue;
  protected String newValue;

  public static UserOperationLogEntryDto map(UserOperationLogEntry entry) {
    UserOperationLogEntryDto dto = new UserOperationLogEntryDto();

    dto.id = entry.getId();
    dto.deploymentId = entry.getDeploymentId();
    dto.processDefinitionId = entry.getProcessDefinitionId();
    dto.processDefinitionKey = entry.getProcessDefinitionKey();
    dto.processInstanceId = entry.getProcessInstanceId();
    dto.executionId = entry.getExecutionId();
    dto.caseDefinitionId = entry.getCaseDefinitionId();
    dto.caseInstanceId = entry.getCaseInstanceId();
    dto.caseExecutionId = entry.getCaseExecutionId();
    dto.taskId = entry.getTaskId();
    dto.jobId = entry.getJobId();
    dto.jobDefinitionId = entry.getJobDefinitionId();
    dto.userId = entry.getUserId();
    dto.timestamp = entry.getTimestamp();
    dto.operationId = entry.getOperationId();
    dto.operationType = entry.getOperationType();
    dto.entityType = entry.getEntityType();
    dto.property = entry.getProperty();
    dto.orgValue = entry.getOrgValue();
    dto.newValue = entry.getNewValue();

    return dto;
  }

  public static List<UserOperationLogEntryDto> map(List<UserOperationLogEntry> entries) {
    List<UserOperationLogEntryDto> result = new ArrayList<UserOperationLogEntryDto>();
    for (UserOperationLogEntry entry : entries) {
      result.add(map(entry));
    }
    return result;
  }

  public String getId() {
    return id;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
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

  public String getJobId() {
    return jobId;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public String getUserId() {
    return userId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getOperationId() {
    return operationId;
  }

  public String getOperationType() {
    return operationType;
  }

  public String getEntityType() {
    return entityType;
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
}