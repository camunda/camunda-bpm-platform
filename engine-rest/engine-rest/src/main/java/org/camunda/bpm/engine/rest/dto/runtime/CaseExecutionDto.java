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
package org.camunda.bpm.engine.rest.dto.runtime;

import org.camunda.bpm.engine.runtime.CaseExecution;


/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionDto {

  protected String id;
  protected String caseInstanceId;
  protected String caseDefinitionId;
  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String activityDescription;
  protected String parentId;
  protected String tenantId;
  protected boolean required;
  protected boolean enabled;
  protected boolean active;
  protected boolean disabled;

  public String getId() {
    return id;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
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

  public String getActivityDescription() {
    return activityDescription;
  }

  public String getParentId() {
    return parentId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public static CaseExecutionDto fromCaseExecution(CaseExecution caseExecution) {
    CaseExecutionDto dto = new CaseExecutionDto();

    dto.id = caseExecution.getId();
    dto.caseInstanceId = caseExecution.getCaseInstanceId();
    dto.caseDefinitionId = caseExecution.getCaseDefinitionId();
    dto.activityId = caseExecution.getActivityId();
    dto.activityName = caseExecution.getActivityName();
    dto.activityType = caseExecution.getActivityType();
    dto.activityDescription = caseExecution.getActivityDescription();
    dto.parentId = caseExecution.getParentId();
    dto.tenantId = caseExecution.getTenantId();
    dto.required = caseExecution.isRequired();
    dto.active = caseExecution.isActive();
    dto.enabled = caseExecution.isEnabled();
    dto.disabled = caseExecution.isDisabled();

    return dto;
  }

}
