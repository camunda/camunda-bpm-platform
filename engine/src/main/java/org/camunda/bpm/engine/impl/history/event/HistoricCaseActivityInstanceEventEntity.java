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

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;

import java.util.Date;


/**
 * <p>{@link HistoryEvent} implementation for events that happen in a case activity.</p>
 *
 * @author Sebastian Menski
 */
public class HistoricCaseActivityInstanceEventEntity extends HistoricScopeInstanceEvent {

  private static final long serialVersionUID = 1L;

  /** the id of the case activity */
  protected String caseActivityId;

  /** the name of the case activity */
  protected String caseActivityName;

  /** the type of the case activity */
  protected String caseActivityType;

  /** the state of this case activity instance */
  protected int caseActivityInstanceState;

  /** the id of the parent case activity instance */
  protected String parentCaseActivityInstanceId;

  /** the id of the called task in case of a human task */
  protected String taskId;

  /** the id of the called process in case of a process task */
  protected String calledProcessInstanceId;

  /** the id of the called case in case of a case task */
  protected String calledCaseInstanceId;

  /** id of the tenant which belongs to the case activity instance  */
  protected String tenantId;

  /** the flag whether this case activity is required */
  protected boolean required = false;

  // getters and setters //////////////////////////////////////////////////////

  public String getCaseExecutionId() {
    return getId();
  }

  public String getCaseActivityId() {
    return caseActivityId;
  }

  public void setCaseActivityId(String caseActivityId) {
    this.caseActivityId = caseActivityId;
  }

  public String getCaseActivityName() {
    return caseActivityName;
  }

  public void setCaseActivityName(String caseActivityName) {
    this.caseActivityName = caseActivityName;
  }

  public String getCaseActivityType() {
    return caseActivityType;
  }

  public void setCaseActivityType(String caseActivityType) {
    this.caseActivityType = caseActivityType;
  }

  public int getCaseActivityInstanceState() {
    return caseActivityInstanceState;
  }

  public void setCaseActivityInstanceState(int caseActivityInstanceState) {
    this.caseActivityInstanceState = caseActivityInstanceState;
  }

  public String getParentCaseActivityInstanceId() {
    return parentCaseActivityInstanceId;
  }

  public void setParentCaseActivityInstanceId(String parentCaseActivityInstanceId) {
    this.parentCaseActivityInstanceId = parentCaseActivityInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getCalledProcessInstanceId() {
    return calledProcessInstanceId;
  }

  public void setCalledProcessInstanceId(String calledProcessInstanceId) {
    this.calledProcessInstanceId = calledProcessInstanceId;
  }

  public String getCalledCaseInstanceId() {
    return calledCaseInstanceId;
  }

  public void setCalledCaseInstanceId(String calledCaseInstanceId) {
    this.calledCaseInstanceId = calledCaseInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Date getCreateTime() {
    return startTime;
  }

  public void setCreateTime(Date createTime) {
    setStartTime(createTime);
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isAvailable() {
    return caseActivityInstanceState == AVAILABLE.getStateCode();
  }

  public boolean isEnabled() {
    return caseActivityInstanceState == ENABLED.getStateCode();
  }

  public boolean isDisabled() {
    return caseActivityInstanceState == DISABLED.getStateCode();
  }

  public boolean isActive() {
    return caseActivityInstanceState == ACTIVE.getStateCode();
  }

  public boolean isSuspended() {
    return caseActivityInstanceState == SUSPENDED.getStateCode();
  }

  public boolean isCompleted() {
    return caseActivityInstanceState == COMPLETED.getStateCode();
  }

  public boolean isTerminated() {
    return caseActivityInstanceState == TERMINATED.getStateCode();
  }

  public String toString() {
    return this.getClass().getSimpleName()
           + "[caseActivityId=" + caseActivityId
           + ", caseActivityName=" + caseActivityName
           + ", caseActivityInstanceId=" + id
           + ", caseActivityInstanceState=" + caseActivityInstanceState
           + ", parentCaseActivityInstanceId=" + parentCaseActivityInstanceId
           + ", taskId=" + taskId
           + ", calledProcessInstanceId=" + calledProcessInstanceId
           + ", calledCaseInstanceId=" + calledCaseInstanceId
           + ", durationInMillis=" + durationInMillis
           + ", createTime=" + startTime
           + ", endTime=" + endTime
           + ", eventType=" + eventType
           + ", caseExecutionId=" + caseExecutionId
           + ", caseDefinitionId=" + caseDefinitionId
           + ", caseInstanceId=" + caseInstanceId
           + ", tenantId=" + tenantId
           + "]";
  }
}
