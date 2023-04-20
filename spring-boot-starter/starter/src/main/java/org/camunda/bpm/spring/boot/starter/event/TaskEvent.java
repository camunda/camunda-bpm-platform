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
package org.camunda.bpm.spring.boot.starter.event;

import java.util.Date;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class TaskEvent {

  protected String assignee;
  protected String caseDefinitionId;
  protected String caseExecutionId;
  protected String caseInstanceId;
  protected Date createTime; // The time when the task has been created
  protected String deleteReason;
  protected String description;
  protected Date dueDate;
  protected String eventName;
  protected String executionId;
  protected Date followUpDate;
  protected String id;
  protected Date lastUpdated;
  protected String name;
  protected String owner;
  protected int priority;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String taskDefinitionKey;
  protected String tenantId;

  public TaskEvent(DelegateTask delegateTask) {
    this.assignee = delegateTask.getAssignee();
    this.caseDefinitionId = delegateTask.getCaseDefinitionId();
    this.caseExecutionId = delegateTask.getCaseExecutionId();
    this.caseInstanceId = delegateTask.getCaseInstanceId();
    this.createTime = delegateTask.getCreateTime();
    this.deleteReason = delegateTask.getDeleteReason();
    this.description = delegateTask.getDescription();
    this.dueDate = delegateTask.getDueDate();
    this.eventName = delegateTask.getEventName();
    this.executionId = delegateTask.getExecutionId();
    this.followUpDate = delegateTask.getFollowUpDate();
    this.id = delegateTask.getId();
    this.lastUpdated = delegateTask.getLastUpdated();
    this.name = delegateTask.getName();
    this.owner = delegateTask.getOwner();
    this.priority = delegateTask.getPriority();
    this.processDefinitionId = delegateTask.getProcessDefinitionId();
    this.processInstanceId = delegateTask.getProcessInstanceId();
    this.taskDefinitionKey = delegateTask.getTaskDefinitionKey();
    this.tenantId = delegateTask.getTenantId();
  }

  /**
   * The {@link User.getId() userId} of the person to which this task is
   * delegated.
   */
  public String getAssignee() {
    return assignee;
  }

  /**
   * Reference to the case definition or null if it is not related to a case.
   */
  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  /**
   * Reference to the case execution or null if it is not related to a case
   * instance.
   */
  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  /**
   * Reference to the case instance or null if it is not related to a case
   * instance.
   */
  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  /** The date/time when this task was created */
  public Date getCreateTime() {
    return createTime;
  }

  /** Get delete reason of the task. */
  public String getDeleteReason() {
    return deleteReason;
  }

  /** Free text description of the task. */
  public String getDescription() {
    return description;
  }

  /** Due date of the task. */
  public Date getDueDate() {
    return dueDate;
  }

  /**
   * Returns the event name which triggered the task listener to fire for this
   * task.
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Reference to the path of execution or null if it is not related to a
   * process instance.
   */
  public String getExecutionId() {
    return executionId;
  }

  /** Follow-up date of the task. */
  public Date getFollowUpDate() {
    return followUpDate;
  }

  /** DB id of the task. */
  public String getId() {
    return id;
  }

  /**
   * The date/time when this task was last updated.
   * All operations that fire {@link TaskListener#EVENTNAME_UPDATE} count as an update to the task.
   * Returns null if the task was never updated before (i.e. it was only created).
   * */
  public Date getLastUpdated() {
    return lastUpdated;
  }

  /** Name or title of the task. */
  public String getName() {
    return name;
  }

  /**
   * The {@link User.getId() userId} of the person responsible for this task.
   */
  public String getOwner() {
    return owner;
  }

  /**
   * indication of how important/urgent this task is with a number between 0 and
   * 100 where higher values mean a higher priority and lower values mean lower
   * priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high
   * [80..100] highest
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Reference to the process definition or null if it is not related to a
   * process.
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  /**
   * Reference to the process instance or null if it is not related to a process
   * instance.
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  /**
   * The id of the activity in the process defining this task or null if this is
   * not related to a process
   */
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  /**
   * Return the id of the tenant this task belongs to. Can be <code>null</code>
   * if the task belongs to no single tenant.
   */
  public String getTenantId() {
    return tenantId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "[id=" + id
        + ", eventName=" + eventName
        + ", name=" + name
        + ", createTime=" + createTime
        + ", lastUpdated=" + lastUpdated
        + ", executionId=" + executionId
        + ", processDefinitionId=" + processDefinitionId
        + ", processInstanceId=" + processInstanceId
        + ", taskDefinitionKey=" + taskDefinitionKey
        + ", assignee=" + assignee
        + ", owner=" + owner
        + ", description=" + description
        + ", dueDate=" + dueDate
        + ", followUpDate=" + followUpDate
        + ", priority=" + priority
        + ", deleteReason=" + deleteReason
        + ", caseDefinitionId=" + caseDefinitionId
        + ", caseExecutionId=" + caseExecutionId
        + ", caseInstanceId=" + caseInstanceId
        + ", tenantId=" + tenantId
        + "]";
  }
}