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
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionQueryImpl extends AbstractVariableQueryImpl<CaseExecutionQuery, CaseExecution> implements CaseExecutionQuery {

  private static final long serialVersionUID = 1L;

  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String activityId;
  protected String caseExecutionId;
  protected String caseInstanceId;
  protected String businessKey;
  protected CaseExecutionState state;
  protected Boolean required = false;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;

  // Not used by end-users, but needed for dynamic ibatis query
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected String superCaseInstanceId;
  protected String subCaseInstanceId;
  protected String deploymentId;

  public CaseExecutionQueryImpl() {
  }

  public CaseExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public CaseExecutionQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull(NotValidException.class, "caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public CaseExecutionQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull(NotValidException.class, "caseDefinitionId", caseDefinitionId);
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public CaseExecutionQuery caseDefinitionKey(String caseDefinitionKey) {
    ensureNotNull(NotValidException.class, "caseDefinitionKey", caseDefinitionKey);
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  public CaseExecutionQuery caseInstanceBusinessKey(String caseInstanceBusinessKey) {
    ensureNotNull(NotValidException.class, "caseInstanceBusinessKey", caseInstanceBusinessKey);
    this.businessKey = caseInstanceBusinessKey;
    return this;
  }

  public CaseExecutionQuery caseExecutionId(String caseExecutionId) {
    ensureNotNull(NotValidException.class, "caseExecutionId", caseExecutionId);
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public CaseExecutionQuery activityId(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    this.activityId = activityId;
    return this;
  }

  public CaseExecutionQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public CaseExecutionQuery withoutTenantId() {
    this.tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  public CaseExecutionQuery required() {
    this.required = true;
    return this;
  }

  public CaseExecutionQuery available() {
    state = CaseExecutionState.AVAILABLE;
    return this;
  }

  public CaseExecutionQuery enabled() {
    state = CaseExecutionState.ENABLED;
    return this;
  }

  public CaseExecutionQuery active() {
    state = CaseExecutionState.ACTIVE;
    return this;
  }

  public CaseExecutionQuery disabled() {
    state = CaseExecutionState.DISABLED;
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueEquals(String name, Object value) {
    addVariable(name, value, QueryOperator.EQUALS, false);
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueNotEquals(String name, Object value) {
    addVariable(name, value, QueryOperator.NOT_EQUALS, false);
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueGreaterThan(String name, Object value) {
    addVariable(name, value, QueryOperator.GREATER_THAN, false);
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueGreaterThanOrEqual(String name, Object value) {
    addVariable(name, value, QueryOperator.GREATER_THAN_OR_EQUAL, false);
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueLessThan(String name, Object value) {
    addVariable(name, value, QueryOperator.LESS_THAN, false);
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueLessThanOrEqual(String name, Object value) {
    addVariable(name, value, QueryOperator.LESS_THAN_OR_EQUAL, false);
    return this;
  }

  public CaseExecutionQuery caseInstanceVariableValueLike(String name, String value) {
    addVariable(name, value, QueryOperator.LIKE, false);
    return this;
  }

  // order by ///////////////////////////////////////////

  public CaseExecutionQuery orderByCaseExecutionId() {
    orderBy(CaseExecutionQueryProperty.CASE_EXECUTION_ID);
    return this;
  }

  public CaseExecutionQuery orderByCaseDefinitionKey() {
    orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_CASE_DEFINITION,
        CaseExecutionQueryProperty.CASE_DEFINITION_KEY));
    return this;
  }

  public CaseExecutionQuery orderByCaseDefinitionId() {
    orderBy(CaseExecutionQueryProperty.CASE_DEFINITION_ID);
    return this;
  }

  public CaseExecutionQuery orderByTenantId() {
    orderBy(CaseExecutionQueryProperty.TENANT_ID);
    return this;
  }

  // results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getCaseExecutionManager()
      .findCaseExecutionCountByQueryCriteria(this);
  }

  public List<CaseExecution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<CaseExecution> result = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionsByQueryCriteria(this, page);

    for (CaseExecution caseExecution : result) {
      CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;
      // initializes the name, type and description
      // of the activity on current case execution
      caseExecutionEntity.getActivity();
    }

    return result;
  }

  // getters /////////////////////////////////////////////

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public CaseExecutionState getState() {
    return state;
  }

  public boolean isCaseInstancesOnly() {
    return false;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public String getSubCaseInstanceId() {
    return subCaseInstanceId;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public Boolean isRequired() {
    return required;
  }

}
