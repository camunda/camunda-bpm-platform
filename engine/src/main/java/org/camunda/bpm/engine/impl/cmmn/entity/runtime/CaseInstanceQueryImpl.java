/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.AbstractVariableQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceQueryImpl extends AbstractVariableQueryImpl<CaseInstanceQuery, CaseInstance> implements CaseInstanceQuery {

  private static final long serialVersionUID = 1L;

  protected String caseExecutionId;
  protected String businessKey;
  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected CaseExecutionState state;

  public CaseInstanceQueryImpl() {
  }

  public CaseInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public CaseInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public CaseInstanceQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull(NotValidException.class, "caseInstanceId", caseInstanceId);
    caseExecutionId = caseInstanceId;
    return this;
  }

  public CaseInstanceQuery caseInstanceBusinessKey(String caseInstanceBusinessKey) {
    ensureNotNull(NotValidException.class, "businessKey", caseInstanceBusinessKey);
    this.businessKey = caseInstanceBusinessKey;
    return this;
  }

  public CaseInstanceQuery caseDefinitionKey(String caseDefinitionKey) {
    ensureNotNull(NotValidException.class, "caseDefinitionKey", caseDefinitionKey);
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  public CaseInstanceQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull(NotValidException.class, "caseDefinitionId", caseDefinitionId);
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public CaseInstanceQuery active() {
    state = CaseExecutionState.ACTIVE;
    return this;
  }

  public CaseInstanceQuery completed() {
    state = CaseExecutionState.COMPLETED;
    return this;
  }

  public CaseInstanceQuery terminated() {
    state = CaseExecutionState.TERMINATED;
    return this;
  }

  //ordering /////////////////////////////////////////////////////////////////

  public CaseInstanceQuery orderByCaseInstanceId() {
    orderBy(CaseInstanceQueryProperty.CASE_INSTANCE_ID);
    return this;
  }

  public CaseInstanceQuery orderByCaseDefinitionKey() {
    orderBy(CaseInstanceQueryProperty.CASE_DEFINITION_KEY);
    return this;
  }

  public CaseInstanceQuery orderByCaseDefinitionId() {
    orderBy(CaseInstanceQueryProperty.CASE_DEFINITION_ID);
    return this;
  }

  //results /////////////////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getCaseExecutionManager()
      .findCaseInstanceCountByQueryCriteria(this);
  }

  public List<CaseInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getCaseExecutionManager()
      .findCaseInstanceByQueryCriteria(this, page);
  }

  //getters /////////////////////////////////////////////////////////////////

  public String getCaseInstanceId() {
    return caseExecutionId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getActivityId() {
    return null;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public CaseExecutionState getState() {
    return state;
  }

  public boolean isCaseInstancesOnly() {
    return true;
  }

}
