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

package org.camunda.bpm.engine.impl.event;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;



/**
 * @author Daniel Meyer
 */
public class SignalEventHandler extends AbstractEventHandler {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final String EVENT_HANDLER_TYPE = "signal";

  public String getEventHandlerType() {
    return EVENT_HANDLER_TYPE;
  }

  protected void handleStartEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
    String processDefinitionId = eventSubscription.getConfiguration();
    ensureNotNull("Configuration of signal start event subscription '" + eventSubscription.getId() + "' contains no process definition id.",
        processDefinitionId);

    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null || processDefinition.isSuspended()) {
      // ignore event subscription
      LOG.debugIgnoringEventSubscription(eventSubscription, processDefinitionId);
    } else {

      ActivityImpl signalStartEvent = processDefinition.findActivity(eventSubscription.getActivityId());
      PvmProcessInstance processInstance = processDefinition.createProcessInstanceForInitial(signalStartEvent);
      processInstance.start();
    }
  }

  @Override
  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
    if (eventSubscription.getExecutionId() != null) {
      handleIntermediateEvent(eventSubscription, payload, commandContext);
    }
    else {
      handleStartEvent(eventSubscription, payload, commandContext);
    }
  }

}
