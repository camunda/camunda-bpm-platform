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
package org.camunda.bpm.engine.impl.util;

import static org.camunda.bpm.engine.impl.ProcessEngineLogger.UTIL_LOGGER;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.el.StartProcessVariableScope;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

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

  public static ProcessDefinitionImpl getProcessDefinitionToCall(VariableScope execution,
      String defaultTenantId, BaseCallableElement callableElement) {
    String processDefinitionKey = callableElement.getDefinitionKey(execution);
    String tenantId = callableElement.getDefinitionTenantId(execution, defaultTenantId);

    return getCalledProcessDefinition(execution, callableElement, processDefinitionKey, tenantId);
  }

  public static ProcessDefinition getStaticallyBoundProcessDefinition(
      String callingProcessDefinitionId,
      String activityId,
      BaseCallableElement callableElement,
      String tenantId) {
    if(callableElement.hasDynamicReferences()){
      return null;
    }

    VariableScope emptyVariableScope = StartProcessVariableScope.getSharedInstance();

    String targetTenantId = callableElement.getDefinitionTenantId(emptyVariableScope, tenantId);

    try {
      String processDefinitionKey = callableElement.getDefinitionKey(emptyVariableScope);
      return getCalledProcessDefinition(emptyVariableScope, callableElement, processDefinitionKey, targetTenantId);
    } catch (ProcessEngineException e) {
      UTIL_LOGGER.debugCouldNotResolveCallableElement(callingProcessDefinitionId, activityId, e);
      return null;
    }
  }

  private static ProcessDefinitionEntity getCalledProcessDefinition(
    VariableScope execution,
    BaseCallableElement callableElement,
    String processDefinitionKey,
    String tenantId) {

    DeploymentCache deploymentCache = getDeploymentCache();

    ProcessDefinitionEntity processDefinition = null;

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

  public static CmmnCaseDefinition getCaseDefinitionToCall(VariableScope execution, String defaultTenantId, BaseCallableElement callableElement) {
    String caseDefinitionKey = callableElement.getDefinitionKey(execution);
    String tenantId = callableElement.getDefinitionTenantId(execution, defaultTenantId);

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

  public static DecisionDefinition getDecisionDefinitionToCall(VariableScope execution, String defaultTenantId, BaseCallableElement callableElement) {
    String decisionDefinitionKey = callableElement.getDefinitionKey(execution);
    String tenantId = callableElement.getDefinitionTenantId(execution, defaultTenantId);

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
