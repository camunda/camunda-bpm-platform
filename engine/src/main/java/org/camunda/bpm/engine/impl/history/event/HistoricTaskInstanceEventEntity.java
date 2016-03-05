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
package org.camunda.bpm.engine.impl.history.event;

import java.util.Date;

/**
 * @author Marcel Wieczorek
 */
public class HistoricTaskInstanceEventEntity extends HistoricScopeInstanceEvent {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String assignee;
  protected String owner;
  protected String name;
  protected String description;
  protected Date dueDate;
  protected Date followUpDate;
  protected int priority;
  protected String parentTaskId;
  protected String deleteReason;
  protected String taskDefinitionKey;
  protected String activityInstanceId;
  protected String tenantId;

  // getters and setters //////////////////////////////////////////////////////

  public String getDeleteReason() {
    return deleteReason;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public void setDeleteReason(final String deleteReason) {
    this.deleteReason = deleteReason;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[taskId" + taskId
           + ", assignee=" + assignee
           + ", owner=" + owner
           + ", name=" + name
           + ", description=" + description
           + ", dueDate=" + dueDate
           + ", followUpDate=" + followUpDate
           + ", priority=" + priority
           + ", parentTaskId=" + parentTaskId
           + ", deleteReason=" + deleteReason
           + ", taskDefinitionKey=" + taskDefinitionKey
           + ", durationInMillis=" + durationInMillis
           + ", startTime=" + startTime
           + ", endTime=" + endTime
           + ", id=" + id
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + ", activityInstanceId=" + activityInstanceId
           + ", tenantId=" + tenantId
           + "]";
  }
}
