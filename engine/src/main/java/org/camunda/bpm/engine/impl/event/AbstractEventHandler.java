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

import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Daniel Meyer
 * @author Falko Menge
 */
public abstract class AbstractEventHandler implements EventHandler {

  private static Logger log = Logger.getLogger(AbstractEventHandler.class.getName());

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {

    ExecutionEntity execution = eventSubscription.getExecution();
    ActivityImpl activity = eventSubscription.getActivity();

    if (activity == null) {
      throw new ProcessEngineException("Error while sending signal for event subscription '" + eventSubscription.getId() + "': "
              + "no activity associated with event subscription");
    }

    if (payload instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> processVariables = (Map<String, Object>) payload;
      execution.setVariables(processVariables);
    }

    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    if (activityBehavior instanceof BoundaryEventActivityBehavior) {

      try {
        execution.executeActivity(activity);

      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new ProcessEngineException("exception while sending signal for event subscription '" + eventSubscription + "':" + e.getMessage(), e);
      }

    } else if (activityBehavior instanceof EventSubProcessStartEventActivityBehavior) {

      try {
        execution.executeActivity(activity.getParentActivity());

      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new ProcessEngineException("exception while sending signal for event subscription '" + eventSubscription + "':" + e.getMessage(), e);
      }

    } else { // not boundary
      if (activityBehavior instanceof IntermediateCatchEventActivityBehavior) {
        IntermediateCatchEventActivityBehavior catchBehavior = (IntermediateCatchEventActivityBehavior) activityBehavior;

        if (catchBehavior.isAfterEventBasedGateway()) {
          execution.executeActivity(activity);
          return;
        }
      }

      if (!activity.equals( execution.getActivity() )) {
        execution.setActivity(activity);
      }
      execution.signal("signal", null);
    }

  }
}
