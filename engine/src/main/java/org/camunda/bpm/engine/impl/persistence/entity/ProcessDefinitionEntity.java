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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.form.FormDefinition;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.IdentityLinkType;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, ResourceDefinitionEntity<ProcessDefinitionEntity>, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String key;
  protected int revision = 1;
  protected int version;
  protected String category;
  protected String deploymentId;
  protected String resourceName;
  protected Integer historyLevel;
  protected StartFormHandler startFormHandler;
  protected FormDefinition startFormDefinition;
  protected String diagramResourceName;
  protected boolean isGraphicalNotationDefined;
  protected Map<String, TaskDefinition> taskDefinitions;
  protected boolean hasStartFormKey;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected String tenantId;
  protected String versionTag;
  protected Integer historyTimeToLive;
  protected boolean isIdentityLinksInitialized = false;
  protected List<IdentityLinkEntity> definitionIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
  protected Set<Expression> candidateStarterUserIdExpressions = new HashSet<Expression>();
  protected Set<Expression> candidateStarterGroupIdExpressions = new HashSet<Expression>();
  protected boolean isStartableInTasklist;

  // firstVersion is true, when version == 1 or when
  // this definition does not have any previous definitions
  protected boolean firstVersion = false;
  protected String previousProcessDefinitionId;

  public ProcessDefinitionEntity() {
    super(null);
  }

  protected void ensureNotSuspended() {
    if (isSuspended()) {
      throw LOG.suspendedEntityException("Process Definition", id);
    }
  }

  @Override
  public ExecutionEntity createProcessInstance() {
    return (ExecutionEntity) super.createProcessInstance();
  }

  @Override
  public ExecutionEntity createProcessInstance(String businessKey) {
    return (ExecutionEntity) super.createProcessInstance(businessKey);
  }

  @Override
  public ExecutionEntity createProcessInstance(String businessKey, String caseInstanceId) {
    return (ExecutionEntity) super.createProcessInstance(businessKey, caseInstanceId);
  }

  @Override
  public ExecutionEntity createProcessInstance(String businessKey, ActivityImpl initial) {
    return (ExecutionEntity) super.createProcessInstance(businessKey, initial);
  }

  @Override
  protected PvmExecutionImpl newProcessInstance() {
    ExecutionEntity newExecution = ExecutionEntity.createNewExecution();

    if(tenantId != null) {
      newExecution.setTenantId(tenantId);
    }

    return newExecution;
  }

  @Override
  public ExecutionEntity createProcessInstance(String businessKey, String caseInstanceId, ActivityImpl initial) {
    ensureNotSuspended();

    ExecutionEntity processInstance = (ExecutionEntity) createProcessInstanceForInitial(initial);

    // do not reset executions (CAM-2557)!
    // processInstance.setExecutions(new ArrayList<ExecutionEntity>());

    processInstance.setProcessDefinition(processDefinition);

    // Do not initialize variable map (let it happen lazily)

    // reset the process instance in order to have the db-generated process instance id available
    processInstance.setProcessInstance(processInstance);

    // initialize business key
    if (businessKey != null) {
      processInstance.setBusinessKey(businessKey);
    }

    // initialize case instance id
    if (caseInstanceId != null) {
      processInstance.setCaseInstanceId(caseInstanceId);
    }

    if(tenantId != null) {
      processInstance.setTenantId(tenantId);
    }

    return processInstance;
  }

  public IdentityLinkEntity addIdentityLink(String userId, String groupId) {
    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.newIdentityLink();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessDef(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(IdentityLinkType.CANDIDATE);
    identityLinkEntity.setTenantId(getTenantId());
    identityLinkEntity.insert();
    return identityLinkEntity;
  }

  public void deleteIdentityLink(String userId, String groupId) {
    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkManager()
      .findIdentityLinkByProcessDefinitionUserAndGroup(id, userId, groupId);

    for (IdentityLinkEntity identityLink: identityLinks) {
      identityLink.delete();
    }
  }

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      definitionIdentityLinkEntities = Context
        .getCommandContext()
        .getIdentityLinkManager()
        .findIdentityLinksByProcessDefinitionId(id);
      isIdentityLinksInitialized = true;
    }

    return definitionIdentityLinkEntities;
  }

  @Override
  public String toString() {
    return "ProcessDefinitionEntity["+id+"]";
  }

  /**
   * Updates all modifiable fields from another process definition entity.
   * @param updatingProcessDefinition
   */
  @Override
  public void updateModifiableFieldsFromEntity(ProcessDefinitionEntity updatingProcessDefinition) {
    if (this.key.equals(updatingProcessDefinition.key) && this.deploymentId.equals(updatingProcessDefinition.deploymentId)) {
      // TODO: add a guard once the mismatch between revisions in deployment cache and database has been resolved
      this.revision = updatingProcessDefinition.revision;
      this.suspensionState = updatingProcessDefinition.suspensionState;
      this.historyTimeToLive = updatingProcessDefinition.historyTimeToLive;
    }
    else {
      LOG.logUpdateUnrelatedProcessDefinitionEntity(this.key, updatingProcessDefinition.key, this.deploymentId, updatingProcessDefinition.deploymentId);
    }
  }

  // previous process definition //////////////////////////////////////////////

  public ProcessDefinitionEntity getPreviousDefinition() {
    ProcessDefinitionEntity previousProcessDefinition = null;

    String previousProcessDefinitionId = getPreviousProcessDefinitionId();
    if (previousProcessDefinitionId != null) {

      previousProcessDefinition = loadProcessDefinition(previousProcessDefinitionId);

      if (previousProcessDefinition == null) {
        resetPreviousProcessDefinitionId();
        previousProcessDefinitionId = getPreviousProcessDefinitionId();

        if (previousProcessDefinitionId != null) {
          previousProcessDefinition = loadProcessDefinition(previousProcessDefinitionId);
        }
      }
    }

    return previousProcessDefinition;
  }

  /**
   * Returns the cached version if exists; does not update the entity from the database in that case
   */
  protected ProcessDefinitionEntity loadProcessDefinition(String processDefinitionId) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = configuration.getDeploymentCache();

    ProcessDefinitionEntity processDefinition = deploymentCache.findProcessDefinitionFromCache(processDefinitionId);

    if (processDefinition == null) {
      CommandContext commandContext = Context.getCommandContext();
      ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
      processDefinition = processDefinitionManager.findLatestProcessDefinitionById(processDefinitionId);

      if (processDefinition != null) {
        processDefinition = deploymentCache.resolveProcessDefinition(processDefinition);
      }
    }

    return processDefinition;

  }

  public String getPreviousProcessDefinitionId() {
    ensurePreviousProcessDefinitionIdInitialized();
    return previousProcessDefinitionId;
  }

  protected void resetPreviousProcessDefinitionId() {
    previousProcessDefinitionId = null;
    ensurePreviousProcessDefinitionIdInitialized();
  }

  protected void setPreviousProcessDefinitionId(String previousProcessDefinitionId) {
    this.previousProcessDefinitionId = previousProcessDefinitionId;
  }

  protected void ensurePreviousProcessDefinitionIdInitialized() {
    if (previousProcessDefinitionId == null && !firstVersion) {
      previousProcessDefinitionId = Context
          .getCommandContext()
          .getProcessDefinitionManager()
          .findPreviousProcessDefinitionId(key, version, tenantId);

      if (previousProcessDefinitionId == null) {
        firstVersion = true;
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("suspensionState", this.suspensionState);
    persistentState.put("historyTimeToLive", this.historyTimeToLive);
    return persistentState;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String getDescription() {
    return (String) getProperty(BpmnParse.PROPERTYNAME_DOCUMENTATION);
  }

  @Override
  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
    firstVersion = (this.version == 1);
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public Integer getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(Integer historyLevel) {
    this.historyLevel = historyLevel;
  }

  public StartFormHandler getStartFormHandler() {
    return startFormHandler;
  }

  public void setStartFormHandler(StartFormHandler startFormHandler) {
    this.startFormHandler = startFormHandler;
  }

  public FormDefinition getStartFormDefinition() {
    return startFormDefinition;
  }

  public void setStartFormDefinition(FormDefinition startFormDefinition) {
    this.startFormDefinition = startFormDefinition;
  }

  public Map<String, TaskDefinition> getTaskDefinitions() {
    return taskDefinitions;
  }

  public void setTaskDefinitions(Map<String, TaskDefinition> taskDefinitions) {
    this.taskDefinitions = taskDefinitions;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }

  public boolean hasStartFormKey() {
    return hasStartFormKey;
  }

  public boolean getHasStartFormKey() {
    return hasStartFormKey;
  }

  public void setStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }

  public void setHasStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }

  public boolean isGraphicalNotationDefined() {
    return isGraphicalNotationDefined;
  }

  public void setGraphicalNotationDefined(boolean isGraphicalNotationDefined) {
    this.isGraphicalNotationDefined = isGraphicalNotationDefined;
  }

  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }

  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  public Set<Expression> getCandidateStarterUserIdExpressions() {
    return candidateStarterUserIdExpressions;
  }

  public void addCandidateStarterUserIdExpression(Expression userId) {
    candidateStarterUserIdExpressions.add(userId);
  }

  public Set<Expression> getCandidateStarterGroupIdExpressions() {
    return candidateStarterGroupIdExpressions;
  }

  public void addCandidateStarterGroupIdExpression(Expression groupId) {
    candidateStarterGroupIdExpressions.add(groupId);
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getVersionTag() {
    return versionTag;
  }

  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }

  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  public boolean isStartableInTasklist() {
    return isStartableInTasklist;
  }

  public void setStartableInTasklist(boolean isStartableInTasklist) {
    this.isStartableInTasklist = isStartableInTasklist;
  }
}
