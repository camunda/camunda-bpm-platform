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

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

import java.util.List;


/**
 * @author Kristin Polenz
 */
public class SignalEndEventActivityBehavior extends FlowNodeActivityBehavior {

  protected EventSubscriptionDeclaration signalDefinition;

  public SignalEndEventActivityBehavior(EventSubscriptionDeclaration signalDefinition) {
    this.signalDefinition = signalDefinition;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {

    String eventName = resolveExpressionOfEventName(signalDefinition.getEventNameAsExpression(), execution);
    List<EventSubscriptionEntity> signalEventSubscriptions =
        findSignalEventSubscriptions(eventName, execution.getTenantId());

    for (EventSubscriptionEntity signalEventSubscription : signalEventSubscriptions) {
      signalEventSubscription.eventReceived(null, signalDefinition.isAsync());
    }

    leave(execution);
  }

  protected String resolveExpressionOfEventName(Expression eventNameAsExpression, ActivityExecution execution) {
    if (isExpressionAvailable(eventNameAsExpression)) {
      return (String) eventNameAsExpression.getValue(execution);
    } else {
      return null;
    }
  }

  protected boolean isExpressionAvailable(Expression expression) {
    return expression != null;
  }

  protected List<EventSubscriptionEntity> findSignalEventSubscriptions(String signalName, String tenantId) {
    EventSubscriptionManager eventSubscriptionManager = Context.getCommandContext().getEventSubscriptionManager();

    if (tenantId != null) {
      return eventSubscriptionManager
          .findSignalEventSubscriptionsByEventNameAndTenantIdIncludeWithoutTenantId(signalName, tenantId);

    } else {
      // find event subscriptions without tenant id
      return eventSubscriptionManager.findSignalEventSubscriptionsByEventNameAndTenantId(signalName, null);
    }
  }

  public EventSubscriptionDeclaration getSignalDefinition() {
    return signalDefinition;
  }

  public void setSignalDefinition(EventSubscriptionDeclaration signalDefinition) {
    this.signalDefinition = signalDefinition;
  }
}
