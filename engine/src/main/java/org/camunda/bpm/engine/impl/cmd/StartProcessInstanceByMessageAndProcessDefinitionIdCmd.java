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

import java.util.Map;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * Command to start a process instance by message.
 *
 * See {@link StartProcessInstanceByMessageCmd}. In addition, this command allows
 * specifying the exactly version of the process definition with the given id.
 *
 *
 * @author Philipp Ossler
 */
public class StartProcessInstanceByMessageAndProcessDefinitionIdCmd implements Command<ProcessInstance> {

  protected final String messageName;
  protected final String businessKey;
  protected final Map<String, Object> processVariables;
  protected final String processDefinitionId;

  public StartProcessInstanceByMessageAndProcessDefinitionIdCmd(String messageName, String processDefinitionId, String businessKey, Map<String, Object> processVariables) {
    this.messageName = messageName;
    this.businessKey = businessKey;
    this.processVariables = processVariables;
    this.processDefinitionId = processDefinitionId;
  }

  public ProcessInstance execute(CommandContext commandContext) {
    ensureNotNull("Cannot start process instance by message and process definition id", "messageName", messageName);
    ensureNotNull("Cannot start process instance by message and process definition id", "processDefinitionId", processDefinitionId);

    DeploymentCache deploymentCache = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    ensureNotNull("No process definition found for id '" + processDefinitionId + "'", "processDefinition", processDefinition);

    // check authorization
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCreateProcessInstance(processDefinition);

    String activityId = findStartActivityIdByMessage(processDefinition);
    ensureNotNull("Cannot start process instance by message: no message start event with name '" + messageName + "' found for process definition with id '" + processDefinitionId + "'", "activityId", activityId);

    ActivityImpl startActivity = processDefinition.findActivity(activityId);
    ExecutionEntity processInstance = processDefinition.createProcessInstance(businessKey, startActivity);
    processInstance.start(processVariables);
    return processInstance;
  }

  protected String findStartActivityIdByMessage(ProcessDefinitionEntity processDefinition){
	  for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(processDefinition)) {
		  if(isMessageStartEventWithName(declaration, messageName)) {
			  return declaration.getActivityId();
		  }
		}
	  return null;
  }

  protected boolean isMessageStartEventWithName(EventSubscriptionDeclaration declaration, String messageName) {
    return MessageEventHandler.EVENT_HANDLER_TYPE.equals(declaration.getEventType())
			&& declaration.isStartEvent() && messageName.equals(declaration.getEventName());
  }

}
