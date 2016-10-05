/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.bpmn.parser.ConditionalEventDefinition;
import org.camunda.bpm.engine.impl.event.ConditionalVariableEventPayload;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class IntermediateConditionalEventBehavior extends IntermediateCatchEventActivityBehavior implements ConditionalEventBehavioral {

  private final ConditionalEventDefinition conditionalEvent;

  public IntermediateConditionalEventBehavior(ConditionalEventDefinition conditionalEvent, boolean isAfterEventBasedGateway) {
    super(isAfterEventBasedGateway);
    this.conditionalEvent = conditionalEvent;
  }

  @Override
  public void execute(final ActivityExecution execution) throws Exception {
    super.execute(execution);
    if (conditionalEvent.evaluate(execution, execution)) {
      leave(execution);
    } else if (execution instanceof ExecutionEntity) {
      //add variable life cycle listener to evaluate condition after a variable was changed
      ActivityImpl conditionalActivity = (ActivityImpl) execution.getActivity();
      EventSubscriptionEntity.createAndInsert((ExecutionEntity) execution, EventType.CONDITONAL, conditionalActivity);
    }
  }

  @Override
  public void leaveOnSatisfiedCondition(final EventSubscriptionEntity eventSubscription, final ConditionalVariableEventPayload conditionalVariableEventPayload, final CommandContext commandContext) {

    PvmExecutionImpl execution = eventSubscription.getExecution();
    ActivityImpl activity = eventSubscription.getActivity();
    final VariableScope scope = conditionalVariableEventPayload.getScope();

    ExecutionQueryImpl query = new ExecutionQueryImpl();
    query.activityId(activity.getId());
    query.processInstanceId(execution.getProcessInstanceId());
    Page p = new Page(0, 1);
    List<ExecutionEntity> executions = commandContext.getExecutionManager().findExecutionsByQueryCriteria(query, p);
    if (!executions.isEmpty() && scope != null) {
      execution = executions.get(0);
      if (!execution.isEnded() && conditionalEvent.evaluate(scope, execution)) {
        if (execution.isActive() && execution.isScope()) {
          leave(execution);
        }
      }
    }
  }
}
