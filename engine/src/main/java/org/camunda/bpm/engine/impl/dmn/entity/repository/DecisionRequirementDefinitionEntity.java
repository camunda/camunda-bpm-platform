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

import java.io.Serializable;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionRequirementDiagramImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.DecisionRequirementDefinition;

public class DecisionRequirementDefinitionEntity extends DmnDecisionRequirementDiagramImpl implements DecisionRequirementDefinition, ResourceDefinitionEntity, DbEntity, HasDbRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision = 1;
  protected String name;
  protected String category;
  protected String key;
  protected int version;
  protected String deploymentId;
  protected String resourceName;
  protected String diagramResourceName;
  protected String tenantId;

  // firstVersion is true, when version == 1 or when
  // this definition does not have any previous definitions
  protected boolean firstVersion = false;
  protected String previousDecisionRequirementDefinitionId;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public String getResourceName() {
    return resourceName;
  }

  @Override
  public String getDeploymentId() {
    return deploymentId;
  }

  @Override
  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }

  @Override
  public void setRevision(int revision) {
    this.revision = revision;
  }

  @Override
  public int getRevision() {
    return revision;
  }

  @Override
  public int getRevisionNext() {
    return revision + 1;
  }

  @Override
  public Object getPersistentState() {
    return DecisionRequirementDefinitionEntity.class;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public void setVersion(int version) {
    this.version = version;
  }

  @Override
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  @Override
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  @Override
  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }

  @Override
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public ResourceDefinitionEntity getPreviousDefinition() {
    DecisionRequirementDefinitionEntity previousDecisionDefinition = null;

    String previousDecisionDefinitionId = getPreviousDecisionRequirementDefinitionId();
    if (previousDecisionDefinitionId != null) {

      previousDecisionDefinition = loadDecisionRequirementDefinition(previousDecisionDefinitionId);

      if (previousDecisionDefinition == null) {
        resetPreviousDecisionRequirementDefinitionId();
        previousDecisionDefinitionId = getPreviousDecisionRequirementDefinitionId();

        if (previousDecisionDefinitionId != null) {
          previousDecisionDefinition = loadDecisionRequirementDefinition(previousDecisionDefinitionId);
        }
      }
    }

    return previousDecisionDefinition;
  }

  /**
   * Returns the cached version if exists; does not update the entity from the database in that case
   */
  protected DecisionRequirementDefinitionEntity loadDecisionRequirementDefinition(String decisionRequirementDefinitionId) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = configuration.getDeploymentCache();

    DecisionRequirementDefinitionEntity decisionRequirementDefinition = deploymentCache.findDecisionRequirementDefinitionFromCache(decisionRequirementDefinitionId);

    if (decisionRequirementDefinition == null) {
      CommandContext commandContext = Context.getCommandContext();
      DecisionDefinitionManager decisionDefinitionManager = commandContext.getDecisionDefinitionManager();
      decisionRequirementDefinition = decisionDefinitionManager.findDecisionRequirementDefinitionById(decisionRequirementDefinitionId);

      if (decisionRequirementDefinition != null) {
        decisionRequirementDefinition = deploymentCache.resolveDecisionRequirementDefinition(decisionRequirementDefinition);
      }
    }

    return decisionRequirementDefinition;
  }

  public String getPreviousDecisionRequirementDefinitionId() {
    ensurePreviousDecisionRequirementDefinitionIdInitialized();
    return previousDecisionRequirementDefinitionId;
  }

  public void setPreviousDecisionDefinitionId(String previousDecisionDefinitionId) {
    this.previousDecisionRequirementDefinitionId = previousDecisionDefinitionId;
  }

  protected void resetPreviousDecisionRequirementDefinitionId() {
    previousDecisionRequirementDefinitionId = null;
    ensurePreviousDecisionRequirementDefinitionIdInitialized();
  }

  protected void ensurePreviousDecisionRequirementDefinitionIdInitialized() {
    if (previousDecisionRequirementDefinitionId == null && !firstVersion) {
      previousDecisionRequirementDefinitionId = Context
          .getCommandContext()
          .getDecisionDefinitionManager()
          .findPreviousDecisionRequirementDefinitionId(key, version, tenantId);

      if (previousDecisionRequirementDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  @Override
  public String toString() {
    return "DecisionRequirementDefinitionEntity [id=" + id + ", revision=" + revision + ", name=" + name + ", category=" + category + ", key=" + key
        + ", version=" + version + ", deploymentId=" + deploymentId + ", tenantId=" + tenantId + "]";
  }

}
