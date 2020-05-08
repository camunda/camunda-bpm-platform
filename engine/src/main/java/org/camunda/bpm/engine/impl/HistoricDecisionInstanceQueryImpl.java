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

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.ImmutablePair;

/**
 *  @author Philipp Ossler
 */
public class HistoricDecisionInstanceQueryImpl extends AbstractQuery<HistoricDecisionInstanceQuery, HistoricDecisionInstance> implements HistoricDecisionInstanceQuery {

  private static final long serialVersionUID = 1L;

  protected String decisionInstanceId;
  protected String[] decisionInstanceIdIn;

  protected String decisionDefinitionId;
  protected String[] decisionDefinitionIdIn;

  protected String decisionDefinitionKey;
  protected String[] decisionDefinitionKeyIn;

  protected String decisionDefinitionName;
  protected String decisionDefinitionNameLike;

  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;

  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;

  protected String[] activityInstanceIds;
  protected String[] activityIds;

  protected Date evaluatedBefore;
  protected Date evaluatedAfter;

  protected String userId;

  protected boolean includeInput = false;
  protected boolean includeOutputs = false;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  protected String rootDecisionInstanceId;
  protected boolean rootDecisionInstancesOnly = false;

  protected String decisionRequirementsDefinitionId;
  protected String decisionRequirementsDefinitionKey;

  protected String[] tenantIds;
  protected boolean isTenantIdSet;

  public HistoricDecisionInstanceQueryImpl() {
  }

  public HistoricDecisionInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDecisionInstanceQuery decisionInstanceId(String decisionInstanceId) {
    ensureNotNull(NotValidException.class, "decisionInstanceId", decisionInstanceId);
    this.decisionInstanceId = decisionInstanceId;
    return this;
  }

  public HistoricDecisionInstanceQuery decisionInstanceIdIn(String... decisionInstanceIdIn) {
    ensureNotNull("decisionInstanceIdIn", (Object[]) decisionInstanceIdIn);
    this.decisionInstanceIdIn = decisionInstanceIdIn;
    return this;
  }

