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

import java.io.Serializable;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionRequirementsGraphImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;

public class DecisionRequirementsDefinitionEntity extends DmnDecisionRequirementsGraphImpl implements DecisionRequirementsDefinition, ResourceDefinitionEntity<DecisionRequirementsDefinitionEntity>, DbEntity, HasDbRevision, Serializable {

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
  protected String previousDecisionRequirementsDefinitionId;

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
  public Integer getHistoryTimeToLive() {
    return null;
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
    return DecisionRequirementsDefinitionEntity.class;
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
    DecisionRequirementsDefinitionEntity previousDecisionDefinition = null;

    String previousDecisionDefinitionId = getPreviousDecisionRequirementsDefinitionId();
    if (previousDecisionDefinitionId != null) {

      previousDecisionDefinition = loadDecisionRequirementsDefinition(previousDecisionDefinitionId);

      if (previousDecisionDefinition == null) {
        resetPreviousDecisionRequirementsDefinitionId();
        previousDecisionDefinitionId = getPreviousDecisionRequirementsDefinitionId();

        if (previousDecisionDefinitionId != null) {
          previousDecisionDefinition = loadDecisionRequirementsDefinition(previousDecisionDefinitionId);
        }
      }
    }

    return previousDecisionDefinition;
  }

  @Override
  public void updateModifiableFieldsFromEntity(DecisionRequirementsDefinitionEntity updatingDefinition) {
  }

  /**
   * Returns the cached version if exists; does not update the entity from the database in that case
   */
  protected DecisionRequirementsDefinitionEntity loadDecisionRequirementsDefinition(String decisionRequirementsDefinitionId) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = configuration.getDeploymentCache();

    DecisionRequirementsDefinitionEntity decisionRequirementsDefinition = deploymentCache.findDecisionRequirementsDefinitionFromCache(decisionRequirementsDefinitionId);

    if (decisionRequirementsDefinition == null) {
      CommandContext commandContext = Context.getCommandContext();
      DecisionRequirementsDefinitionManager manager = commandContext.getDecisionRequirementsDefinitionManager();
      decisionRequirementsDefinition = manager.findDecisionRequirementsDefinitionById(decisionRequirementsDefinitionId);

      if (decisionRequirementsDefinition != null) {
        decisionRequirementsDefinition = deploymentCache.resolveDecisionRequirementsDefinition(decisionRequirementsDefinition);
      }
    }

    return decisionRequirementsDefinition;
  }

  public String getPreviousDecisionRequirementsDefinitionId() {
    ensurePreviousDecisionRequirementsDefinitionIdInitialized();
    return previousDecisionRequirementsDefinitionId;
  }

  public void setPreviousDecisionDefinitionId(String previousDecisionDefinitionId) {
    this.previousDecisionRequirementsDefinitionId = previousDecisionDefinitionId;
  }

  protected void resetPreviousDecisionRequirementsDefinitionId() {
    previousDecisionRequirementsDefinitionId = null;
    ensurePreviousDecisionRequirementsDefinitionIdInitialized();
  }

  protected void ensurePreviousDecisionRequirementsDefinitionIdInitialized() {
    if (previousDecisionRequirementsDefinitionId == null && !firstVersion) {
      previousDecisionRequirementsDefinitionId = Context
          .getCommandContext()
          .getDecisionRequirementsDefinitionManager()
          .findPreviousDecisionRequirementsDefinitionId(key, version, tenantId);

      if (previousDecisionRequirementsDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "DecisionRequirementsDefinitionEntity [id=" + id + ", revision=" + revision + ", name=" + name + ", category=" + category + ", key=" + key
        + ", version=" + version + ", deploymentId=" + deploymentId + ", tenantId=" + tenantId + "]";
  }

}
