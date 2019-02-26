/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Daniel Meyer
 * @author Fabian Bahle
 */
public class ProcessInstanceQueryImpl extends AbstractVariableQueryImpl<ProcessInstanceQuery, ProcessInstance> implements ProcessInstanceQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String businessKey;
  protected String businessKeyLike;
  protected String processDefinitionId;
  protected Set<String> processInstanceIds;
  protected String processDefinitionKey;
  protected String deploymentId;
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected SuspensionState suspensionState;
  protected String incidentType;
  protected String incidentId;
  protected String incidentMessage;
  protected String incidentMessageLike;
  protected String caseInstanceId;
  protected String superCaseInstanceId;
  protected String subCaseInstanceId;
  protected String[] activityIds;
  protected boolean isRootProcessInstances;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;

  // or query /////////////////////////////
  protected List<ProcessInstanceQueryImpl> queries = new ArrayList<ProcessInstanceQueryImpl>(Arrays.asList(this));
  protected boolean isOrQueryActive = false;

  public ProcessInstanceQueryImpl() {
  }

  public ProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    ensureNotNull("Process instance id", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    ensureNotEmpty("Set of process instance ids", processInstanceIds);
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    ensureNotNull("Business key", businessKey);
    this.businessKey = businessKey;
    return this;
  }

  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey, String processDefinitionKey) {
    ensureNotNull("Business key", businessKey);
    this.businessKey = businessKey;
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public ProcessInstanceQuery processInstanceBusinessKeyLike(String businessKeyLike) {
    this.businessKeyLike = businessKeyLike;
    return this;
  }

  public ProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    ensureNotNull("Process definition id", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ProcessInstanceQueryImpl processDefinitionKey(String processDefinitionKey) {
    ensureNotNull("Process definition key", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public ProcessInstanceQuery deploymentId(String deploymentId) {
    ensureNotNull("Deployment id", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    if (isRootProcessInstances) {
      throw new ProcessEngineException("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId");
    }
    this.superProcessInstanceId = superProcessInstanceId;
    return this;
  }

  public ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
    return this;
  }

  public ProcessInstanceQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull("caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public ProcessInstanceQuery superCaseInstanceId(String superCaseInstanceId) {
    ensureNotNull("superCaseInstanceId", superCaseInstanceId);
    this.superCaseInstanceId = superCaseInstanceId;
    return this;
  }

  public ProcessInstanceQuery subCaseInstanceId(String subCaseInstanceId) {
    ensureNotNull("subCaseInstanceId", subCaseInstanceId);
    this.subCaseInstanceId = subCaseInstanceId;
    return this;
  }

  public ProcessInstanceQuery orderByProcessInstanceId() {
    orderBy(ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public ProcessInstanceQuery orderByProcessDefinitionId() {
    orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_PROCESS_DEFINITION,
        ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID));
    return this;
  }

  public ProcessInstanceQuery orderByProcessDefinitionKey() {
    orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_PROCESS_DEFINITION,
        ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY));
    return this;
  }

  public ProcessInstanceQuery orderByTenantId() {
    orderBy(ProcessInstanceQueryProperty.TENANT_ID);
    return this;
  }

  public ProcessInstanceQuery orderByBusinessKey() {
    orderBy(ProcessInstanceQueryProperty.BUSINESS_KEY);
    return this;
  }

  public ProcessInstanceQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public ProcessInstanceQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public ProcessInstanceQuery incidentType(String incidentType) {
    ensureNotNull("incident type", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public ProcessInstanceQuery incidentId(String incidentId) {
    ensureNotNull("incident id", incidentId);
    this.incidentId = incidentId;
    return this;
  }

  public ProcessInstanceQuery incidentMessage(String incidentMessage) {
    ensureNotNull("incident message", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public ProcessInstanceQuery incidentMessageLike(String incidentMessageLike) {
    ensureNotNull("incident messageLike", incidentMessageLike);
    this.incidentMessageLike = incidentMessageLike;
    return this;
  }

  public ProcessInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public ProcessInstanceQuery withoutTenantId() {
    tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  public ProcessInstanceQuery activityIdIn(String... activityIds) {
    ensureNotNull("activity ids", (Object[]) activityIds);
    this.activityIds = activityIds;
    return this;
  }

  public ProcessInstanceQuery rootProcessInstances() {
    if (superProcessInstanceId != null) {
      throw new ProcessEngineException("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId");
    }
    isRootProcessInstances = true;
    return this;
  }

  //results /////////////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    ensureOrExpressionsEvaluated();
    ensureVariablesInitialized();
    checkQueryOk();

    return commandContext
      .getExecutionManager()
      .findProcessInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<ProcessInstance> executeList(CommandContext commandContext, Page page) {
    ensureOrExpressionsEvaluated();
    ensureVariablesInitialized();
    checkQueryOk();

    return commandContext
      .getExecutionManager()
      .findProcessInstancesByQueryCriteria(this, page);
  }

  public List<String> executeIdsList(CommandContext commandContext) {
    ensureOrExpressionsEvaluated();
    ensureVariablesInitialized();
    checkQueryOk();

    return commandContext
      .getExecutionManager()
      .findProcessInstancesIdsByQueryCriteria(this);
  }

  protected void ensureOrExpressionsEvaluated() {
    // skips first query as it has already been evaluated
    for (int i = 1; i < queries.size(); i++) {
      queries.get(i).validate();
      queries.get(i).evaluateExpressions();
    }
  }

  @Override
  protected void ensureVariablesInitialized() {
    super.ensureVariablesInitialized();

    if (!queries.isEmpty()) {
      VariableSerializers variableSerializers = Context.getProcessEngineConfiguration().getVariableSerializers();
      for (ProcessInstanceQueryImpl orQuery: queries) {
        for (QueryVariableValue var : orQuery.queryVariableValues) {
          var.initialize(variableSerializers);
        }
      }
    }
  }

  //getters /////////////////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public List<ProcessInstanceQueryImpl> getQueries() {
    return queries;
  }

  public void addOrQuery(ProcessInstanceQueryImpl orQuery) {
    orQuery.isOrQueryActive = true;
    this.queries.add(orQuery);
  }

  public void setOrQueryActive() {
    isOrQueryActive = true;
  }

  public boolean isOrQueryActive() {
    return isOrQueryActive;
  }

  public String[] getActivityIds() {
    return activityIds;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getBusinessKeyLike() {
    return businessKeyLike;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }

  public String getIncidentId() {
    return incidentId;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public String getIncidentMessageLike() {
    return incidentMessageLike;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public String getSubCaseInstanceId() {
    return subCaseInstanceId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public boolean isRootProcessInstances() {
    return isRootProcessInstances;
  }

  @Override
  public ProcessInstanceQuery or() {
    if (this != queries.get(0)) {
      throw new ProcessEngineException("Invalid query usage: cannot set or() within 'or' query");
    }

    ProcessInstanceQueryImpl orQuery = new ProcessInstanceQueryImpl();
    orQuery.isOrQueryActive = true;
    orQuery.queries = queries;
    queries.add(orQuery);
    return orQuery;
  }

  @Override
  public ProcessInstanceQuery endOr() {
    if (!queries.isEmpty() && this != queries.get(queries.size()-1)) {
      throw new ProcessEngineException("Invalid query usage: cannot set endOr() before or()");
    }

    return queries.get(0);
  }
}
