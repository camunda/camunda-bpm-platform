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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.UserOperationLogEntry;

/**
 * @author Danny Gräf
 */
public class UserOperationLogEntryDto {

  private String id;
  private String processDefinitionId;
  private String processInstanceId;
  private String executionId;
  private String taskId;
  private String userId;
  private Date timestamp;
  private String operationId;
  private String operationType;
  private String entityType;
  private String property;
  private String orgValue;
  private String newValue;

  public static UserOperationLogEntryDto map(UserOperationLogEntry entry) {
    UserOperationLogEntryDto dto = new UserOperationLogEntryDto();

    dto.id = entry.getId();
    dto.processDefinitionId = entry.getProcessDefinitionId();
    dto.processInstanceId = entry.getProcessInstanceId();
    dto.executionId = entry.getExecutionId();
    dto.taskId = entry.getTaskId();
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

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
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