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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmd.ActivateProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskActivityBehavior extends ProcessOrCaseTaskActivityBehavior {

  protected void triggerCallableElement(CmmnActivityExecution execution, Map<String, Object> variables, String businessKey) {
    String processDefinitionKey = getDefinitionKey(execution);

    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();

    ProcessDefinitionImpl processDefinition = null;

    if (isLatestBinding()) {
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);

    } else if (isDeploymentBinding()) {
      String deploymentId = getDeploymentId(execution);
      processDefinition = deploymentCache.findDeployedProcessDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);

    } else if (isVersionBinding()) {
      Integer version = getVersion(execution);

      processDefinition = deploymentCache.findDeployedProcessDefinitionByKeyAndVersion(processDefinitionKey, version);
    }

    PvmProcessInstance caseInstance = execution.createSubProcessInstance(processDefinition);
    caseInstance.start(businessKey, variables);
  }

  protected void manualCompleting(CmmnActivityExecution execution) {
    ExecutionEntity subProcessInstance = getSubProcessInstance(execution);

    if (subProcessInstance != null) {
      throw new ProcessEngineException("It is not possible to complete a process task manually, because the called process instance is still active.");
    }
  }

  protected void terminating(CmmnActivityExecution execution) {
    ExecutionEntity subProcessInstance = getSubProcessInstance(execution);

    if (subProcessInstance != null) {
      Context
        .getCommandContext()
        .getExecutionManager()
        .deleteProcessInstance(subProcessInstance.getId(), "terminated");
    }
  }

  protected void suspending(CmmnActivityExecution execution) {
    ExecutionEntity subProcessInstance = getSubProcessInstance(execution);

    CommandContext commandContext = Context.getCommandContext();

    new SuspendProcessInstanceCmd(subProcessInstance.getId(), null, null).execute(commandContext);

  }

  protected void resuming(CmmnActivityExecution execution) {
    ExecutionEntity subProcessInstance = getSubProcessInstance(execution);

    CommandContext commandContext = Context.getCommandContext();

    new ActivateProcessInstanceCmd(subProcessInstance.getId(), null, null).execute(commandContext);

  }

  protected ExecutionEntity getSubProcessInstance(CmmnActivityExecution execution) {
    String id = execution.getId();
    ExecutionEntity subProcessInstance = Context
        .getCommandContext()
        .getExecutionManager()
        .findSubProcessInstanceBySuperCaseExecutionId(id);

    return subProcessInstance;
  }

}
