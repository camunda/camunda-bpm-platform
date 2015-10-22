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
package org.camunda.bpm.engine.impl.cmmn.entity.repository;

import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.CaseDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionEntity extends CmmnCaseDefinition implements CaseDefinition, ResourceDefinitionEntity, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;

  protected int revision = 1;
  protected String category;
  protected String key;
  protected int version;
  protected String deploymentId;
  protected String resourceName;
  protected String diagramResourceName;
  protected Map<String, TaskDefinition> taskDefinitions;

  // firstVersion is true, when version == 1 or when
  // this definition does not have any previous definitions
  protected boolean firstVersion = false;
  protected String previousCaseDefinitionId;

  public CaseDefinitionEntity() {
    super(null);
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

  public Map<String, TaskDefinition> getTaskDefinitions() {
    return taskDefinitions;
  }

  public void setTaskDefinitions(Map<String, TaskDefinition> taskDefinitions) {
    this.taskDefinitions = taskDefinitions;
  }

  // previous case definition //////////////////////////////////////////////

  public CaseDefinitionEntity getPreviousDefinition() {
    CaseDefinitionEntity previousCaseDefinition = null;

    String previousCaseDefinitionId = getPreviousCaseDefinitionId();
    if (previousCaseDefinitionId != null) {

      previousCaseDefinition = loadPreviousCaseDefinition(previousCaseDefinitionId);

      if (previousCaseDefinition == null) {
        setPreviousCaseDefinitionId(null);
        previousCaseDefinitionId = getPreviousCaseDefinitionId();

        if (previousCaseDefinitionId != null) {
          previousCaseDefinition = loadPreviousCaseDefinition(previousCaseDefinitionId);
        }
      }
    }

    return previousCaseDefinition;
  }

  protected CaseDefinitionEntity loadPreviousCaseDefinition(String previousCaseDefinitionId) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = configuration.getDeploymentCache();

    CaseDefinitionEntity previousCaseDefinition = deploymentCache.findCaseDefinitionFromCache(previousCaseDefinitionId);

    if (previousCaseDefinition == null) {
      CommandContext commandContext = Context.getCommandContext();
      CaseDefinitionManager caseDefinitionManager = commandContext.getCaseDefinitionManager();
      previousCaseDefinition = caseDefinitionManager.findCaseDefinitionById(previousCaseDefinitionId);

      if (previousCaseDefinition != null) {
        previousCaseDefinition = deploymentCache.resolveCaseDefinition(previousCaseDefinition);
      }
    }

    return previousCaseDefinition;

  }

  protected String getPreviousCaseDefinitionId() {
    ensurePreviousCaseDefinitionIdInitialized();
    return previousCaseDefinitionId;
  }

  protected void setPreviousCaseDefinitionId(String previousCaseDefinitionId) {
    this.previousCaseDefinitionId = previousCaseDefinitionId;
  }

  protected void ensurePreviousCaseDefinitionIdInitialized() {
    if (previousCaseDefinitionId == null && !firstVersion) {
      previousCaseDefinitionId = Context
          .getCommandContext()
          .getCaseDefinitionManager()
          .findPreviousCaseDefinitionIdByKeyAndVersion(key, version);

      if (previousCaseDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  @Override
  protected CmmnExecution newCaseInstance() {
    CaseExecutionEntity caseInstance = new CaseExecutionEntity();
    Context
        .getCommandContext()
        .getCaseExecutionManager()
        .insertCaseExecution(caseInstance);
    return caseInstance;
  }

  public Object getPersistentState() {
    return CaseDefinitionEntity.class;
  }

  public String toString() {
    return "CaseDefinitionEntity["+id+"]";
  }

}
