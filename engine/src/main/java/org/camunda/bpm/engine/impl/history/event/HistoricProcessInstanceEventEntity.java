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


/**
 * <p>{@link HistoryEvent} signifying a top-level event in a process instance.</p>
 *
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 *
 */
public class HistoricProcessInstanceEventEntity extends HistoricScopeInstanceEvent {

  private static final long serialVersionUID = 1L;

  /** the business key of the process instance */
  protected String businessKey;

  /** the id of the user that started the process instance */
  protected String startUserId;

  /** the id of the super process instance */
  protected String superProcessInstanceId;

  /** the id of the super case instance */
  protected String superCaseInstanceId;

  /** the reason why this process instance was cancelled (deleted) */
  protected String deleteReason;

  /** id of the activity which ended the process instance */
  protected String endActivityId;

  /** id of the activity which started the process instance */
  protected String startActivityId;

  /** id of the tenant which belongs to the process instance  */
  protected String tenantId;

  protected String state;

  // getters / setters ////////////////////////////////////////

  public String getEndActivityId() {
    return endActivityId;
  }

  public void setEndActivityId(String endActivityId) {
    this.endActivityId = endActivityId;
  }

  public String getStartActivityId() {
    return startActivityId;
  }

  public void setStartActivityId(String startActivityId) {
    this.startActivityId = startActivityId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public void setSuperCaseInstanceId(String superCaseInstanceId) {
    this.superCaseInstanceId = superCaseInstanceId;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[businessKey=" + businessKey
           + ", startUserId=" + startUserId
           + ", superProcessInstanceId=" + superProcessInstanceId
           + ", rootProcessInstanceId=" + rootProcessInstanceId
           + ", superCaseInstanceId=" + superCaseInstanceId
           + ", deleteReason=" + deleteReason
           + ", durationInMillis=" + durationInMillis
           + ", startTime=" + startTime
           + ", endTime=" + endTime
           + ", removalTime=" + removalTime
           + ", endActivityId=" + endActivityId
           + ", startActivityId=" + startActivityId
           + ", id=" + id
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + ", tenantId=" + tenantId
           + "]";
  }

}
