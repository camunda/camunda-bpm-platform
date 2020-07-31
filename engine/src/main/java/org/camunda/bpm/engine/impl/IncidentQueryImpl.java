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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;

/**
 * @author roman.smirnov
 */
public class IncidentQueryImpl extends AbstractQuery<IncidentQuery, Incident> implements IncidentQuery, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String incidentType;
  protected String incidentMessage;
  protected String incidentMessageLike;
  protected String executionId;
  protected Date incidentTimestampBefore;
  protected Date incidentTimestampAfter;
  protected String activityId;
  protected String failedActivityId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String[] processDefinitionKeys;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected String[] tenantIds;
  protected String[] jobDefinitionIds;

  public IncidentQueryImpl() {
  }

  public IncidentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public IncidentQuery incidentId(String incidentId) {
    this.id = incidentId;
    return this;
  }

  public IncidentQuery incidentType(String incidentType) {
    this.incidentType = incidentType;
    return this;
  }

  public IncidentQuery incidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
    return this;
  }

  public IncidentQuery incidentMessageLike(String incidentMessageLike) {
    this.incidentMessageLike = incidentMessageLike;
    return this;
  }

  public IncidentQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public IncidentQuery incidentTimestampBefore(Date incidentTimestampBefore) {
    this.incidentTimestampBefore = incidentTimestampBefore;
    return this;
  }

  public IncidentQuery incidentTimestampAfter(Date incidentTimestampAfter) {
    this.incidentTimestampAfter = incidentTimestampAfter;
    return this;
  }

  public IncidentQuery activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public IncidentQuery failedActivityId(String activityId) {
    this.failedActivityId = activityId;
    return this;
  }

  public IncidentQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public IncidentQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public IncidentQuery processDefinitionKeyIn(String... processDefinitionKeys) {
    ensureNotNull("processDefinitionKeys", (Object[]) processDefinitionKeys);
    this.processDefinitionKeys = processDefinitionKeys;
    return this;
  }

  public IncidentQuery causeIncidentId(String causeIncidentId) {
    this.causeIncidentId = causeIncidentId;
    return this;
  }

  public IncidentQuery rootCauseIncidentId(String rootCauseIncidentId) {
    this.rootCauseIncidentId = rootCauseIncidentId;
    return this;
  }

  public IncidentQuery configuration(String configuration) {
    this.configuration = configuration;
    return this;
  }

  public IncidentQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    return this;
  }

  public IncidentQuery jobDefinitionIdIn(String... jobDefinitionIds) {
    ensureNotNull("jobDefinitionIds", (Object[]) jobDefinitionIds);
    this.jobDefinitionIds = jobDefinitionIds;
    return this;
  }

  //ordering ////////////////////////////////////////////////////

  public IncidentQuery orderByIncidentId() {
    orderBy(IncidentQueryProperty.INCIDENT_ID);
    return this;
  }

  public IncidentQuery orderByIncidentTimestamp() {
    orderBy(IncidentQueryProperty.INCIDENT_TIMESTAMP);
    return this;
  }

  public IncidentQuery orderByIncidentType() {
    orderBy(IncidentQueryProperty.INCIDENT_TYPE);
    return this;
  }

  public IncidentQuery orderByExecutionId() {
    orderBy(IncidentQueryProperty.EXECUTION_ID);
    return this;
  }

  public IncidentQuery orderByActivityId() {
    orderBy(IncidentQueryProperty.ACTIVITY_ID);
    return this;
  }

  public IncidentQuery orderByProcessInstanceId() {
    orderBy(IncidentQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public IncidentQuery orderByProcessDefinitionId() {
    orderBy(IncidentQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public IncidentQuery orderByCauseIncidentId() {
    orderBy(IncidentQueryProperty.CAUSE_INCIDENT_ID);
    return this;
  }

  public IncidentQuery orderByRootCauseIncidentId() {
    orderBy(IncidentQueryProperty.ROOT_CAUSE_INCIDENT_ID);
    return this;
  }

  public IncidentQuery orderByConfiguration() {
    orderBy(IncidentQueryProperty.CONFIGURATION);
    return this;
  }

  public IncidentQuery orderByTenantId() {
    return orderBy(IncidentQueryProperty.TENANT_ID);
  }

  @Override
  public IncidentQuery orderByIncidentMessage() {
    return orderBy(IncidentQueryProperty.INCIDENT_MESSAGE);
  }

  //results ////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getIncidentManager()
      .findIncidentCountByQueryCriteria(this);
  }

  @Override
  public List<Incident> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getIncidentManager()
      .findIncidentByQueryCriteria(this, page);
  }

  public String[] getProcessDefinitionKeys() {
    return processDefinitionKeys;
  }

}
