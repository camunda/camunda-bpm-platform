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

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


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

    // trigger all event subscriptions for the signal (start and intermediate)
    List<EventSubscriptionEntity> signalEventSubscriptions = findSignalEventSubscriptions(signalDefinition.getEventName(), execution.getTenantId());

    for (EventSubscriptionEntity signalEventSubscription : signalEventSubscriptions) {
      if(isActiveEventSubscription(signalEventSubscription)){
        signalEventSubscription.eventReceived(null, signalDefinition.isAsync());
      }
    }

    leave(execution);
  }

  protected List<EventSubscriptionEntity> findSignalEventSubscriptions(String signalName, String tenantId) {
    EventSubscriptionManager eventSubscriptionManager = Context.getCommandContext().getEventSubscriptionManager();

    if(tenantId != null) {
      return eventSubscriptionManager
          .findSignalEventSubscriptionsByEventNameAndTenantIdIncludeWithoutTenantId(signalName, tenantId);

    } else {
      // find event subscriptions without tenant id
      return eventSubscriptionManager.findSignalEventSubscriptionsByEventNameAndTenantId(signalName, null);
    }
  }

  protected boolean isActiveEventSubscription(EventSubscriptionEntity signalEventSubscriptionEntity) {
    return isStartEventSubscription(signalEventSubscriptionEntity)
        || isActiveIntermediateEventSubscription(signalEventSubscriptionEntity);
  }

  protected boolean isStartEventSubscription(EventSubscriptionEntity signalEventSubscriptionEntity) {
    return signalEventSubscriptionEntity.getExecutionId() == null;
  }

  protected boolean isActiveIntermediateEventSubscription(EventSubscriptionEntity signalEventSubscriptionEntity) {
    ExecutionEntity execution = signalEventSubscriptionEntity.getExecution();
    return execution != null && !execution.isEnded() && !execution.isCanceled();
  }

}
