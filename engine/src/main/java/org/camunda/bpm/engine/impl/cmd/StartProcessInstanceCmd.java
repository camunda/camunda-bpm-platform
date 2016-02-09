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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class StartProcessInstanceCmd implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final ProcessInstantiationBuilderImpl processInstantiationBuilder;

  // TODO move tenant-id to builder - CAM-5211
  protected String tenantId;

  public StartProcessInstanceCmd(ProcessInstantiationBuilderImpl processInstantiationBuilder, String tenantId) {
    this.processInstantiationBuilder = processInstantiationBuilder;
    this.tenantId = tenantId;
  }

  public ProcessInstance execute(CommandContext commandContext) {

    ProcessDefinitionEntity processDefinition = findProcessDefinition();

    // check authorization
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCreateProcessInstance(processDefinition);

    // Start the process instance
    ExecutionEntity processInstance = processDefinition.createProcessInstance(processInstantiationBuilder.getBusinessKey(),
        processInstantiationBuilder.getCaseInstanceId());
    processInstance.start(processInstantiationBuilder.getVariables());
    return processInstance;
  }

  protected ProcessDefinitionEntity findProcessDefinition() {
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();

    ProcessDefinitionEntity processDefinition = null;

    String processDefinitionId = processInstantiationBuilder.getProcessDefinitionId();
    String processDefinitionKey = processInstantiationBuilder.getProcessDefinitionKey();
    ensureOnlyOneNotNull("either process definition id or key must be set", processDefinitionId, processDefinitionKey);

    if (processDefinitionId != null) {
      processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      ensureNotNull("No process definition found for id = '" + processDefinitionId + "'", "processDefinition", processDefinition);

    } else {
      // TODO allow to start a process instance by key from any tenant if only
      // one tenant has a definition with this key - CAM-5211
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
      ensureNotNull("No process definition found for key '" + processDefinitionKey + "' and tenant-id '" + tenantId + "'", "processDefinition",
          processDefinition);
    }

    return processDefinition;
  }
}
