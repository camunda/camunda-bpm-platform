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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.IdentityLinkType;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, DbEntity, HasDbRevision {

  private static final long serialVersionUID = 1L;

  protected String key;
  protected int revision = 1;
  protected int version;
  protected String category;
  protected String deploymentId;
  protected String resourceName;
  protected Integer historyLevel;
  protected StartFormHandler startFormHandler;
  protected String diagramResourceName;
  protected boolean isGraphicalNotationDefined;
  protected Map<String, TaskDefinition> taskDefinitions;
  protected boolean hasStartFormKey;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected boolean isIdentityLinksInitialized = false;
  protected List<IdentityLinkEntity> definitionIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
  protected Set<Expression> candidateStarterUserIdExpressions = new HashSet<Expression>();
  protected Set<Expression> candidateStarterGroupIdExpressions = new HashSet<Expression>();

  public ProcessDefinitionEntity() {
    super(null);
  }

  protected void ensureNotSuspended() {
    if (isSuspended()) {
      throw new SuspendedEntityInteractionException("Process definition " + id + " is suspended.");
    }
  }

  public ExecutionEntity createProcessInstance(String businessKey, ActivityImpl initial) {
    ensureNotSuspended();

    ExecutionEntity processInstance = null;

    if(initial == null) {
      processInstance = (ExecutionEntity) super.createProcessInstance();
    }else {
      processInstance = (ExecutionEntity) super.createProcessInstanceForInitial(initial);
    }

    processInstance.setExecutions(new ArrayList<ExecutionEntity>());
    processInstance.setProcessDefinition(processDefinition);
    // Do not initialize variable map (let it happen lazily)

    if (businessKey != null) {
    	processInstance.setBusinessKey(businessKey);
    }

    // reset the process instance in order to have the db-generated process instance id available
    processInstance.setProcessInstance(processInstance);

    String initiatorVariableName = (String) getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
    if (initiatorVariableName!=null) {
      String authenticatedUserId = Context.getCommandContext().getAuthenticatedUserId();
      processInstance.setVariable(initiatorVariableName, authenticatedUserId);
    }

    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    int historyLevel = configuration.getHistoryLevel();
    // TODO: This smells bad, as the rest of the history is done via the ParseListener
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      final HistoryEventProducer eventFactory = configuration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = configuration.getHistoryEventHandler();

      // publish event for historic process instance start
      HistoryEvent pise = eventFactory.createProcessInstanceStartEvt(processInstance);
      eventHandler.handleEvent(pise);

    }

    return processInstance;
  }
  public ExecutionEntity createProcessInstance(String businessKey) {
    return createProcessInstance(businessKey, null);
  }

  public ExecutionEntity createProcessInstance() {
    return createProcessInstance(null);
  }


  @Override
  protected PvmExecutionImpl newProcessInstance(ActivityImpl activityImpl) {
    ExecutionEntity processInstance = new ExecutionEntity(activityImpl);
    processInstance.insert();
    return processInstance;
  }

  public IdentityLinkEntity addIdentityLink(String userId, String groupId) {
    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.createAndInsert();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessDef(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(IdentityLinkType.CANDIDATE);
    return identityLinkEntity;
  }

  public void deleteIdentityLink(String userId, String groupId) {
    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkManager()
      .findIdentityLinkByProcessDefinitionUserAndGroup(id, userId, groupId);

    for (IdentityLinkEntity identityLink: identityLinks) {
      Context
        .getCommandContext()
        .getDbSqlSession()
        .delete(identityLink);
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

  public String toString() {
    return "ProcessDefinitionEntity["+id+"]";
  }

  /**
   * Updates all modifiable fields from another process definition entity.
   * @param updatingProcessDefinition
   */
  public void updateModifiedFieldsFromEntity(ProcessDefinitionEntity updatingProcessDefinition) {
    if (!this.key.equals(updatingProcessDefinition.key) || !this.deploymentId.equals(updatingProcessDefinition.deploymentId)) {
      throw new ProcessEngineException("Cannot update entity from an unrelated process definition");
    }

    // TODO: add a guard once the mismatch between revisions in deployment cache and database has been resolved
    this.revision = updatingProcessDefinition.revision;
    this.suspensionState = updatingProcessDefinition.suspensionState;

  }


  // getters and setters //////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("suspensionState", this.suspensionState);
    return persistentState;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDescription() {
    return (String) getProperty(BpmnParse.PROPERTYNAME_DOCUMENTATION);
  }

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
  }

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

}
