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

import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Behavior for a compensation end event.
 * 
 * @see IntermediateThrowCompensationEventActivityBehavior
 * 
 * @author Philipp Ossler
 *
 */
public class CompensationEndEventActivityBehavior extends FlowNodeActivityBehavior {

  protected final CompensateEventDefinition compensateEventDefinition;

  public CompensationEndEventActivityBehavior(CompensateEventDefinition compensateEventDefinition) {
    this.compensateEventDefinition = compensateEventDefinition;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {

    final List<CompensateEventSubscriptionEntity> eventSubscriptions = collectEventSubscriptions(execution);
    if (eventSubscriptions.isEmpty()) {
      leave(execution);
    } else {
      // async (waitForCompletion=false in bpmn) is not supported
      CompensationUtil.throwCompensationEvent(eventSubscriptions, execution, false);
    }
  }

  protected List<CompensateEventSubscriptionEntity> collectEventSubscriptions(ActivityExecution execution) {
    final String activityRef = compensateEventDefinition.getActivityRef();
    if (activityRef != null) {
      return CompensationUtil.collectCompensateEventSubscriptionsForActivity(execution, activityRef);
    } else {
      return CompensationUtil.collectCompensateEventSubscriptionsForScope(execution);
    }
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    // join compensating executions
    if (execution.getExecutions().isEmpty()) {
      leave(execution);
    } else {
      ((ExecutionEntity) execution).forceUpdate();
    }
  }

}