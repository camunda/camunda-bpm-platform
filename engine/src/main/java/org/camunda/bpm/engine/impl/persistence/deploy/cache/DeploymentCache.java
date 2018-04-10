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

package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.cache.Cache;

import java.util.List;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class DeploymentCache {

  protected ProcessDefinitionCache processDefinitionEntityCache;
  protected CaseDefinitionCache caseDefinitionCache;
  protected DecisionDefinitionCache decisionDefinitionCache;
  protected DecisionRequirementsDefinitionCache decisionRequirementsDefinitionCache;

  protected BpmnModelInstanceCache bpmnModelInstanceCache;
  protected CmmnModelInstanceCache cmmnModelInstanceCache;
  protected DmnModelInstanceCache dmnModelInstanceCache;
  protected CacheDeployer cacheDeployer = new CacheDeployer();

  public DeploymentCache(CacheFactory factory, int cacheCapacity) {
    processDefinitionEntityCache = new ProcessDefinitionCache(factory, cacheCapacity, cacheDeployer);
    caseDefinitionCache = new CaseDefinitionCache(factory, cacheCapacity, cacheDeployer);
    decisionDefinitionCache = new DecisionDefinitionCache(factory, cacheCapacity, cacheDeployer);
    decisionRequirementsDefinitionCache = new DecisionRequirementsDefinitionCache(factory, cacheCapacity, cacheDeployer);

    bpmnModelInstanceCache = new BpmnModelInstanceCache(factory, cacheCapacity, processDefinitionEntityCache);
    cmmnModelInstanceCache = new CmmnModelInstanceCache(factory, cacheCapacity, caseDefinitionCache);
    dmnModelInstanceCache = new DmnModelInstanceCache(factory, cacheCapacity, decisionDefinitionCache);
  }

  public void deploy(final DeploymentEntity deployment) {
    cacheDeployer.deploy(deployment);
  }

  // PROCESS DEFINITION ////////////////////////////////////////////////////////////////////////////////

  public ProcessDefinitionEntity findProcessDefinitionFromCache(String processDefinitionId) {
    return processDefinitionEntityCache.findDefinitionFromCache(processDefinitionId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    return processDefinitionEntityCache.findDeployedDefinitionById(processDefinitionId);
  }

  /**
   * @return the latest version of the process definition with the given key (from any tenant)
   * @throws ProcessEngineException if more than one tenant has a process definition with the given key
   * @see #findDeployedLatestProcessDefinitionByKeyAndTenantId(String, String)
   */
  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    return processDefinitionEntityCache.findDeployedLatestDefinitionByKey(processDefinitionKey);
  }

  /**
   * @return the latest version of the process definition with the given key and tenant id
   */
  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
    return processDefinitionEntityCache.findDeployedLatestDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyVersionAndTenantId(final String processDefinitionKey, final Integer processDefinitionVersion, final String tenantId) {
    return processDefinitionEntityCache.findDeployedDefinitionByKeyVersionAndTenantId(processDefinitionKey, processDefinitionVersion, tenantId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyVersionTagAndTenantId(String processDefinitionKey, String processDefinitionVersionTag, String tenantId) {
    return processDefinitionEntityCache.findDeployedDefinitionByKeyVersionTagAndTenantId(processDefinitionKey, processDefinitionVersionTag, tenantId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    return processDefinitionEntityCache.findDeployedDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);
  }

  public ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    return processDefinitionEntityCache.resolveDefinition(processDefinition);
  }

  public BpmnModelInstance findBpmnModelInstanceForProcessDefinition(ProcessDefinitionEntity processDefinitionEntity) {
    return bpmnModelInstanceCache.findBpmnModelInstanceForDefinition(processDefinitionEntity);
  }

  public BpmnModelInstance findBpmnModelInstanceForProcessDefinition(String processDefinitionId) {
    return bpmnModelInstanceCache.findBpmnModelInstanceForDefinition(processDefinitionId);
  }

  public void addProcessDefinition(ProcessDefinitionEntity processDefinition) {
    processDefinitionEntityCache.addDefinition(processDefinition);
  }

  public void removeProcessDefinition(String processDefinitionId) {
    processDefinitionEntityCache.removeDefinitionFromCache(processDefinitionId);
    bpmnModelInstanceCache.remove(processDefinitionId);
  }

  public void discardProcessDefinitionCache() {
    processDefinitionEntityCache.clear();
    bpmnModelInstanceCache.clear();
  }

  // CASE DEFINITION ////////////////////////////////////////////////////////////////////////////////

  public CaseDefinitionEntity findCaseDefinitionFromCache(String caseDefinitionId) {
    return caseDefinitionCache.findDefinitionFromCache(caseDefinitionId);
  }

  public CaseDefinitionEntity findDeployedCaseDefinitionById(String caseDefinitionId) {
    return caseDefinitionCache.findDeployedDefinitionById(caseDefinitionId);
  }

  /**
   * @return the latest version of the case definition with the given key (from any tenant)
   * @throws ProcessEngineException if more than one tenant has a case definition with the given key
   * @see #findDeployedLatestCaseDefinitionByKeyAndTenantId(String, String)
   */
  public CaseDefinitionEntity findDeployedLatestCaseDefinitionByKey(String caseDefinitionKey) {
    return caseDefinitionCache.findDeployedLatestDefinitionByKey(caseDefinitionKey);
  }

  /**
   * @return the latest version of the case definition with the given key and tenant id
   */
  public CaseDefinitionEntity findDeployedLatestCaseDefinitionByKeyAndTenantId(String caseDefinitionKey, String tenantId) {
    return caseDefinitionCache.findDeployedLatestDefinitionByKeyAndTenantId(caseDefinitionKey, tenantId);
  }

  public CaseDefinitionEntity findDeployedCaseDefinitionByKeyVersionAndTenantId(String caseDefinitionKey, Integer caseDefinitionVersion, String tenantId) {
    return caseDefinitionCache.findDeployedDefinitionByKeyVersionAndTenantId(caseDefinitionKey, caseDefinitionVersion, tenantId);
  }

  public CaseDefinitionEntity findDeployedCaseDefinitionByDeploymentAndKey(String deploymentId, String caseDefinitionKey) {
    return caseDefinitionCache.findDeployedDefinitionByDeploymentAndKey(deploymentId, caseDefinitionKey);
  }

  public CaseDefinitionEntity getCaseDefinitionById(String caseDefinitionId) {
    return caseDefinitionCache.getCaseDefinitionById(caseDefinitionId);
  }

  public CaseDefinitionEntity resolveCaseDefinition(CaseDefinitionEntity caseDefinition) {
    return caseDefinitionCache.resolveDefinition(caseDefinition);
  }

  public CmmnModelInstance findCmmnModelInstanceForCaseDefinition(String caseDefinitionId) {
    return cmmnModelInstanceCache.findBpmnModelInstanceForDefinition(caseDefinitionId);
  }

  public void addCaseDefinition(CaseDefinitionEntity caseDefinition) {
    caseDefinitionCache.addDefinition(caseDefinition);
  }

  public void removeCaseDefinition(String caseDefinitionId) {
    caseDefinitionCache.removeDefinitionFromCache(caseDefinitionId);
    cmmnModelInstanceCache.remove(caseDefinitionId);
  }

  public void discardCaseDefinitionCache() {
    caseDefinitionCache.clear();
    cmmnModelInstanceCache.clear();
  }

  // DECISION DEFINITION ////////////////////////////////////////////////////////////////////////////

  public DecisionDefinitionEntity findDecisionDefinitionFromCache(String decisionDefinitionId) {
    return decisionDefinitionCache.findDefinitionFromCache(decisionDefinitionId);
  }

  public DecisionDefinitionEntity findDeployedDecisionDefinitionById(String decisionDefinitionId) {
    return decisionDefinitionCache.findDeployedDefinitionById(decisionDefinitionId);
  }

  public DecisionDefinition findDeployedLatestDecisionDefinitionByKey(String decisionDefinitionKey) {
    return decisionDefinitionCache.findDeployedLatestDefinitionByKey(decisionDefinitionKey);
  }

  public DecisionDefinition findDeployedLatestDecisionDefinitionByKeyAndTenantId(String decisionDefinitionKey, String tenantId) {
    return decisionDefinitionCache.findDeployedLatestDefinitionByKeyAndTenantId(decisionDefinitionKey, tenantId);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByDeploymentAndKey(String deploymentId, String decisionDefinitionKey) {
    return decisionDefinitionCache.findDeployedDefinitionByDeploymentAndKey(deploymentId, decisionDefinitionKey);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByKeyAndVersion(String decisionDefinitionKey, Integer decisionDefinitionVersion) {
    return decisionDefinitionCache.findDeployedDefinitionByKeyAndVersion(decisionDefinitionKey, decisionDefinitionVersion);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByKeyVersionAndTenantId(String decisionDefinitionKey, Integer decisionDefinitionVersion, String tenantId) {
    return decisionDefinitionCache.findDeployedDefinitionByKeyVersionAndTenantId(decisionDefinitionKey, decisionDefinitionVersion, tenantId);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByKeyVersionTagAndTenantId(String decisionDefinitionKey, String decisionDefinitionVersionTag, String tenantId) {
    return decisionDefinitionCache.findDeployedDefinitionByKeyVersionTagAndTenantId(decisionDefinitionKey, decisionDefinitionVersionTag, tenantId);
  }

  public DecisionDefinitionEntity resolveDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    return decisionDefinitionCache.resolveDefinition(decisionDefinition);
  }

  public DmnModelInstance findDmnModelInstanceForDecisionDefinition(String decisionDefinitionId) {
    return dmnModelInstanceCache.findBpmnModelInstanceForDefinition(decisionDefinitionId);
  }

  public void addDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    decisionDefinitionCache.addDefinition(decisionDefinition);
  }

  public void removeDecisionDefinition(String decisionDefinitionId) {
    decisionDefinitionCache.removeDefinitionFromCache(decisionDefinitionId);
    dmnModelInstanceCache.remove(decisionDefinitionId);
  }

  public void discardDecisionDefinitionCache() {
    decisionDefinitionCache.clear();
    dmnModelInstanceCache.clear();
  }

  //DECISION REQUIREMENT DEFINITION ////////////////////////////////////////////////////////////////////////////

  public void addDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    decisionRequirementsDefinitionCache.addDefinition(decisionRequirementsDefinition);
  }

  public DecisionRequirementsDefinitionEntity findDecisionRequirementsDefinitionFromCache(String decisionRequirementsDefinitionId) {
    return decisionRequirementsDefinitionCache.findDefinitionFromCache(decisionRequirementsDefinitionId);
  }

  public DecisionRequirementsDefinitionEntity findDeployedDecisionRequirementsDefinitionById(String decisionRequirementsDefinitionId) {
    return decisionRequirementsDefinitionCache.findDeployedDefinitionById(decisionRequirementsDefinitionId);
  }

  public DecisionRequirementsDefinitionEntity resolveDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    return decisionRequirementsDefinitionCache.resolveDefinition(decisionRequirementsDefinition);
  }

  public void discardDecisionRequirementsDefinitionCache() {
    decisionDefinitionCache.clear();
  }

  public void removeDecisionRequirementsDefinition(String decisionRequirementsDefinitionId) {
    decisionRequirementsDefinitionCache.removeDefinitionFromCache(decisionRequirementsDefinitionId);
  }

  // getters and setters //////////////////////////////////////////////////////

  public Cache<String, BpmnModelInstance> getBpmnModelInstanceCache() {
    return bpmnModelInstanceCache.getCache();
  }

  public Cache<String, CmmnModelInstance> getCmmnModelInstanceCache() {
    return cmmnModelInstanceCache.getCache();
  }

  public Cache<String, DmnModelInstance> getDmnDefinitionCache() {
    return dmnModelInstanceCache.getCache();
  }

  public Cache<String, DecisionDefinitionEntity> getDecisionDefinitionCache() {
    return decisionDefinitionCache.getCache();
  }

  public Cache<String, DecisionRequirementsDefinitionEntity> getDecisionRequirementsDefinitionCache() {
    return decisionRequirementsDefinitionCache.getCache();
  }

  public Cache<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionEntityCache.getCache();
  }

  public Cache<String, CaseDefinitionEntity> getCaseDefinitionCache() {
    return caseDefinitionCache.getCache();
  }

  public void setDeployers(List<Deployer> deployers) {
    this.cacheDeployer.setDeployers(deployers);
  }

  public void removeDeployment(String deploymentId) {
    bpmnModelInstanceCache.removeAllDefinitionsByDeploymentId(deploymentId);
    cmmnModelInstanceCache.removeAllDefinitionsByDeploymentId(deploymentId);
    dmnModelInstanceCache.removeAllDefinitionsByDeploymentId(deploymentId);
    removeAllDecisionRequirementsDefinitionsByDeploymentId(deploymentId);
  }

  protected void removeAllDecisionRequirementsDefinitionsByDeploymentId(String deploymentId) {
    // remove all decision requirements definitions for a specific deployment

    List<DecisionRequirementsDefinition> allDefinitionsForDeployment = new DecisionRequirementsDefinitionQueryImpl()
        .deploymentId(deploymentId)
        .list();

    for (DecisionRequirementsDefinition decisionRequirementsDefinition : allDefinitionsForDeployment) {
      try {
        removeDecisionDefinition(decisionRequirementsDefinition.getId());
      } catch (Exception e) {
        ProcessEngineLogger.PERSISTENCE_LOGGER
            .removeEntryFromDeploymentCacheFailure("decision requirement", decisionRequirementsDefinition.getId(), e);
      }
    }
  }

  public CachePurgeReport purgeCache() {

    CachePurgeReport result = new CachePurgeReport();
    Cache<String, ProcessDefinitionEntity> processDefinitionCache = getProcessDefinitionCache();
    if (!processDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.PROCESS_DEF_CACHE, processDefinitionCache.keySet());
      processDefinitionCache.clear();
    }

    Cache<String, BpmnModelInstance> bpmnModelInstanceCache = getBpmnModelInstanceCache();
    if (!bpmnModelInstanceCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.BPMN_MODEL_INST_CACHE, bpmnModelInstanceCache.keySet());
      bpmnModelInstanceCache.clear();
    }

    Cache<String, CaseDefinitionEntity> caseDefinitionCache = getCaseDefinitionCache();
    if (!caseDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.CASE_DEF_CACHE, caseDefinitionCache.keySet());
      caseDefinitionCache.clear();
    }

    Cache<String, CmmnModelInstance> cmmnModelInstanceCache = getCmmnModelInstanceCache();
    if (!cmmnModelInstanceCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.CASE_MODEL_INST_CACHE, cmmnModelInstanceCache.keySet());
      cmmnModelInstanceCache.clear();
    }

    Cache<String, DecisionDefinitionEntity> decisionDefinitionCache = getDecisionDefinitionCache();
    if (!decisionDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.DMN_DEF_CACHE, decisionDefinitionCache.keySet());
      decisionDefinitionCache.clear();
    }

    Cache<String, DmnModelInstance> dmnModelInstanceCache = getDmnDefinitionCache();
    if (!dmnModelInstanceCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.DMN_MODEL_INST_CACHE, dmnModelInstanceCache.keySet());
      dmnModelInstanceCache.clear();
    }

    Cache<String, DecisionRequirementsDefinitionEntity> decisionRequirementsDefinitionCache = getDecisionRequirementsDefinitionCache();
    if (!decisionRequirementsDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.DMN_REQ_DEF_CACHE, decisionRequirementsDefinitionCache.keySet());
      decisionRequirementsDefinitionCache.clear();
    }

    return result;
  }

}
