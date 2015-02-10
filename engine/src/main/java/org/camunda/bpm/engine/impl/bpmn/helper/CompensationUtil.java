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

package org.camunda.bpm.engine.impl.bpmn.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Daniel Meyer
 */
public class CompensationUtil {

  /**
   * we create a separate execution for each compensation handler invocation.
   */
  public static void throwCompensationEvent(List<CompensateEventSubscriptionEntity> eventSubscriptions, ActivityExecution execution, boolean async) {

    // first spawn the compensating executions
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      ExecutionEntity compensatingExecution = null;
      // check whether compensating execution is already created
      // (which is the case when compensating an embedded subprocess,
      // where the compensating execution is created when leaving the subprocess
      // and holds snapshot data).
      if(eventSubscription.getConfiguration() !=null) {
        compensatingExecution = Context.getCommandContext()
          .getExecutionManager()
          .findExecutionById(eventSubscription.getConfiguration());
        // move the compensating execution under this execution:
        compensatingExecution.setParent((PvmExecutionImpl) execution);
        compensatingExecution.setEventScope(false);
      } else {
        compensatingExecution = (ExecutionEntity) execution.createExecution();
        eventSubscription.setConfiguration(compensatingExecution.getId());
      }
      compensatingExecution.setConcurrent(true);
    }

    // signal compensation events in reverse order of their 'created' timestamp
    Collections.sort(eventSubscriptions, new Comparator<EventSubscriptionEntity>() {
      public int compare(EventSubscriptionEntity o1, EventSubscriptionEntity o2) {
        return o2.getCreated().compareTo(o1.getCreated());
      }
    });

    for (CompensateEventSubscriptionEntity compensateEventSubscriptionEntity : eventSubscriptions) {
      compensateEventSubscriptionEntity.eventReceived(null, async);
    }
  }

  /**
   * creates an event scope for the given execution:
   *
   * create a new event scope execution under the parent of the given
   * execution and move all event subscriptions to that execution.
   *
   * this allows us to "remember" the event subscriptions after finishing a
   * scope
   */
  public static void createEventScopeExecution(ExecutionEntity execution) {

    PvmActivity activity = execution.getActivity();
    PvmScope levelOfSubprocess = activity.getLevelOfSubprocessScope();

    ExecutionEntity levelOfSubprocessScopeExecution = (ExecutionEntity) execution.findExecutionForFlowScope(levelOfSubprocess);

    List<CompensateEventSubscriptionEntity> eventSubscriptions = execution.getCompensateEventSubscriptions();

    if(eventSubscriptions.size() > 0) {

      ExecutionEntity eventScopeExecution = levelOfSubprocessScopeExecution.createExecution();
      eventScopeExecution.setActivity(execution.getActivity());
      eventScopeExecution.enterActivityInstance();
      eventScopeExecution.setActive(false);
      eventScopeExecution.setConcurrent(false);
      eventScopeExecution.setEventScope(true);

      // copy local variables to eventScopeExecution by value. This way,
      // the eventScopeExecution references a 'snapshot' of the local variables
      Map<String, Object> variables = execution.getVariablesLocal();
      for (Entry<String, Object> variable : variables.entrySet()) {
        eventScopeExecution.setVariableLocal(variable.getKey(), variable.getValue());
      }

      // set event subscriptions to the event scope execution:
      for (CompensateEventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
        eventSubscriptionEntity = eventSubscriptionEntity.moveUnder(eventScopeExecution);
      }

      CompensateEventSubscriptionEntity eventSubscription = CompensateEventSubscriptionEntity.createAndInsert(levelOfSubprocessScopeExecution);
      eventSubscription.setActivity(execution.getActivity());
      eventSubscription.setConfiguration(eventScopeExecution.getId());

    }
  }

}
