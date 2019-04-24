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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsEmptyString;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CompareUtil;

/**
 * @author Sebastian Menski
 */
public class HistoricCaseInstanceQueryImpl extends AbstractVariableQueryImpl<HistoricCaseInstanceQuery, HistoricCaseInstance> implements HistoricCaseInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String caseInstanceId;
  protected Set<String> caseInstanceIds;
  protected String caseDefinitionId;
  protected String caseDefinitionName;
  protected String caseDefinitionNameLike;
  protected String businessKey;
  protected String businessKeyLike;
  protected Integer state;
  protected Boolean notClosed;
  protected String createdBy;
  protected String superCaseInstanceId;
  protected String subCaseInstanceId;
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected List<String> caseKeyNotIn;
  protected Date createdBefore;
  protected Date createdAfter;
  protected Date closedBefore;
  protected Date closedAfter;
  protected String caseDefinitionKey;
  protected String[] caseActivityIds;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;

  public HistoricCaseInstanceQueryImpl() {
  }

  public HistoricCaseInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricCaseInstanceQueryImpl caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public HistoricCaseInstanceQuery caseInstanceIds(Set<String> caseInstanceIds) {
    ensureNotEmpty("Set of case instance ids", caseInstanceIds);
    this.caseInstanceIds = caseInstanceIds;
    return this;
  }

  public HistoricCaseInstanceQueryImpl caseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public HistoricCaseInstanceQuery caseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  public HistoricCaseInstanceQuery caseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
    return this;
  }

  public HistoricCaseInstanceQuery caseDefinitionNameLike(String nameLike) {
    this.caseDefinitionNameLike = nameLike;
    return this;
  }

  public HistoricCaseInstanceQuery caseInstanceBusinessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public HistoricCaseInstanceQuery caseInstanceBusinessKeyLike(String businessKeyLike) {
    this.businessKeyLike = businessKeyLike;
    return this;
  }

  public HistoricCaseInstanceQuery createdBy(String userId) {
    this.createdBy = userId;
    return this;
  }

  public HistoricCaseInstanceQuery caseDefinitionKeyNotIn(List<String> caseDefinitionKeys) {
    ensureNotContainsNull("caseDefinitionKeys", caseDefinitionKeys);
    ensureNotContainsEmptyString("caseDefinitionKeys", caseDefinitionKeys);
    this.caseKeyNotIn = caseDefinitionKeys;
    return this;
  }

  public HistoricCaseInstanceQuery caseActivityIdIn(String... caseActivityIds) {
    ensureNotNull("caseActivityIds", (Object[]) caseActivityIds);
    this.caseActivityIds = caseActivityIds;
    return this;
  }

  public HistoricCaseInstanceQuery createdAfter(Date date) {
    createdAfter = date;
    return this;
  }

  public HistoricCaseInstanceQuery createdBefore(Date date) {
    createdBefore = date;
    return this;
  }

  public HistoricCaseInstanceQuery closedAfter(Date date) {
    if (state!= null && (!state.equals(CaseExecutionState.CLOSED.getStateCode()))) {
      throw new NotValidException("Already querying for case instance state '" + state + "'");
    }

    closedAfter = date;
    state = CaseExecutionState.CLOSED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery closedBefore(Date date) {
    if (state!= null && (!state.equals(CaseExecutionState.CLOSED.getStateCode()))) {
      throw new NotValidException("Already querying for case instance state '" + state + "'");
    }

    closedBefore = date;
    state = CaseExecutionState.CLOSED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery superCaseInstanceId(String superCaseInstanceId) {
	  this.superCaseInstanceId = superCaseInstanceId;
	  return this;
  }

  public HistoricCaseInstanceQuery subCaseInstanceId(String subCaseInstanceId) {
    this.subCaseInstanceId = subCaseInstanceId;
    return this;
  }

  public HistoricCaseInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
    return this;
  }

  public HistoricCaseInstanceQuery subProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
    return this;
  }

  public HistoricCaseInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public HistoricCaseInstanceQuery withoutTenantId() {
    tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  public HistoricCaseInstanceQuery active() {
    ensureNull(NotValidException.class, "Already querying for case instance state '" + state + "'", "state", state);
    this.state = CaseExecutionState.ACTIVE.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery completed() {
    ensureNull(NotValidException.class, "Already querying for case instance state '" + state + "'", "state", state);
    this.state = CaseExecutionState.COMPLETED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery terminated() {
    ensureNull(NotValidException.class, "Already querying for case instance state '" + state + "'", "state", state);
    this.state = CaseExecutionState.TERMINATED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery failed() {
    ensureNull(NotValidException.class, "Already querying for case instance state '" + state + "'", "state", state);
    this.state = CaseExecutionState.FAILED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery suspended() {
    ensureNull(NotValidException.class, "Already querying for case instance state '" + state + "'", "state", state);
    this.state = CaseExecutionState.SUSPENDED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery closed() {
    ensureNull(NotValidException.class, "Already querying for case instance state '" + state + "'", "state", state);
    this.state = CaseExecutionState.CLOSED.getStateCode();
    return this;
  }

  public HistoricCaseInstanceQuery notClosed() {
    this.notClosed = true;
    return this;
  }

  public HistoricCaseInstanceQuery orderByCaseInstanceBusinessKey() {
    return orderBy(HistoricCaseInstanceQueryProperty.BUSINESS_KEY);
  }

  public HistoricCaseInstanceQuery orderByCaseInstanceDuration() {
    return orderBy(HistoricCaseInstanceQueryProperty.DURATION);
  }

  public HistoricCaseInstanceQuery orderByCaseInstanceCreateTime() {
    return orderBy(HistoricCaseInstanceQueryProperty.CREATE_TIME);
  }

  public HistoricCaseInstanceQuery orderByCaseInstanceCloseTime() {
    return orderBy(HistoricCaseInstanceQueryProperty.CLOSE_TIME);
  }

  public HistoricCaseInstanceQuery orderByCaseDefinitionId() {
    return orderBy(HistoricCaseInstanceQueryProperty.PROCESS_DEFINITION_ID);
  }

  public HistoricCaseInstanceQuery orderByCaseInstanceId() {
    return orderBy(HistoricCaseInstanceQueryProperty.PROCESS_INSTANCE_ID_);
  }

  public HistoricCaseInstanceQuery orderByTenantId() {
    return orderBy(HistoricCaseInstanceQueryProperty.TENANT_ID);
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricCaseInstanceManager()
      .findHistoricCaseInstanceCountByQueryCriteria(this);
  }

  public List<HistoricCaseInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricCaseInstanceManager()
      .findHistoricCaseInstancesByQueryCriteria(this, page);
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(createdAfter, createdBefore)
      || CompareUtil.areNotInAscendingOrder(closedAfter, closedBefore)
      || CompareUtil.elementIsNotContainedInList(caseInstanceId, caseInstanceIds)
      || CompareUtil.elementIsContainedInList(caseDefinitionKey, caseKeyNotIn);
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getBusinessKeyLike() {
    return businessKeyLike;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionIdLike() {
    return caseDefinitionKey + ":%:%";
  }

  public String getCaseDefinitionName() {
    return caseDefinitionName;
  }

  public String getCaseDefinitionNameLike() {
    return caseDefinitionNameLike;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public Set<String> getCaseInstanceIds() {
    return caseInstanceIds;
  }

  public String getStartedBy() {
    return createdBy;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public void setSuperCaseInstanceId(String superCaseInstanceId) {
    this.superCaseInstanceId = superCaseInstanceId;
  }

  public List<String> getCaseKeyNotIn() {
    return caseKeyNotIn;
  }

  public Date getCreatedAfter() {
    return createdAfter;
  }

  public Date getCreatedBefore() {
    return createdBefore;
  }

  public Date getClosedAfter() {
    return closedAfter;
  }

  public Date getClosedBefore() {
    return closedBefore;
  }

  public String getSubCaseInstanceId() {
    return subCaseInstanceId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }

}
