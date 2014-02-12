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

package org.camunda.bpm.engine.impl.persistence.deploy;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class DeploymentCache {

  private Logger LOGGER = Logger.getLogger(DeploymentCache.class.getName());

  protected Map<String, ProcessDefinitionEntity> processDefinitionCache = new HashMap<String, ProcessDefinitionEntity>();
  protected Map<String, BpmnModelInstance> bpmnModelInstanceCache = new HashMap<String, BpmnModelInstance>();
  protected Map<String, Object> knowledgeBaseCache = new HashMap<String, Object>();
  protected List<Deployer> deployers;

  public void deploy(DeploymentEntity deployment) {
    for (Deployer deployer: deployers) {
      deployer.deploy(deployment);
    }
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ProcessEngineException("Invalid process definition id : null");
    }
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findLatestProcessDefinitionById(processDefinitionId);
    if(processDefinition == null) {
      throw new ProcessEngineException("no deployed process definition found with id '" + processDefinitionId + "'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findLatestProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition==null) {
      throw new ProcessEngineException("no processes deployed with key '"+processDefinitionKey+"'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findProcessDefinitionByKeyAndVersion(processDefinitionKey, processDefinitionVersion);
    if (processDefinition==null) {
      throw new ProcessEngineException("no processes deployed with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);
    if (processDefinition==null) {
      throw new ProcessEngineException("no processes deployed with key = '" + processDefinitionKey + "' in deployment = '" + deploymentId + "'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String processDefinitionId = processDefinition.getId();
    String deploymentId = processDefinition.getDeploymentId();
    ProcessDefinitionEntity cachedProcessDefinition = processDefinitionCache.get(processDefinitionId);
    if (cachedProcessDefinition==null) {
      DeploymentEntity deployment = Context
        .getCommandContext()
        .getDeploymentManager()
        .findDeploymentById(deploymentId);
      deployment.setNew(false);
      deploy(deployment);
      cachedProcessDefinition = processDefinitionCache.get(processDefinitionId);

      if (cachedProcessDefinition==null) {
        throw new ProcessEngineException("deployment '"+deploymentId+"' didn't put process definition '"+processDefinitionId+"' in the cache");
      }
    } else {
      // update cached process definition
      cachedProcessDefinition.updateModifiedFieldsFromEntity(processDefinition);
    }
    return cachedProcessDefinition;
  }

  public BpmnModelInstance findBpmnModelInstanceForProcessDefinition(String processDefinitionId) {
    BpmnModelInstance bpmnModelInstance = bpmnModelInstanceCache.get(processDefinitionId);
    if(bpmnModelInstance == null) {

      ProcessDefinitionEntity processDefinition = findDeployedProcessDefinitionById(processDefinitionId);
      String deploymentId = processDefinition.getDeploymentId();
      String resourceName = processDefinition.getResourceName();

      InputStream bpmnResourceInputStream = new GetDeploymentResourceCmd(deploymentId, resourceName)
        .execute(Context.getCommandContext());

      try {
        bpmnModelInstance = Bpmn.readModelFromStream(bpmnResourceInputStream);
      }catch(Exception e) {
        throw new ProcessEngineException("Could not load Bpmn Model for process definition "+processDefinitionId, e);
      }

      // put model instance into cache.
      bpmnModelInstanceCache.put(processDefinitionId, bpmnModelInstance);

    }
    return bpmnModelInstance;
  }

  public void addProcessDefinition(ProcessDefinitionEntity processDefinition) {
    processDefinitionCache.put(processDefinition.getId(), processDefinition);
  }

  public void removeProcessDefinition(String processDefinitionId) {
    processDefinitionCache.remove(processDefinitionId);
  }

  public void addKnowledgeBase(String knowledgeBaseId, Object knowledgeBase) {
    knowledgeBaseCache.put(knowledgeBaseId, knowledgeBase);
  }

  public void removeKnowledgeBase(String knowledgeBaseId) {
    knowledgeBaseCache.remove(knowledgeBaseId);
  }

  public void discardProcessDefinitionCache() {
    processDefinitionCache.clear();
  }

  public void discardKnowledgeBaseCache() {
    knowledgeBaseCache.clear();
  }
  // getters and setters //////////////////////////////////////////////////////

  public Map<String, BpmnModelInstance> getBpmnModelInstanceCache() {
    return bpmnModelInstanceCache;
  }

  public Map<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionCache;
  }

  public void setProcessDefinitionCache(Map<String, ProcessDefinitionEntity> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }

  public Map<String, Object> getKnowledgeBaseCache() {
    return knowledgeBaseCache;
  }

  public void setKnowledgeBaseCache(Map<String, Object> knowledgeBaseCache) {
    this.knowledgeBaseCache = knowledgeBaseCache;
  }

  public List<Deployer> getDeployers() {
    return deployers;
  }

  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public void removeDeployment(String deploymentId) {
    // remove all process definitions for a specific deployment
    List<ProcessDefinition> allDefinitionsForDeployment = new ProcessDefinitionQueryImpl(Context.getCommandContext())
      .deploymentId(deploymentId)
      .list();
    for (ProcessDefinition processDefinition : allDefinitionsForDeployment) {
      try {
        removeProcessDefinition(processDefinition.getId());

      } catch(Exception e) {
        LOGGER.log(Level.WARNING, "Could not remove process definition with id '"+processDefinition.getId()+"' from the cache.", e);

      }
    }


  }
}
