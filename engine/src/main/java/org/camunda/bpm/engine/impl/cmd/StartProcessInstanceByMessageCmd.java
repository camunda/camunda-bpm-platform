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

import java.util.Map;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Daniel Meyer
 */
public class StartProcessInstanceByMessageCmd implements Command<ProcessInstance> {

  protected final String messageName;
  protected final String businessKey;
  protected final Map<String, Object> processVariables;

  public StartProcessInstanceByMessageCmd(String messageName, String businessKey, Map<String, Object> processVariables) {
    this.messageName = messageName;
    this.businessKey = businessKey;
    this.processVariables = processVariables;    
  }

  public ProcessInstance execute(CommandContext commandContext) {

    ensureNotNull("Cannot start process instance by message", "message name", messageName);

    MessageEventSubscriptionEntity messageEventSubscription = commandContext.getEventSubscriptionManager()
      .findMessageStartEventSubscriptionByName(messageName);

    ensureNotNull("Cannot start process instance by message: no subscription to message with name '" + messageName + "' found", "messageEventSubscription", messageEventSubscription);

    String processDefinitionId = messageEventSubscription.getConfiguration();
    ensureNotNull("Cannot start process instance by message: subscription to message with name '" + messageName + "' is not a message start event", "processDefinitionId", processDefinitionId);

    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();

    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    ensureNotNull("No process definition found for id '" + processDefinitionId + "'", "processDefinition", processDefinition);

    ActivityImpl startActivity = processDefinition.findActivity(messageEventSubscription.getActivityId());
    ExecutionEntity processInstance = processDefinition.createProcessInstance(businessKey, startActivity);

    if (processVariables != null) {
      processInstance.setVariables(processVariables);
    }

    processInstance.start();

    return processInstance;
  }

}
