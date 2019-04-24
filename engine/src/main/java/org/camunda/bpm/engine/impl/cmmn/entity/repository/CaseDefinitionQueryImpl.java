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
package org.camunda.bpm.engine.impl.cmmn.entity.repository;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensurePositive;

import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionQueryImpl extends AbstractQuery<CaseDefinitionQuery, CaseDefinition> implements CaseDefinitionQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String[] ids;
  protected String category;
  protected String categoryLike;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected String key;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected boolean latest = false;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected boolean includeDefinitionsWithoutTenantId = false;

  public CaseDefinitionQueryImpl() {
  }

  public CaseDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // Query parameter //////////////////////////////////////////////////////////////

  public CaseDefinitionQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull(NotValidException.class, "caseDefinitionId", caseDefinitionId);
    this.id = caseDefinitionId;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionIdIn(String... ids) {
    this.ids = ids;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionCategory(String caseDefinitionCategory) {
    ensureNotNull(NotValidException.class, "category", caseDefinitionCategory);
    this.category = caseDefinitionCategory;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionCategoryLike(String caseDefinitionCategoryLike) {
    ensureNotNull(NotValidException.class, "categoryLike", caseDefinitionCategoryLike);
    this.categoryLike = caseDefinitionCategoryLike;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionName(String caseDefinitionName) {
    ensureNotNull(NotValidException.class, "name", caseDefinitionName);
    this.name = caseDefinitionName;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionNameLike(String caseDefinitionNameLike) {
    ensureNotNull(NotValidException.class, "nameLike", caseDefinitionNameLike);
    this.nameLike = caseDefinitionNameLike;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionKey(String caseDefinitionKey) {
    ensureNotNull(NotValidException.class, "key", caseDefinitionKey);
    this.key = caseDefinitionKey;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionKeyLike(String caseDefinitionKeyLike) {
    ensureNotNull(NotValidException.class, "keyLike", caseDefinitionKeyLike);
    this.keyLike = caseDefinitionKeyLike;
    return this;
  }

  public CaseDefinitionQuery deploymentId(String deploymentId) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionVersion(Integer caseDefinitionVersion) {
    ensureNotNull(NotValidException.class, "version", caseDefinitionVersion);
    ensurePositive(NotValidException.class, "version", caseDefinitionVersion.longValue());
    this.version = caseDefinitionVersion;
    return this;
  }

  public CaseDefinitionQuery latestVersion() {
    this.latest = true;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionResourceName(String resourceName) {
    ensureNotNull(NotValidException.class, "resourceName", resourceName);
    this.resourceName = resourceName;
    return this;
  }

  public CaseDefinitionQuery caseDefinitionResourceNameLike(String resourceNameLike) {
    ensureNotNull(NotValidException.class, "resourceNameLike", resourceNameLike);
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public CaseDefinitionQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public CaseDefinitionQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public CaseDefinitionQuery includeCaseDefinitionsWithoutTenantId() {
    this.includeDefinitionsWithoutTenantId  = true;
    return this;
  }

  public CaseDefinitionQuery orderByCaseDefinitionCategory() {
    orderBy(CaseDefinitionQueryProperty.CASE_DEFINITION_CATEGORY);
    return this;
  }

  public CaseDefinitionQuery orderByCaseDefinitionKey() {
    orderBy(CaseDefinitionQueryProperty.CASE_DEFINITION_KEY);
    return this;
  }

  public CaseDefinitionQuery orderByCaseDefinitionId() {
    orderBy(CaseDefinitionQueryProperty.CASE_DEFINITION_ID);
    return this;
  }

  public CaseDefinitionQuery orderByCaseDefinitionVersion() {
    orderBy(CaseDefinitionQueryProperty.CASE_DEFINITION_VERSION);
    return this;
  }

  public CaseDefinitionQuery orderByCaseDefinitionName() {
    orderBy(CaseDefinitionQueryProperty.CASE_DEFINITION_NAME);
    return this;
  }

  public CaseDefinitionQuery orderByDeploymentId() {
    orderBy(CaseDefinitionQueryProperty.DEPLOYMENT_ID);
    return this;
  }

  public CaseDefinitionQuery orderByTenantId() {
    return orderBy(CaseDefinitionQueryProperty.TENANT_ID);
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions() || CompareUtil.elementIsNotContainedInArray(id, ids);
  }

  //results ////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getCaseDefinitionManager()
      .findCaseDefinitionCountByQueryCriteria(this);
  }

  @Override
  public List<CaseDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getCaseDefinitionManager()
      .findCaseDefinitionsByQueryCriteria(this, page);
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

  public boolean isLatest() {
    return latest;
  }

}
