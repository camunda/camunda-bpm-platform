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
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.FAILED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Sebastian Menski
 */
public class HistoricCaseActivityInstanceQueryImpl extends AbstractQuery<HistoricCaseActivityInstanceQuery, HistoricCaseActivityInstance> implements HistoricCaseActivityInstanceQuery {

  protected String caseActivityInstanceId;
  protected String caseInstanceId;
  protected String caseDefinitionId;
  protected String caseExecutionId;
  protected String caseActivityId;
  protected String caseActivityName;
  protected Date createdBefore;
  protected Date createdAfter;
  protected Date endedBefore;
  protected Date endedAfter;
  protected Boolean finished;
  protected Integer caseActivityInstanceState;

  public HistoricCaseActivityInstanceQueryImpl() {
  }

  public HistoricCaseActivityInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
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
    this.caseActivityInstanceId = caseActivityInstanceId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseActivityId(String caseActivityId) {
    this.caseActivityId = caseActivityId;
    return this;
  }

  public HistoricCaseActivityInstanceQuery caseActivityName(String caseActivityName) {
    this.caseActivityName = caseActivityName;
    return this;
  }

  public HistoricCaseActivityInstanceQuery createdBefore(Date date) {
    this.createdBefore = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery createdAfter(Date date) {
    this.createdAfter = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery endedBefore(Date date) {
    this.endedBefore = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery endedAfter(Date date) {
    this.endedAfter = date;
    return this;
  }

  public HistoricCaseActivityInstanceQuery finished() {
    this.finished = true;
    return this;
  }

  public HistoricCaseActivityInstanceQuery unfinished() {
    this.finished = false;
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

  public HistoricCaseActivityInstanceQuery failed() {
    ensureNull(NotValidException.class, "Already querying for case activity instance state '" + caseActivityInstanceState + "'", "caseActivityState", caseActivityInstanceState);
    this.caseActivityInstanceState = FAILED.getStateCode();
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
    orderBy(HistoricCaseActivityInstanceQueryProperty.CASE_EXECUTION_ID);
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

  // getter

  public String getCaseActivityInstanceId() {
    return caseActivityInstanceId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getCaseActivityId() {
    return caseActivityId;
  }

  public String getCaseActivityName() {
    return caseActivityName;
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

  public Boolean getFinished() {
    return finished;
  }

  public Integer getCaseActivityInstanceState() {
    return caseActivityInstanceState;
  }

}
