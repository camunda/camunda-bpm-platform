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
package org.camunda.bpm.engine.impl.util;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.repository.DecisionDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class CallableElementUtil {

  public static DeploymentCache getDeploymentCache() {
    return Context
        .getProcessEngineConfiguration()
        .getDeploymentCache();
  }

  public static ProcessDefinitionImpl getProcessDefinitionToCall(VariableScope execution, BaseCallableElement callableElement) {
    String processDefinitionKey = callableElement.getDefinitionKey(execution);
    String tenantId = callableElement.getDefinitionTenantId(execution);

    DeploymentCache deploymentCache = getDeploymentCache();

    ProcessDefinitionImpl processDefinition = null;

    if (callableElement.isLatestBinding()) {
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);

    } else if (callableElement.isDeploymentBinding()) {
      String deploymentId = callableElement.getDeploymentId();
      processDefinition = deploymentCache.findDeployedProcessDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);

    } else if (callableElement.isVersionBinding()) {
      Integer version = callableElement.getVersion(execution);
      processDefinition = deploymentCache.findDeployedProcessDefinitionByKeyVersionAndTenantId(processDefinitionKey, version, tenantId);

    } else if (callableElement.isVersionTagBinding()) {
      String versionTag = callableElement.getVersionTag(execution);
      processDefinition = deploymentCache.findDeployedProcessDefinitionByKeyVersionTagAndTenantId(processDefinitionKey, versionTag, tenantId);

    }

    return processDefinition;
  }

  public static CmmnCaseDefinition getCaseDefinitionToCall(VariableScope execution, BaseCallableElement callableElement) {
    String caseDefinitionKey = callableElement.getDefinitionKey(execution);
    String tenantId = callableElement.getDefinitionTenantId(execution);

    DeploymentCache deploymentCache = getDeploymentCache();

    CmmnCaseDefinition caseDefinition = null;
    if (callableElement.isLatestBinding()) {
      caseDefinition = deploymentCache.findDeployedLatestCaseDefinitionByKeyAndTenantId(caseDefinitionKey, tenantId);

    } else if (callableElement.isDeploymentBinding()) {
      String deploymentId = callableElement.getDeploymentId();
      caseDefinition = deploymentCache.findDeployedCaseDefinitionByDeploymentAndKey(deploymentId, caseDefinitionKey);

    } else if (callableElement.isVersionBinding()) {
      Integer version = callableElement.getVersion(execution);
      caseDefinition = deploymentCache.findDeployedCaseDefinitionByKeyVersionAndTenantId(caseDefinitionKey, version, tenantId);
    }

    return caseDefinition;
  }

  public static DecisionDefinition getDecisionDefinitionToCall(VariableScope execution, BaseCallableElement callableElement) {
    String decisionDefinitionKey = callableElement.getDefinitionKey(execution);
    String tenantId = callableElement.getDefinitionTenantId(execution);

    DeploymentCache deploymentCache = getDeploymentCache();

    DecisionDefinition decisionDefinition = null;
    if (callableElement.isLatestBinding()) {
      decisionDefinition = deploymentCache.findDeployedLatestDecisionDefinitionByKeyAndTenantId(decisionDefinitionKey, tenantId);

    } else if (callableElement.isDeploymentBinding()) {
      String deploymentId = callableElement.getDeploymentId();
      decisionDefinition = deploymentCache.findDeployedDecisionDefinitionByDeploymentAndKey(deploymentId, decisionDefinitionKey);

    } else if (callableElement.isVersionBinding()) {
      Integer version = callableElement.getVersion(execution);
      decisionDefinition = deploymentCache.findDeployedDecisionDefinitionByKeyVersionAndTenantId(decisionDefinitionKey, version, tenantId);

    } else if (callableElement.isVersionTagBinding()) {
      String versionTag = callableElement.getVersionTag(execution);
      decisionDefinition = deploymentCache.findDeployedDecisionDefinitionByKeyVersionTagAndTenantId(decisionDefinitionKey, versionTag, tenantId);
    }

    return decisionDefinition;
  }

}
