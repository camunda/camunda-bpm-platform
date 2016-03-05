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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricTaskInstance;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricTaskInstanceDto {

  protected String id;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String activityInstanceId;
  protected String name;
  protected String description;
  protected String deleteReason;
  protected String owner;
  protected String assignee;
  protected Date startTime;
  protected Date endTime;
  protected Long duration;
  protected String taskDefinitionKey;
  protected int priority;
  protected Date due;
  protected String parentTaskId;
  protected Date followUp;
  private String tenantId;

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

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public String getOwner() {
    return owner;
  }

  public String getAssignee() {
    return assignee;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Long getDuration() {
    return duration;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public int getPriority() {
    return priority;
  }

  public Date getDue() {
    return due;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public Date getFollowUp() {
    return followUp;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static HistoricTaskInstanceDto fromHistoricTaskInstance(HistoricTaskInstance taskInstance) {

    HistoricTaskInstanceDto dto = new HistoricTaskInstanceDto();

    dto.id = taskInstance.getId();
    dto.processDefinitionKey = taskInstance.getProcessDefinitionKey();
    dto.processDefinitionId = taskInstance.getProcessDefinitionId();
    dto.processInstanceId = taskInstance.getProcessInstanceId();
    dto.executionId = taskInstance.getExecutionId();
    dto.caseDefinitionKey = taskInstance.getCaseDefinitionKey();
    dto.caseDefinitionId = taskInstance.getCaseDefinitionId();
    dto.caseInstanceId = taskInstance.getCaseInstanceId();
    dto.caseExecutionId = taskInstance.getCaseExecutionId();
    dto.activityInstanceId = taskInstance.getActivityInstanceId();
    dto.name = taskInstance.getName();
    dto.description = taskInstance.getDescription();
    dto.deleteReason = taskInstance.getDeleteReason();
    dto.owner = taskInstance.getOwner();
    dto.assignee = taskInstance.getAssignee();
    dto.startTime = taskInstance.getStartTime();
    dto.endTime = taskInstance.getEndTime();
    dto.duration = taskInstance.getDurationInMillis();
    dto.taskDefinitionKey = taskInstance.getTaskDefinitionKey();
    dto.priority = taskInstance.getPriority();
    dto.due = taskInstance.getDueDate();
    dto.parentTaskId = taskInstance.getParentTaskId();
    dto.followUp = taskInstance.getFollowUpDate();
    dto.tenantId = taskInstance.getTenantId();

    return dto;
  }

}
