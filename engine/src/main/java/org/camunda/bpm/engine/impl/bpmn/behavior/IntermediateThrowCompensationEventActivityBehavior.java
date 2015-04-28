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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.Collector;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;


/**
 * @author Daniel Meyer
 */
public class IntermediateThrowCompensationEventActivityBehavior extends FlowNodeActivityBehavior {

  protected final CompensateEventDefinition compensateEventDefinition;

  public IntermediateThrowCompensationEventActivityBehavior(CompensateEventDefinition compensateEventDefinition) {
    this.compensateEventDefinition = compensateEventDefinition;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    final String activityRef = compensateEventDefinition.getActivityRef();

    ExecutionEntity scopeExecution = (ExecutionEntity) (execution.isScope() ? execution : execution.getParent());

    List<CompensateEventSubscriptionEntity> eventSubscriptions = collectCompensateEventSubScriptions(execution);

    if(activityRef != null) {
      ActivityImpl activityToCompensate = scopeExecution.getProcessDefinition().findActivity(activityRef);
      String compensationHandlerId  = (String) activityToCompensate.getProperty(BpmnParse.PROPERTYNAME_COMPENSATION_HANDLER_ID);
      String subscriptionActivityId = null;
      if(compensationHandlerId != null) {
        subscriptionActivityId = compensationHandlerId;
      }
      else {
        // HACK <!> backwards compatibility (?)
        subscriptionActivityId = activityRef;
      }

      List<CompensateEventSubscriptionEntity> eventSubscriptionsForActivity = new ArrayList<CompensateEventSubscriptionEntity>();
      for (CompensateEventSubscriptionEntity subscription : eventSubscriptions) {
        if (subscriptionActivityId.equals(subscription.getActivityId())) {
          eventSubscriptionsForActivity.add(subscription);
        }
      }

      eventSubscriptions = eventSubscriptionsForActivity;
    }

    if(eventSubscriptions.isEmpty()) {
      leave(execution);
    } else {
      // TODO: implement async (waitForCompletion=false in bpmn)
      CompensationUtil.throwCompensationEvent(eventSubscriptions, execution, false);
    }

  }

  protected List<CompensateEventSubscriptionEntity> collectCompensateEventSubScriptions(final ActivityExecution execution) {
    final Map<ScopeImpl, PvmExecutionImpl> scopeExecutionMapping = execution.createActivityExecutionMapping();
    ScopeImpl activity = (ScopeImpl) execution.getActivity();

    // <LEGACY>: different flow scopes may have the same scope execution => collect subscriptions in a set
    final Set<CompensateEventSubscriptionEntity> subscriptions = new HashSet<CompensateEventSubscriptionEntity>();
    Collector<ScopeImpl> eventSubscriptionCollector = new Collector<ScopeImpl>() {
      public void collect(ScopeImpl obj) {
        PvmExecutionImpl execution = scopeExecutionMapping.get(obj);
        subscriptions.addAll(((ExecutionEntity) execution).getCompensateEventSubscriptions());
      }
    };

    new FlowScopeWalker(activity)
      .addPostCollector(eventSubscriptionCollector)
      .walkUntil(new WalkCondition<ScopeImpl>() {
        public boolean isFulfilled(ScopeImpl element) {
          Boolean consumesCompensationProperty = (Boolean) element.getProperty(BpmnParse.PROPERTYNAME_CONSUMES_COMPENSATION);
          return consumesCompensationProperty == null || consumesCompensationProperty == Boolean.TRUE;
        }
      });

    return new ArrayList<CompensateEventSubscriptionEntity>(subscriptions);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {

    // join compensating executions
    if(execution.getExecutions().isEmpty()) {
      leave(execution);
    } else {
      ((ExecutionEntity)execution).forceUpdate();
    }

  }


}
