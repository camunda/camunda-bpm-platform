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

import org.camunda.bpm.engine.impl.bpmn.parser.ConditionalEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Daniel Meyer
 */
public class EventBasedGatewayActivityBehavior extends FlowNodeActivityBehavior {

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    // If conditional events exist after the event based gateway they should be evaluated.
    // If a condition is satisfied the event based gateway should be left,
    // otherwise the event based gateway is a wait state
    ActivityImpl eventBasedGateway = (ActivityImpl) execution.getActivity();
    for (ActivityImpl act : eventBasedGateway.getEventActivities()) {
      ActivityBehavior activityBehavior = act.getActivityBehavior();
      if (activityBehavior instanceof ConditionalEventBehavior) {
        ConditionalEventBehavior conditionalEventBehavior = (ConditionalEventBehavior) activityBehavior;
        ConditionalEventDefinition conditionalEventDefinition = conditionalEventBehavior.getConditionalEventDefinition();
        if (conditionalEventDefinition.tryEvaluate(execution)) {
          ((ExecutionEntity) execution).executeEventHandlerActivity(conditionalEventDefinition.getConditionalActivity());
          return;
        }
      }
    }
  }
}
