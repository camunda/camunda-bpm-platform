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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

public class GetDeployedProcessDefinitionCmd implements Command<ProcessDefinitionEntity> {

  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected String processDefinitionTenantId;
  protected boolean isTenantIdSet = false;

  protected final boolean checkReadPermission;

  public GetDeployedProcessDefinitionCmd(String processDefinitionId, boolean checkReadPermission) {
    this.processDefinitionId = processDefinitionId;
    this.checkReadPermission = checkReadPermission;
  }

  public GetDeployedProcessDefinitionCmd(ProcessInstantiationBuilderImpl instantiationBuilder, boolean checkReadPermission) {
    this.processDefinitionId = instantiationBuilder.getProcessDefinitionId();
    this.processDefinitionKey = instantiationBuilder.getProcessDefinitionKey();
    this.processDefinitionTenantId = instantiationBuilder.getProcessDefinitionTenantId();
    this.isTenantIdSet = instantiationBuilder.isProcessDefinitionTenantIdSet();
    this.checkReadPermission = checkReadPermission;
  }

  @Override
  public ProcessDefinitionEntity execute(CommandContext commandContext) {

    ensureOnlyOneNotNull("either process definition id or key must be set", processDefinitionId, processDefinitionKey);

    ProcessDefinitionEntity processDefinition = find(commandContext);

    if (checkReadPermission) {
      for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkReadProcessDefinition(processDefinition);
      }
    }

    return processDefinition;
  }

  protected ProcessDefinitionEntity find(CommandContext commandContext) {
    DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();

    if (processDefinitionId != null) {
      return findById(deploymentCache, processDefinitionId);

    } else {
      return findByKey(deploymentCache, processDefinitionKey);
    }
  }

  protected ProcessDefinitionEntity findById(DeploymentCache deploymentCache, String processDefinitionId) {
    return deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
  }

  protected ProcessDefinitionEntity findByKey(DeploymentCache deploymentCache, String processDefinitionKey) {
    if (isTenantIdSet) {
      return deploymentCache.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, processDefinitionTenantId);

    } else {
      return deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
    }
  }

}
