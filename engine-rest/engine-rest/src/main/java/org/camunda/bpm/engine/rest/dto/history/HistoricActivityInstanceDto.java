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

import org.camunda.bpm.engine.history.HistoricActivityInstance;

public class HistoricActivityInstanceDto {

  private String id;
  private String parentActivityInstanceId;
  private String activityId;
  private String activityName;
  private String activityType;
  private String processDefinitionKey;
  private String processDefinitionId;
  private String processInstanceId;
  private String executionId;
  private String taskId;
  private String calledProcessInstanceId;
  private String calledCaseInstanceId;
  private String assignee;
  private Date startTime;
  private Date endTime;
  private Long durationInMillis;
  private Boolean canceled;
  private Boolean completeScope;
  private String tenantId;
  private Date removalTime;
  private String rootProcessInstanceId;

  public String getId() {
    return id;
  }

  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getActivityName() {
    return activityName;
  }

  public String getActivityType() {
    return activityType;
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

  public String getTaskId() {
    return taskId;
  }

  public String getCalledProcessInstanceId() {
    return calledProcessInstanceId;
  }

  public String getCalledCaseInstanceId() {
    return calledCaseInstanceId;
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

  public Long getDurationInMillis() {
    return durationInMillis;
  }

  public Boolean getCanceled() {
    return canceled;
  }

  public Boolean getCompleteScope() {
    return completeScope;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public static void fromHistoricActivityInstance(HistoricActivityInstanceDto dto,
                                                  HistoricActivityInstance historicActivityInstance) {

    dto.id = historicActivityInstance.getId();
    dto.parentActivityInstanceId = historicActivityInstance.getParentActivityInstanceId();
    dto.activityId = historicActivityInstance.getActivityId();
    dto.activityName = historicActivityInstance.getActivityName();
    dto.activityType = historicActivityInstance.getActivityType();
    dto.processDefinitionKey = historicActivityInstance.getProcessDefinitionKey();
    dto.processDefinitionId = historicActivityInstance.getProcessDefinitionId();
    dto.processInstanceId = historicActivityInstance.getProcessInstanceId();
    dto.executionId = historicActivityInstance.getExecutionId();
    dto.taskId = historicActivityInstance.getTaskId();
    dto.calledProcessInstanceId = historicActivityInstance.getCalledProcessInstanceId();
    dto.calledCaseInstanceId = historicActivityInstance.getCalledCaseInstanceId();
    dto.assignee = historicActivityInstance.getAssignee();
    dto.startTime = historicActivityInstance.getStartTime();
    dto.endTime = historicActivityInstance.getEndTime();
    dto.durationInMillis = historicActivityInstance.getDurationInMillis();
    dto.canceled = historicActivityInstance.isCanceled();
    dto.completeScope = historicActivityInstance.isCompleteScope();
    dto.tenantId = historicActivityInstance.getTenantId();
    dto.removalTime = historicActivityInstance.getRemovalTime();
    dto.rootProcessInstanceId = historicActivityInstance.getRootProcessInstanceId();
  }
}
