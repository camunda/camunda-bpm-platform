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
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class DecisionDefinitionEntity extends DmnDecisionImpl implements DecisionDefinition, ResourceDefinitionEntity<DecisionDefinitionEntity>, DbEntity, HasDbRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

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
  protected String decisionRequirementsDefinitionId;
  protected String decisionRequirementsDefinitionKey;

  // firstVersion is true, when version == 1 or when
  // this definition does not have any previous definitions
  protected boolean firstVersion = false;
  protected String previousDecisionDefinitionId;

  protected Integer historyTimeToLive;
  protected String versionTag;

  public DecisionDefinitionEntity() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
    this.firstVersion = (this.version == 1);
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getDecisionRequirementsDefinitionId() {
    return decisionRequirementsDefinitionId;
  }

  public void setDecisionRequirementsDefinitionId(String decisionRequirementsDefinitionId) {
    this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
  }

  public String getDecisionRequirementsDefinitionKey() {
    return decisionRequirementsDefinitionKey;
  }

  public void setDecisionRequirementsDefinitionKey(String decisionRequirementsDefinitionKey) {
    this.decisionRequirementsDefinitionKey = decisionRequirementsDefinitionKey;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("historyTimeToLive", this.historyTimeToLive);
    return persistentState;
  }

  /**
   * Updates all modifiable fields from another decision definition entity.
   *
   * @param updatingDecisionDefinition
   */
  @Override
  public void updateModifiableFieldsFromEntity(DecisionDefinitionEntity updatingDecisionDefinition) {
    if (this.key.equals(updatingDecisionDefinition.key) && this.deploymentId.equals(updatingDecisionDefinition.deploymentId)) {
      this.revision = updatingDecisionDefinition.revision;
      this.historyTimeToLive = updatingDecisionDefinition.historyTimeToLive;
    } else {
      LOG.logUpdateUnrelatedDecisionDefinitionEntity(this.key, updatingDecisionDefinition.key, this.deploymentId, updatingDecisionDefinition.deploymentId);
    }
  }

  // previous decision definition //////////////////////////////////////////////

  public DecisionDefinitionEntity getPreviousDefinition() {
    DecisionDefinitionEntity previousDecisionDefinition = null;

    String previousDecisionDefinitionId = getPreviousDecisionDefinitionId();
    if (previousDecisionDefinitionId != null) {

      previousDecisionDefinition = loadDecisionDefinition(previousDecisionDefinitionId);

      if (previousDecisionDefinition == null) {
        resetPreviousDecisionDefinitionId();
        previousDecisionDefinitionId = getPreviousDecisionDefinitionId();

        if (previousDecisionDefinitionId != null) {
          previousDecisionDefinition = loadDecisionDefinition(previousDecisionDefinitionId);
        }
      }
    }

    return previousDecisionDefinition;
  }

  /**
   * Returns the cached version if exists; does not update the entity from the database in that case
   */
  protected DecisionDefinitionEntity loadDecisionDefinition(String decisionDefinitionId) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = configuration.getDeploymentCache();

    DecisionDefinitionEntity decisionDefinition = deploymentCache.findDecisionDefinitionFromCache(decisionDefinitionId);

    if (decisionDefinition == null) {
      CommandContext commandContext = Context.getCommandContext();
      DecisionDefinitionManager decisionDefinitionManager = commandContext.getDecisionDefinitionManager();
      decisionDefinition = decisionDefinitionManager.findDecisionDefinitionById(decisionDefinitionId);

      if (decisionDefinition != null) {
        decisionDefinition = deploymentCache.resolveDecisionDefinition(decisionDefinition);
      }
    }

    return decisionDefinition;

  }

  public String getPreviousDecisionDefinitionId() {
    ensurePreviousDecisionDefinitionIdInitialized();
    return previousDecisionDefinitionId;
  }

  public void setPreviousDecisionDefinitionId(String previousDecisionDefinitionId) {
    this.previousDecisionDefinitionId = previousDecisionDefinitionId;
  }

  protected void resetPreviousDecisionDefinitionId() {
    previousDecisionDefinitionId = null;
    ensurePreviousDecisionDefinitionIdInitialized();
  }

  protected void ensurePreviousDecisionDefinitionIdInitialized() {
    if (previousDecisionDefinitionId == null && !firstVersion) {
      previousDecisionDefinitionId = Context
          .getCommandContext()
          .getDecisionDefinitionManager()
          .findPreviousDecisionDefinitionId(key, version, tenantId);

      if (previousDecisionDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  @Override
  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  @Override
  public String getVersionTag() {
    return versionTag;
  }

  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }

  @Override
  public String toString() {
    return "DecisionDefinitionEntity{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", category='" + category + '\'' +
      ", key='" + key + '\'' +
      ", version=" + version +
      ", versionTag=" + versionTag +
      ", decisionRequirementsDefinitionId='" + decisionRequirementsDefinitionId + '\'' +
      ", decisionRequirementsDefinitionKey='" + decisionRequirementsDefinitionKey + '\'' +
      ", deploymentId='" + deploymentId + '\'' +
      ", tenantId='" + tenantId + '\'' +
      ", historyTimeToLive=" + historyTimeToLive +
      '}';
  }

}
