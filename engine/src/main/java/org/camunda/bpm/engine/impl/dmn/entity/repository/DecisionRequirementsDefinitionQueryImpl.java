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
package org.camunda.bpm.engine.impl.dmn.entity.repository;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensurePositive;

import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;

public class DecisionRequirementsDefinitionQueryImpl extends AbstractQuery<DecisionRequirementsDefinitionQuery, DecisionRequirementsDefinition> implements DecisionRequirementsDefinitionQuery {

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

  public DecisionRequirementsDefinitionQueryImpl() {
  }

  public DecisionRequirementsDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // Query parameter //////////////////////////////////////////////////////////////

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionId(String id) {
    ensureNotNull(NotValidException.class, "id", id);
    this.id = id;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionIdIn(String... ids) {
    this.ids = ids;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionCategory(String category) {
    ensureNotNull(NotValidException.class, "category", category);
    this.category = category;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionCategoryLike(String categoryLike) {
    ensureNotNull(NotValidException.class, "categoryLike", categoryLike);
    this.categoryLike = categoryLike;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionName(String name) {
    ensureNotNull(NotValidException.class, "name", name);
    this.name = name;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionNameLike(String nameLike) {
    ensureNotNull(NotValidException.class, "nameLike", nameLike);
    this.nameLike = nameLike;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionKey(String key) {
    ensureNotNull(NotValidException.class, "key", key);
    this.key = key;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionKeyLike(String keyLike) {
    ensureNotNull(NotValidException.class, "keyLike", keyLike);
    this.keyLike = keyLike;
    return this;
  }

  public DecisionRequirementsDefinitionQuery deploymentId(String deploymentId) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionVersion(Integer version) {
    ensureNotNull(NotValidException.class, "version", version);
    ensurePositive(NotValidException.class, "version", version.longValue());
    this.version = version;
    return this;
  }

  public DecisionRequirementsDefinitionQuery latestVersion() {
    this.latest = true;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionResourceName(String resourceName) {
    ensureNotNull(NotValidException.class, "resourceName", resourceName);
    this.resourceName = resourceName;
    return this;
  }

  public DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionResourceNameLike(String resourceNameLike) {
    ensureNotNull(NotValidException.class, "resourceNameLike", resourceNameLike);
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public DecisionRequirementsDefinitionQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public DecisionRequirementsDefinitionQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public DecisionRequirementsDefinitionQuery includeDecisionRequirementsDefinitionsWithoutTenantId() {
    this.includeDefinitionsWithoutTenantId  = true;
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionCategory() {
    orderBy(DecisionRequirementsDefinitionQueryProperty.DECISION_REQUIREMENTS_DEFINITION_CATEGORY);
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionKey() {
    orderBy(DecisionRequirementsDefinitionQueryProperty.DECISION_REQUIREMENTS_DEFINITION_KEY);
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionId() {
    orderBy(DecisionRequirementsDefinitionQueryProperty.DECISION_REQUIREMENTS_DEFINITION_ID);
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionVersion() {
    orderBy(DecisionRequirementsDefinitionQueryProperty.DECISION_REQUIREMENTS_DEFINITION_VERSION);
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionName() {
    orderBy(DecisionRequirementsDefinitionQueryProperty.DECISION_REQUIREMENTS_DEFINITION_NAME);
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByDeploymentId() {
    orderBy(DecisionRequirementsDefinitionQueryProperty.DEPLOYMENT_ID);
    return this;
  }

  public DecisionRequirementsDefinitionQuery orderByTenantId() {
    return orderBy(DecisionRequirementsDefinitionQueryProperty.TENANT_ID);
  }

  //results ////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getDecisionRequirementsDefinitionManager()
      .findDecisionRequirementsDefinitionCountByQueryCriteria(this);
  }

  @Override
  public List<DecisionRequirementsDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getDecisionRequirementsDefinitionManager()
      .findDecisionRequirementsDefinitionsByQueryCriteria(this, page);
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
