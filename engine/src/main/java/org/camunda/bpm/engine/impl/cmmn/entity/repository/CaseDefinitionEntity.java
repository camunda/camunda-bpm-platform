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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.CaseDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionEntity extends CmmnCaseDefinition implements CaseDefinition, ResourceDefinitionEntity<CaseDefinitionEntity>, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected int revision = 1;
  protected String category;
  protected String key;
  protected int version;
  protected String deploymentId;
  protected String resourceName;
  protected String diagramResourceName;
  protected String tenantId;
  protected Integer historyTimeToLive;

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

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  // previous case definition //////////////////////////////////////////////

  public CaseDefinitionEntity getPreviousDefinition() {
    CaseDefinitionEntity previousCaseDefinition = null;

    String previousCaseDefinitionId = getPreviousCaseDefinitionId();
    if (previousCaseDefinitionId != null) {

      previousCaseDefinition = loadCaseDefinition(previousCaseDefinitionId);

      if (previousCaseDefinition == null) {
        resetPreviousCaseDefinitionId();
        previousCaseDefinitionId = getPreviousCaseDefinitionId();

        if (previousCaseDefinitionId != null) {
          previousCaseDefinition = loadCaseDefinition(previousCaseDefinitionId);
        }
      }
    }

    return previousCaseDefinition;
  }

  /**
   * Returns the cached version if exists; does not update the entity from the database in that case
   */
  protected CaseDefinitionEntity loadCaseDefinition(String caseDefinitionId) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = configuration.getDeploymentCache();

    CaseDefinitionEntity caseDefinition = deploymentCache.findCaseDefinitionFromCache(caseDefinitionId);

    if (caseDefinition == null) {
      CommandContext commandContext = Context.getCommandContext();
      CaseDefinitionManager caseDefinitionManager = commandContext.getCaseDefinitionManager();
      caseDefinition = caseDefinitionManager.findCaseDefinitionById(caseDefinitionId);

      if (caseDefinition != null) {
        caseDefinition = deploymentCache.resolveCaseDefinition(caseDefinition);
      }
    }

    return caseDefinition;

  }

  protected String getPreviousCaseDefinitionId() {
    ensurePreviousCaseDefinitionIdInitialized();
    return previousCaseDefinitionId;
  }

  protected void setPreviousCaseDefinitionId(String previousCaseDefinitionId) {
    this.previousCaseDefinitionId = previousCaseDefinitionId;
  }

  protected void resetPreviousCaseDefinitionId() {
    previousCaseDefinitionId = null;
    ensurePreviousCaseDefinitionIdInitialized();
  }

  protected void ensurePreviousCaseDefinitionIdInitialized() {
    if (previousCaseDefinitionId == null && !firstVersion) {
      previousCaseDefinitionId = Context
          .getCommandContext()
          .getCaseDefinitionManager()
          .findPreviousCaseDefinitionId(key, version, tenantId);

      if (previousCaseDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  @Override
  protected CmmnExecution newCaseInstance() {
    CaseExecutionEntity caseInstance = new CaseExecutionEntity();

    if (tenantId != null) {
      caseInstance.setTenantId(tenantId);
    }

    Context
        .getCommandContext()
        .getCaseExecutionManager()
        .insertCaseExecution(caseInstance);
    return caseInstance;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("historyTimeToLive", this.historyTimeToLive);
    return persistentState;
  }

  @Override
  public String toString() {
    return "CaseDefinitionEntity["+id+"]";
  }

  /**
   * Updates all modifiable fields from another case definition entity.
   * @param updatingCaseDefinition
   */
  @Override
  public void updateModifiableFieldsFromEntity(CaseDefinitionEntity updatingCaseDefinition) {
    if (this.key.equals(updatingCaseDefinition.key) && this.deploymentId.equals(updatingCaseDefinition.deploymentId)) {
      this.revision = updatingCaseDefinition.revision;
      this.historyTimeToLive = updatingCaseDefinition.historyTimeToLive;
    }
    else {
      LOG.logUpdateUnrelatedCaseDefinitionEntity(this.key, updatingCaseDefinition.key, this.deploymentId, updatingCaseDefinition.deploymentId);
    }
  }
}
