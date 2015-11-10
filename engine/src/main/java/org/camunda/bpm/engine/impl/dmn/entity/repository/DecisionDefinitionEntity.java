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

import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class DecisionDefinitionEntity extends DmnDecisionTableImpl implements DecisionDefinition, ResourceDefinitionEntity, DbEntity, HasDbRevision, Serializable {

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

  // firstVersion is true, when version == 1 or when
  // this definition does not have any previous definitions
  protected boolean firstVersion = false;
  protected String previousDecisionDefinitionId;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getKey() {
    return key;
  }

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

  public Object getPersistentState() {
    return DecisionDefinitionEntity.class;
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
          .findPreviousDecisionDefinitionIdByKeyAndVersion(key, version);

      if (previousDecisionDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  @Override
  public String toString() {
    return "DecisionDefinitionEntity{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", category='" + category + '\'' +
      ", key='" + key + '\'' +
      ", version=" + version +
      ", deploymentId='" + deploymentId + '\'' +
      '}';
  }

}
