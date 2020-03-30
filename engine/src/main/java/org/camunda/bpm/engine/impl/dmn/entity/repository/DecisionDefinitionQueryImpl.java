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
package org.camunda.bpm.engine.impl.dmn.entity.repository;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensurePositive;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;

public class DecisionDefinitionQueryImpl extends AbstractQuery<DecisionDefinitionQuery, DecisionDefinition> implements DecisionDefinitionQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String[] ids;
  protected String category;
  protected String categoryLike;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected Date deployedAfter;
  protected Date deployedAt;
  protected String key;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected boolean latest = false;

  protected String decisionRequirementsDefinitionId;
  protected String decisionRequirementsDefinitionKey;
  protected boolean withoutDecisionRequirementsDefinition = false;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected boolean includeDefinitionsWithoutTenantId = false;

  protected String versionTag;
  protected String versionTagLike;

  // for internal use
  private boolean shouldJoinDeploymentTable = false;

  public DecisionDefinitionQueryImpl() {
  }

  public DecisionDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // Query parameter //////////////////////////////////////////////////////////////

  public DecisionDefinitionQuery decisionDefinitionId(String decisionDefinitionId) {
    ensureNotNull(NotValidException.class, "decisionDefinitionId", decisionDefinitionId);
    this.id = decisionDefinitionId;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionIdIn(String... ids) {
    this.ids = ids;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionCategory(String decisionDefinitionCategory) {
    ensureNotNull(NotValidException.class, "category", decisionDefinitionCategory);
    this.category = decisionDefinitionCategory;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionCategoryLike(String decisionDefinitionCategoryLike) {
    ensureNotNull(NotValidException.class, "categoryLike", decisionDefinitionCategoryLike);
    this.categoryLike = decisionDefinitionCategoryLike;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionName(String decisionDefinitionName) {
    ensureNotNull(NotValidException.class, "name", decisionDefinitionName);
    this.name = decisionDefinitionName;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionNameLike(String decisionDefinitionNameLike) {
    ensureNotNull(NotValidException.class, "nameLike", decisionDefinitionNameLike);
    this.nameLike = decisionDefinitionNameLike;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionKey(String decisionDefinitionKey) {
    ensureNotNull(NotValidException.class, "key", decisionDefinitionKey);
    this.key = decisionDefinitionKey;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionKeyLike(String decisionDefinitionKeyLike) {
    ensureNotNull(NotValidException.class, "keyLike", decisionDefinitionKeyLike);
    this.keyLike = decisionDefinitionKeyLike;
    return this;
  }

  public DecisionDefinitionQuery deploymentId(String deploymentId) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public DecisionDefinitionQuery deployedAfter(Date deployedAfter) {
    ensureNotNull(NotValidException.class, "deployedAfter", deployedAfter);
    shouldJoinDeploymentTable = true;
    this.deployedAfter = deployedAfter;
    return this;
  }

  public DecisionDefinitionQuery deployedAt(Date deployedAt) {
    ensureNotNull(NotValidException.class, "deployedAt", deployedAt);
    shouldJoinDeploymentTable = true;
    this.deployedAt = deployedAt;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionVersion(Integer decisionDefinitionVersion) {
    ensureNotNull(NotValidException.class, "version", decisionDefinitionVersion);
    ensurePositive(NotValidException.class, "version", decisionDefinitionVersion.longValue());
    this.version = decisionDefinitionVersion;
    return this;
  }

  public DecisionDefinitionQuery latestVersion() {
    this.latest = true;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionResourceName(String resourceName) {
    ensureNotNull(NotValidException.class, "resourceName", resourceName);
    this.resourceName = resourceName;
    return this;
  }

  public DecisionDefinitionQuery decisionDefinitionResourceNameLike(String resourceNameLike) {
    ensureNotNull(NotValidException.class, "resourceNameLike", resourceNameLike);
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public DecisionDefinitionQuery decisionRequirementsDefinitionId(String decisionRequirementsDefinitionId) {
    ensureNotNull(NotValidException.class, "decisionRequirementsDefinitionId", decisionRequirementsDefinitionId);
    this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
    return this;
  }

  public DecisionDefinitionQuery decisionRequirementsDefinitionKey(String decisionRequirementsDefinitionKey) {
    ensureNotNull(NotValidException.class, "decisionRequirementsDefinitionKey", decisionRequirementsDefinitionKey);
    this.decisionRequirementsDefinitionKey = decisionRequirementsDefinitionKey;
    return this;
  }

  @Override
  public DecisionDefinitionQuery versionTag(String versionTag) {
    ensureNotNull(NotValidException.class, "versionTag", versionTag);
    this.versionTag = versionTag;
    return this;
  }

  @Override
  public DecisionDefinitionQuery versionTagLike(String versionTagLike) {
    ensureNotNull(NotValidException.class, "versionTagLike", versionTagLike);
    this.versionTagLike = versionTagLike;
    return this;
  }

  public DecisionDefinitionQuery withoutDecisionRequirementsDefinition() {
    withoutDecisionRequirementsDefinition = true;
    return this;
  }

  public DecisionDefinitionQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public DecisionDefinitionQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public DecisionDefinitionQuery includeDecisionDefinitionsWithoutTenantId() {
    this.includeDefinitionsWithoutTenantId  = true;
    return this;
  }

  public DecisionDefinitionQuery orderByDecisionDefinitionCategory() {
    orderBy(DecisionDefinitionQueryProperty.DECISION_DEFINITION_CATEGORY);
    return this;
  }

  public DecisionDefinitionQuery orderByDecisionDefinitionKey() {
    orderBy(DecisionDefinitionQueryProperty.DECISION_DEFINITION_KEY);
    return this;
  }

  public DecisionDefinitionQuery orderByDecisionDefinitionId() {
    orderBy(DecisionDefinitionQueryProperty.DECISION_DEFINITION_ID);
    return this;
  }

  public DecisionDefinitionQuery orderByDecisionDefinitionVersion() {
    orderBy(DecisionDefinitionQueryProperty.DECISION_DEFINITION_VERSION);
    return this;
  }

  public DecisionDefinitionQuery orderByDecisionDefinitionName() {
    orderBy(DecisionDefinitionQueryProperty.DECISION_DEFINITION_NAME);
    return this;
  }

  public DecisionDefinitionQuery orderByDeploymentId() {
    orderBy(DecisionDefinitionQueryProperty.DEPLOYMENT_ID);
    return this;
  }

  public DecisionDefinitionQuery orderByDeploymentTime() {
    shouldJoinDeploymentTable = true;
    orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_DEPLOYMENT, DecisionDefinitionQueryProperty.DEPLOY_TIME));
    return this;
  }

  public DecisionDefinitionQuery orderByTenantId() {
    return orderBy(DecisionDefinitionQueryProperty.TENANT_ID);
  }

  public DecisionDefinitionQuery orderByDecisionRequirementsDefinitionKey() {
    return orderBy(DecisionDefinitionQueryProperty.DECISION_REQUIREMENTS_DEFINITION_KEY);
  }

  @Override
  public DecisionDefinitionQuery orderByVersionTag() {
    return orderBy(DecisionDefinitionQueryProperty.VERSION_TAG);
  }

  //results ////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getDecisionDefinitionManager()
      .findDecisionDefinitionCountByQueryCriteria(this);
  }

  @Override
  public List<DecisionDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getDecisionDefinitionManager()
      .findDecisionDefinitionsByQueryCriteria(this, page);
  }

  @Override
  public void checkQueryOk() {
    super.checkQueryOk();

    // latest() makes only sense when used with key() or keyLike()
    if (latest && ( (id != null) || (name != null) || (nameLike != null) || (version != null) || (deploymentId != null) ) ){
      throw new NotValidException("Calling latest() can only be used in combination with key(String) and keyLike(String)");
    }
  }

  // getters ////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String[] getIds() {
    return ids;
  }

  public String getCategory() {
    return category;
  }

  public String getCategoryLike() {
    return categoryLike;
  }

  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public Date getDeployedAfter() {
    return deployedAfter;
  }

  public Date getDeployedAt() {
    return deployedAt;
  }

  public String getKey() {
    return key;
  }

  public String getKeyLike() {
    return keyLike;
  }

  public String getResourceName() {
    return resourceName;
  }

  public String getResourceNameLike() {
    return resourceNameLike;
  }

  public Integer getVersion() {
    return version;
  }

  public String getVersionTag() {
    return versionTag;
  }

  public String getVersionTagLike() {
    return versionTagLike;
  }

  public boolean isLatest() {
    return latest;
  }

  public boolean isShouldJoinDeploymentTable() {
    return shouldJoinDeploymentTable;
  }
}
