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
import org.camunda.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.cmd.GetDeploymentResourceCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class DeploymentCache {

  private Logger LOGGER = Logger.getLogger(DeploymentCache.class.getName());

  protected Map<String, ProcessDefinitionEntity> processDefinitionCache = new HashMap<String, ProcessDefinitionEntity>();
  protected Map<String, CaseDefinitionEntity> caseDefinitionCache = new HashMap<String, CaseDefinitionEntity>();
  protected Map<String, BpmnModelInstance> bpmnModelInstanceCache = new HashMap<String, BpmnModelInstance>();
  protected Map<String, CmmnModelInstance> cmmnModelInstanceCache = new HashMap<String, CmmnModelInstance>();
  protected List<Deployer> deployers;

  public void deploy(DeploymentEntity deployment) {
    for (Deployer deployer: deployers) {
      deployer.deploy(deployment);
    }
  }

  // PROCESS DEFINITION ////////////////////////////////////////////////////////////////////////////////

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    ensureNotNull("Invalid process definition id", "processDefinitionId", processDefinitionId);
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionEntity processDefinition = commandContext.getDbEntityManager().getCachedEntity(ProcessDefinitionEntity.class, processDefinitionId);
    if (processDefinition == null) {
      processDefinition = commandContext
        .getProcessDefinitionManager()
        .findLatestProcessDefinitionById(processDefinitionId);
    }
    ensureNotNull("no deployed process definition found with id '" + processDefinitionId + "'", "processDefinition", processDefinition);
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findLatestProcessDefinitionByKey(processDefinitionKey);
    ensureNotNull("no processes deployed with key '" + processDefinitionKey + "'", "processDefinition", processDefinition);
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findProcessDefinitionByKeyAndVersion(processDefinitionKey, processDefinitionVersion);
    ensureNotNull("no processes deployed with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'", "processDefinition", processDefinition);
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);
    ensureNotNull("no processes deployed with key = '" + processDefinitionKey + "' in deployment = '" + deploymentId + "'", "processDefinition", processDefinition);
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

      ensureNotNull("deployment '" + deploymentId + "' didn't put process definition '" + processDefinitionId + "' in the cache", "cachedProcessDefinition", cachedProcessDefinition);
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

  public void discardProcessDefinitionCache() {
    processDefinitionCache.clear();
  }

  // CASE DEFINITION ////////////////////////////////////////////////////////////////////////////////

  public CaseDefinitionEntity findDeployedCaseDefinitionById(String caseDefinitionId) {
    ensureNotNull("Invalid case definition id", "caseDefinitionId", caseDefinitionId);

    CommandContext commandContext = Context.getCommandContext();

    // try to load case definition from cache
    CaseDefinitionEntity caseDefinition = commandContext
      .getDbEntityManager()
      .getCachedEntity(CaseDefinitionEntity.class, caseDefinitionId);

    if (caseDefinition == null) {

      // if not found, then load the case definition
      // from db
      caseDefinition = commandContext
        .getCaseDefinitionManager()
        .findCaseDefinitionById(caseDefinitionId);

    }

    ensureNotNull(CaseDefinitionNotFoundException.class, "no deployed case definition found with id '" + caseDefinitionId + "'", "caseDefinition", caseDefinition);

    caseDefinition = resolveCaseDefinition(caseDefinition);

    return caseDefinition;
  }

  public CaseDefinitionEntity findDeployedLatestCaseDefinitionByKey(String caseDefinitionKey) {
    ensureNotNull("Invalid case definition key", "caseDefinitionKey", caseDefinitionKey);

    // load case definition by key from db
    CaseDefinitionEntity caseDefinition = Context
      .getCommandContext()
      .getCaseDefinitionManager()
      .findLatestCaseDefinitionByKey(caseDefinitionKey);

    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key '" + caseDefinitionKey + "'", "caseDefinition", caseDefinition);

    caseDefinition = resolveCaseDefinition(caseDefinition);

    return caseDefinition;
  }

  public CaseDefinitionEntity findDeployedCaseDefinitionByKeyAndVersion(String caseDefinitionKey, Integer caseDefinitionVersion) {

    CaseDefinitionEntity caseDefinition = Context
      .getCommandContext()
      .getCaseDefinitionManager()
      .findCaseDefinitionByKeyAndVersion(caseDefinitionKey, caseDefinitionVersion);

    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key = '" + caseDefinitionKey + "' and version = '" + caseDefinitionVersion + "'", "caseDefinition", caseDefinition);
    caseDefinition = resolveCaseDefinition(caseDefinition);

    return caseDefinition;
  }

  public CaseDefinitionEntity findDeployedCaseDefinitionByDeploymentAndKey(String deploymentId, String caseDefinitionKey) {
    CaseDefinitionEntity caseDefinition = Context
      .getCommandContext()
      .getCaseDefinitionManager()
      .findCaseDefinitionByDeploymentAndKey(deploymentId, caseDefinitionKey);

    ensureNotNull(CaseDefinitionNotFoundException.class, "no case definition deployed with key = '" + caseDefinitionKey + "' in deployment = '" + deploymentId + "'", "caseDefinition", caseDefinition);
    caseDefinition = resolveCaseDefinition(caseDefinition);

    return caseDefinition;
  }

  public CaseDefinitionEntity getCaseDefinitionById(String caseDefinitionId) {
    ensureNotNull("caseDefinitionId", caseDefinitionId);

    CaseDefinitionEntity caseDefinition = caseDefinitionCache.get(caseDefinitionId);

    if (caseDefinition == null) {
      caseDefinition = findDeployedCaseDefinitionById(caseDefinitionId);

    }

    return caseDefinition;
  }

  public CaseDefinitionEntity resolveCaseDefinition(CaseDefinitionEntity caseDefinition) {
    String caseDefinitionId = caseDefinition.getId();
    String deploymentId = caseDefinition.getDeploymentId();

    CaseDefinitionEntity cachedCaseDefinition = caseDefinitionCache.get(caseDefinitionId);

    if (cachedCaseDefinition==null) {
      DeploymentEntity deployment = Context
        .getCommandContext()
        .getDeploymentManager()
        .findDeploymentById(deploymentId);

      deployment.setNew(false);
      deploy(deployment);

      cachedCaseDefinition = caseDefinitionCache.get(caseDefinitionId);

      ensureNotNull("deployment '" + deploymentId + "' didn't put case definition '" + caseDefinitionId + "' in the cache", "cachedCaseDefinition", cachedCaseDefinition);

    }
    return cachedCaseDefinition;
  }

  public CmmnModelInstance findCmmnModelInstanceForCaseDefinition(String caseDefinitionId) {
    CmmnModelInstance cmmnModelInstance = cmmnModelInstanceCache.get(caseDefinitionId);
    if(cmmnModelInstance == null) {

      CaseDefinitionEntity caseDefinition = findDeployedCaseDefinitionById(caseDefinitionId);
      String deploymentId = caseDefinition.getDeploymentId();
      String resourceName = caseDefinition.getResourceName();

      InputStream cmmnResourceInputStream = new GetDeploymentResourceCmd(deploymentId, resourceName)
        .execute(Context.getCommandContext());

      try {
        cmmnModelInstance = Cmmn.readModelFromStream(cmmnResourceInputStream);
      }catch(Exception e) {
        throw new ProcessEngineException("Could not load Cmmn Model for case definition " + caseDefinitionId, e);
      }

      // put model instance into cache.
      cmmnModelInstanceCache.put(caseDefinitionId, cmmnModelInstance);

    }
    return cmmnModelInstance;
  }

  public void addCaseDefinition(CaseDefinitionEntity caseDefinition) {
    caseDefinitionCache.put(caseDefinition.getId(), caseDefinition);
  }

  public void removeCaseDefinition(String caseDefinitionId) {
    caseDefinitionCache.remove(caseDefinitionId);
  }

  public void discardCaseDefinitionCache() {
    caseDefinitionCache.clear();
  }

  // getters and setters //////////////////////////////////////////////////////

  public Map<String, BpmnModelInstance> getBpmnModelInstanceCache() {
    return bpmnModelInstanceCache;
  }

  public Map<String, CmmnModelInstance> getCmmnModelInstanceCache() {
    return cmmnModelInstanceCache;
  }

  public Map<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionCache;
  }

  public void setProcessDefinitionCache(Map<String, ProcessDefinitionEntity> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }

  public Map<String, CaseDefinitionEntity> getCaseDefinitionCache() {
    return caseDefinitionCache;
  }

  public void setCaseDefinitionCache(Map<String, CaseDefinitionEntity> caseDefinitionCache) {
    this.caseDefinitionCache = caseDefinitionCache;
  }

  public List<Deployer> getDeployers() {
    return deployers;
  }

  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public void removeDeployment(String deploymentId) {
    removeAllProcessDefinitionsByDeploymentId(deploymentId);
    removeAllCaseDefinitionsByDeploymentId(deploymentId);
  }

  protected void removeAllProcessDefinitionsByDeploymentId(String deploymentId) {
    // remove all process definitions for a specific deployment
    CommandContext commandContext = Context.getCommandContext();

    List<ProcessDefinition> allDefinitionsForDeployment = new ProcessDefinitionQueryImpl(commandContext)
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

  protected void removeAllCaseDefinitionsByDeploymentId(String deploymentId) {
    // remove all case definitions for a specific deployment
    CommandContext commandContext = Context.getCommandContext();

    List<CaseDefinition> allDefinitionsForDeployment = new CaseDefinitionQueryImpl(commandContext)
        .deploymentId(deploymentId)
        .list();

    for (CaseDefinition caseDefinition : allDefinitionsForDeployment) {
      try {
        removeCaseDefinition(caseDefinition.getId());

      } catch(Exception e) {
        LOGGER.log(Level.WARNING, "Could not remove case definition with id '"+caseDefinition.getId()+"' from the cache.", e);

      }
    }
  }
}
