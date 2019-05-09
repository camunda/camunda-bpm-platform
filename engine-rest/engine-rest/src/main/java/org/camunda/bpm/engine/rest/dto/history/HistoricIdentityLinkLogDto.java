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

import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;

import java.util.Date;

public class HistoricIdentityLinkLogDto {
  protected String id;
  protected Date time;
  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String operationType;
  protected String assignerId;
  protected String tenantId;
  protected Date removalTime;
  protected String rootProcessInstanceId;

  public String getId() {
    return id;
  }

  public Date getTime() {
    return time;
  }

  public String getType() {
    return type;
  }

  public String getUserId() {
    return userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getOperationType() {
    return operationType;
  }

  public String getAssignerId() {
    return assignerId;
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

  public static HistoricIdentityLinkLogDto fromHistoricIdentityLink(HistoricIdentityLinkLog historicIdentityLink) {
    HistoricIdentityLinkLogDto dto = new HistoricIdentityLinkLogDto();
    fromHistoricIdentityLink(dto, historicIdentityLink);
    return dto;
  }

  public static void fromHistoricIdentityLink(HistoricIdentityLinkLogDto dto,
                                              HistoricIdentityLinkLog historicIdentityLink) {
    dto.id = historicIdentityLink.getId();
    dto.assignerId = historicIdentityLink.getAssignerId();
    dto.groupId = historicIdentityLink.getGroupId();
    dto.operationType = historicIdentityLink.getOperationType();
    dto.taskId = historicIdentityLink.getTaskId();
    dto.time = historicIdentityLink.getTime();
    dto.type = historicIdentityLink.getType();
    dto.processDefinitionId = historicIdentityLink.getProcessDefinitionId();
    dto.processDefinitionKey = historicIdentityLink.getProcessDefinitionKey();
    dto.userId = historicIdentityLink.getUserId();
    dto.tenantId = historicIdentityLink.getTenantId();
    dto.removalTime = historicIdentityLink.getRemovalTime();
    dto.rootProcessInstanceId = historicIdentityLink.getRootProcessInstanceId();
  }
}
