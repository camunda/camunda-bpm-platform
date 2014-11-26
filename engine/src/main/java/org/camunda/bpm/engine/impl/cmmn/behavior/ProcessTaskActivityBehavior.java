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

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
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

    PvmProcessInstance caseInstance = execution.createSubProcessInstance(processDefinition, businessKey);
    caseInstance.start(variables);
  }

  public void onManualCompletion(CmmnActivityExecution execution) {
    // Throw always an exception!
    // It should not be possible to complete a process
    // task manually. If the called process instance has
    // been completed, the associated process task will
    // be notified to complete automatically.
    String id = execution.getId();
    String message = "It is not possible to complete case execution '"+id+"' which associated with a process task manually.";
    throw createIllegalStateTransitionException("complete", message, execution);
  }

  protected String getTypeName() {
    return "process task";
  }

}
