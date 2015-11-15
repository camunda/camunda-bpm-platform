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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class StartProcessInstanceAtActivitiesCmd implements Command<ProcessInstance> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected ProcessInstantiationBuilderImpl instantiationBuilder;

  public StartProcessInstanceAtActivitiesCmd(ProcessInstantiationBuilderImpl instantiationBuilder) {
    this.instantiationBuilder = instantiationBuilder;
  }

  public ProcessInstance execute(CommandContext commandContext) {
    String processDefinitionId = instantiationBuilder.getProcessDefinitionId();
    String processDefinitionKey = instantiationBuilder.getProcessDefinitionKey();

    DeploymentCache deploymentCache = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache();
    // Find the process definition
    ProcessDefinitionEntity processDefinition = null;
    if (processDefinitionId!=null) {
      processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      ensureNotNull("No process definition found for id = '" + processDefinitionId + "'", "processDefinition", processDefinition);
    } else if(processDefinitionKey != null) {
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      ensureNotNull("No process definition found for key '" + processDefinitionKey + "'", "processDefinition", processDefinition);
    } else {
      throw new ProcessEngineException("processDefinitionKey and processDefinitionId are null");
    }

    // check authorization
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCreateProcessInstance(processDefinition);

    ProcessInstanceModificationBuilderImpl modificationBuilder = instantiationBuilder.getModificationBuilder();
    ensureNotEmpty("At least one instantiation instruction required (e.g. by invoking startBefore(..), startAfter(..) or startTransition(..))",
        "instructions", modificationBuilder.getModificationOperations());

    // instantiate the process
    ActivityImpl initialActivity = determineFirstActivity(processDefinition, modificationBuilder);

    ExecutionEntity processInstance = processDefinition
        .createProcessInstance(instantiationBuilder.getBusinessKey(), instantiationBuilder.getCaseInstanceId(), initialActivity);
    processInstance.setSkipCustomListeners(modificationBuilder.isSkipCustomListeners());
    processInstance.startWithoutExecuting();

    processInstance.setActivity(null);
    processInstance.setActivityInstanceId(processInstance.getId());

    // set variables
    processInstance.setVariables(modificationBuilder.getProcessVariables());

    // prevent ending of the process instance between instructions
    processInstance.setPreserveScope(true);

    // apply modifications
    List<AbstractProcessInstanceModificationCommand> instructions = modificationBuilder.getModificationOperations();

    for (int i = 0; i < instructions.size(); i++) {
      AbstractProcessInstanceModificationCommand instruction = instructions.get(i);
      LOG.debugStartingInstruction(processInstance.getId(), i, instruction.describe());

      instruction.setProcessInstanceId(processInstance.getId());
      instruction.setSkipCustomListeners(modificationBuilder.isSkipCustomListeners());
      instruction.setSkipIoMappings(modificationBuilder.isSkipIoMappings());
      instruction.execute(commandContext);
    }

    if (!processInstance.hasChildren() && processInstance.isEnded()) {
      // process instance has ended regularly but this has not been propagated yet
      // due to preserveScope setting
      processInstance.propagateEnd();
    }

    return processInstance;
  }

  /**
   * get the activity that is started by the first instruction, if exists;
   * return null if the first instruction is a start-transition instruction
   */
  protected ActivityImpl determineFirstActivity(ProcessDefinitionImpl processDefinition,
      ProcessInstanceModificationBuilderImpl modificationBuilder) {
    AbstractProcessInstanceModificationCommand firstInstruction = modificationBuilder.getModificationOperations().get(0);

    if (firstInstruction instanceof AbstractInstantiationCmd) {
      AbstractInstantiationCmd instantiationInstruction = (AbstractInstantiationCmd) firstInstruction;
      CoreModelElement targetElement = instantiationInstruction.getTargetElement(processDefinition);

      ensureNotNull(NotValidException.class,
          "Element '" + instantiationInstruction.getTargetElementId() + "' does not exist in process " + processDefinition.getId(),
          "targetElement",
          targetElement);

      if (targetElement instanceof ActivityImpl) {
        return (ActivityImpl) targetElement;
      }
      else if (targetElement instanceof TransitionImpl) {
        return (ActivityImpl) ((TransitionImpl) targetElement).getDestination();
      }

    }

    return null;
  }

}
