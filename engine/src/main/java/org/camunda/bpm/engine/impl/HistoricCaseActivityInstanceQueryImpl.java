/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CompareUtil;

/**
 * @author Sebastian Menski
 */
public class HistoricCaseActivityInstanceQueryImpl extends AbstractQuery<HistoricCaseActivityInstanceQuery, HistoricCaseActivityInstance> implements HistoricCaseActivityInstanceQuery {

  private static final long serialVersionUID = 1L;

  protected String[] caseActivityInstanceIds;
  protected String[] caseActivityIds;

  protected String caseInstanceId;
  protected String caseDefinitionId;
  protected String caseActivityName;
  protected String caseActivityType;
  protected Date createdBefore;
  protected Date createdAfter;
  protected Date endedBefore;
  protected Date endedAfter;
  protected Boolean ended;
  protected Integer caseActivityInstanceState;
  protected Boolean required;
  protected String[] tenantIds;

  public HistoricCaseActivityInstanceQueryImpl() {
  }

  public HistoricCaseActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricCaseActivityInstanceManager()
      .findHistoricCaseActivityInstanceCountByQueryCriteria(this);
  }

  public List<HistoricCaseActivityInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricCaseActivityInstanceManager()
      .findHistoricCaseActivityInstancesByQueryCriteria(this, page);
  }

  public HistoricCaseActivityInstanceQuery caseActivityInstanceId(String caseActivityInstanceId) {
    ensureNotNull(NotValidException.class, "caseActivityInstanceId", caseActivityInstanceId);
    return caseActivityInstanceIdIn(caseActivityInstanceId);
  }

  public HistoricCaseActivityInstanceQuery caseActivityInstanceIdIn(String... caseActivityInstanceIds) {
    ensureNotNull(NotValidException.class, "caseActivityInstanceIds", (Object[]) caseActivityInstanceIds);
    this.caseActivityInstanceIds = caseActivityInstanceIds;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull(NotValidException.class, "caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull(NotValidException.class, "caseDefinitionId", caseDefinitionId);
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseExecutionId(String caseExecutionId) {
    ensureNotNull(NotValidException.class, "caseExecutionId", caseExecutionId);
    return caseActivityInstanceIdIn(caseExecutionId);
  }

  public HistoricCaseActivityInstanceQuery caseActivityId(String caseActivityId) {
    ensureNotNull(NotValidException.class, "caseActivityId", caseActivityId);
    return caseActivityIdIn(caseActivityId);
  }

  public HistoricCaseActivityInstanceQuery caseActivityIdIn(String... caseActivityIds) {
    ensureNotNull(NotValidException.class, "caseActivityIds", (Object[]) caseActivityIds);
    this.caseActivityIds = caseActivityIds;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseActivityName(String caseActivityName) {
    ensureNotNull(NotValidException.class, "caseActivityName", caseActivityName);
    this.caseActivityName = caseActivityName;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseActivityType(String caseActivityType) {
    ensureNotNull(NotValidException.class, "caseActivityType", caseActivityType);
    this.caseActivityType = caseActivityType;
    return this;
  }

  public HistoricCaseActivityInstanceQuery createdBefore(Date date) {
    ensureNotNull(NotValidException.class, "createdBefore", date);
    this.createdBefore = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery createdAfter(Date date) {
    ensureNotNull(NotValidException.class, "createdAfter", date);
    this.createdAfter = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery endedBefore(Date date) {
    ensureNotNull(NotValidException.class, "finishedBefore", date);
    this.endedBefore = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery endedAfter(Date date) {
    ensureNotNull(NotValidException.class, "finishedAfter", date);
    this.endedAfter = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery required() {
    this.required = true;
    return this;
  }

  public HistoricCaseActivityInstanceQuery ended() {
    this.ended = true;
    return this;
  }

  public HistoricCaseActivityInstanceQuery notEnded() {
    this.ended = false;
    return this;
  }

  public HistoricCaseActivityInstanceQuery available() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = AVAILABLE.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery enabled() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = ENABLED.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery disabled() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = DISABLED.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery active() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = ACTIVE.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery suspended() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = SUSPENDED.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery completed() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = COMPLETED.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery terminated() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = TERMINATED.getStateCode();
    return this;
  }

  public HistoricCaseActivityInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(createdAfter, createdBefore)
      || CompareUtil.areNotInAscendingOrder(endedAfter, endedBefore);
  }

  // ordering

  public HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceId() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.HISTORIC_CASE_ACTIVITY_INSTANCE_ID);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByCaseInstanceId() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.CASE_INSTANCE_ID);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByCaseExecutionId() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.HISTORIC_CASE_ACTIVITY_INSTANCE_ID);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByCaseActivityId() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.CASE_ACTIVITY_ID);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByCaseActivityName() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.CASE_ACTIVITY_NAME);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByCaseActivityType() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.CASE_ACTIVITY_TYPE);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceCreateTime() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.CREATE);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceEndTime() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.END);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceDuration() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByCaseDefinitionId() {
    orderBy(HistoricCaseActivityInstanceQueryProperty.CASE_DEFINITION_ID);
    return this;
  }

  public HistoricCaseActivityInstanceQuery orderByTenantId() {
    return orderBy(HistoricCaseActivityInstanceQueryProperty.TENANT_ID);
  }

  // getter

  public String[] getCaseActivityInstanceIds() {
    return caseActivityInstanceIds;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String[] getCaseActivityIds() {
    return caseActivityIds;
  }

  public String getCaseActivityName() {
    return caseActivityName;
  }

  public String getCaseActivityType() {
    return caseActivityType;
  }

  public Date getCreatedBefore() {
    return createdBefore;
  }

  public Date getCreatedAfter() {
    return createdAfter;
  }

  public Date getEndedBefore() {
    return endedBefore;
  }

  public Date getEndedAfter() {
    return endedAfter;
  }

  public Boolean getEnded() {
    return ended;
  }

  public Integer getCaseActivityInstanceState() {
    return caseActivityInstanceState;
  }

  public Boolean isRequired() {
    return required;
  }

}
