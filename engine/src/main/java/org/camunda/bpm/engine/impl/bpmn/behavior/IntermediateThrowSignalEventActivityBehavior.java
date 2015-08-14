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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Daniel Meyer
 */
public class IntermediateThrowSignalEventActivityBehavior extends AbstractBpmnActivityBehavior {

  protected final static BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  protected final EventSubscriptionDeclaration signalDefinition;

  public IntermediateThrowSignalEventActivityBehavior(EventSubscriptionDeclaration signalDefinition) {
    this.signalDefinition = signalDefinition;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    final EventSubscriptionManager eventSubscriptionManager = Context.getCommandContext().getEventSubscriptionManager();

    // trigger all event subscriptions for the signal (start and intermediate)
    List<SignalEventSubscriptionEntity> catchSignalEventSubscription = eventSubscriptionManager
      .findSignalEventSubscriptionsByEventName(signalDefinition.getEventName());
    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : catchSignalEventSubscription) {
      if(isActiveEventSubscription(signalEventSubscriptionEntity)){
        signalEventSubscriptionEntity.eventReceived(null, signalDefinition.isAsync());
      }
    }

    leave(execution);
  }

  protected boolean isActiveEventSubscription(SignalEventSubscriptionEntity signalEventSubscriptionEntity) {
    return isStartEventSubscription(signalEventSubscriptionEntity)
        || isActiveIntermediateEventSubscription(signalEventSubscriptionEntity);
  }

  protected boolean isStartEventSubscription(SignalEventSubscriptionEntity signalEventSubscriptionEntity) {
    return signalEventSubscriptionEntity.getExecutionId() == null;
  }

  protected boolean isActiveIntermediateEventSubscription(SignalEventSubscriptionEntity signalEventSubscriptionEntity) {
    ExecutionEntity execution = signalEventSubscriptionEntity.getExecution();
    return execution != null && !execution.isEnded() && !execution.isCanceled();
  }

  protected void startProcessInstanceBySignal(SignalEventSubscriptionEntity eventSubscription) {
    String processDefinitionId = eventSubscription.getConfiguration();
    ensureNotNull("Configuration of signal start event subscription '" + eventSubscription.getId() + "' contains no process definition id.",
        processDefinitionId);

    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null || processDefinition.isSuspended()) {
      // ignore event subscription
      LOG.ignoringEventSubscription(eventSubscription, processDefinitionId);
    } else {

      ActivityImpl signalStartEvent = processDefinition.findActivity(eventSubscription.getActivityId());
      PvmProcessInstance processInstance = processDefinition.createProcessInstanceForInitial(signalStartEvent);
      processInstance.start();
    }
  }

}