  public HistoricDecisionInstanceQuery decisionDefinitionId(String decisionDefinitionId) {
    ensureNotNull(NotValidException.class, "decisionDefinitionId", decisionDefinitionId);
    this.decisionDefinitionId = decisionDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionIdIn(String... decisionDefinitionIdIn) {
    ensureNotNull(NotValidException.class, "decisionDefinitionIdIn", decisionDefinitionIdIn);
    this.decisionDefinitionIdIn = decisionDefinitionIdIn;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionKey(String decisionDefinitionKey) {
    ensureNotNull(NotValidException.class, "decisionDefinitionKey", decisionDefinitionKey);
    this.decisionDefinitionKey = decisionDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionKeyIn(String... decisionDefinitionKeyIn) {
    ensureNotNull(NotValidException.class, "decisionDefinitionKeyIn", decisionDefinitionKeyIn);
    this.decisionDefinitionKeyIn = decisionDefinitionKeyIn;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionName(String decisionDefinitionName) {
    ensureNotNull(NotValidException.class, "decisionDefinitionName", decisionDefinitionName);
    this.decisionDefinitionName = decisionDefinitionName;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionDefinitionNameLike(String decisionDefinitionNameLike) {
    ensureNotNull(NotValidException.class, "decisionDefinitionNameLike", decisionDefinitionNameLike);
    this.decisionDefinitionNameLike = decisionDefinitionNameLike;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull(NotValidException.class, "processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull(NotValidException.class, "processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery processInstanceId(String processInstanceId) {
    ensureNotNull(NotValidException.class, "processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery caseDefinitionKey(String caseDefinitionKey) {
    ensureNotNull(NotValidException.class, "caseDefinitionKey", caseDefinitionKey);
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull(NotValidException.class, "caseDefinitionId", caseDefinitionId);
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull(NotValidException.class, "caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery activityIdIn(String... activityIds) {
    ensureNotNull("activityIds", (Object[]) activityIds);
    this.activityIds = activityIds;
    return this;
  }

  public HistoricDecisionInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    ensureNotNull("activityInstanceIds", (Object[]) activityInstanceIds);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public HistoricDecisionInstanceQuery evaluatedBefore(Date evaluatedBefore) {
    ensureNotNull(NotValidException.class, "evaluatedBefore", evaluatedBefore);
    this.evaluatedBefore = evaluatedBefore;
    return this;
  }

  public HistoricDecisionInstanceQuery evaluatedAfter(Date evaluatedAfter) {
    ensureNotNull(NotValidException.class, "evaluatedAfter", evaluatedAfter);
    this.evaluatedAfter = evaluatedAfter;
    return this;
  }

  public HistoricDecisionInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    this.isTenantIdSet =  true;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery withoutTenantId() {
    this.tenantIds = null;
    this.isTenantIdSet = true;
    return this;
  }

  public HistoricDecisionInstanceQuery orderByTenantId() {
    return orderBy(HistoricDecisionInstanceQueryProperty.TENANT_ID);
  }

  @Override
  public HistoricDecisionInstanceQuery userId(String userId) {
    ensureNotNull(NotValidException.class, "userId", userId);
    this.userId = userId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery orderByEvaluationTime() {
    orderBy(HistoricDecisionInstanceQueryProperty.EVALUATION_TIME);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDecisionInstanceManager()
      .findHistoricDecisionInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricDecisionInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getHistoricDecisionInstanceManager()
        .findHistoricDecisionInstancesByQueryCriteria(this, page);
  }

  @Override
  public List<ImmutablePair<String, String>> executeDeploymentIdMappingsList(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
        .getHistoricDecisionInstanceManager()
        .findHistoricDecisionInstanceDeploymentIdMappingsByQueryCriteria(this);
  }

  public String getDecisionDefinitionId() {
    return decisionDefinitionId;
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

  public String[] getActivityIds() {
    return activityIds;
  }

  public String[] getTenantIds() {
    return tenantIds;
  }

  public HistoricDecisionInstanceQuery includeInputs() {
    includeInput = true;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery includeOutputs() {
    includeOutputs = true;
    return this;
  }

  public boolean isIncludeInput() {
    return includeInput;
  }

  public boolean isIncludeOutputs() {
    return includeOutputs;
  }

  @Override
  public HistoricDecisionInstanceQuery disableBinaryFetching() {
    isByteArrayFetchingEnabled = false;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery disableCustomObjectDeserialization() {
    isCustomObjectDeserializationEnabled = false;
    return this;
  }

  public boolean isByteArrayFetchingEnabled() {
    return isByteArrayFetchingEnabled;
  }

  public boolean isCustomObjectDeserializationEnabled() {
    return isCustomObjectDeserializationEnabled;
  }

  public String getRootDecisionInstanceId() {
    return rootDecisionInstanceId;
  }

  public HistoricDecisionInstanceQuery rootDecisionInstanceId(String rootDecisionInstanceId) {
    ensureNotNull(NotValidException.class, "rootDecisionInstanceId", rootDecisionInstanceId);
    this.rootDecisionInstanceId = rootDecisionInstanceId;
    return this;
  }

  public boolean isRootDecisionInstancesOnly() {
    return rootDecisionInstancesOnly;
  }

  public HistoricDecisionInstanceQuery rootDecisionInstancesOnly() {
    this.rootDecisionInstancesOnly = true;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionRequirementsDefinitionId(String decisionRequirementsDefinitionId) {
    ensureNotNull(NotValidException.class, "decisionRequirementsDefinitionId", decisionRequirementsDefinitionId);
    this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
    return this;
  }

  @Override
  public HistoricDecisionInstanceQuery decisionRequirementsDefinitionKey(String decisionRequirementsDefinitionKey) {
    ensureNotNull(NotValidException.class, "decisionRequirementsDefinitionKey", decisionRequirementsDefinitionKey);
    this.decisionRequirementsDefinitionKey = decisionRequirementsDefinitionKey;
    return this;
  }

  public String getDecisionRequirementsDefinitionId() {
    return decisionRequirementsDefinitionId;
  }

  public String getDecisionRequirementsDefinitionKey() {
    return decisionRequirementsDefinitionKey;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }
}
