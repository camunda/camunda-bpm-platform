/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.event;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.bpmn.behavior.ConditionalEventBehavior;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ConditionalEventHandler implements EventHandler {

  @Override
  public String getEventHandlerType() {
    return EventType.CONDITONAL.name();
  }

  @Override
  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, Object localPayload, String businessKey, CommandContext commandContext) {
    VariableEvent variableEvent;
    if (payload == null || payload instanceof VariableEvent) {
      variableEvent = (VariableEvent) payload;
    } else {
      throw new ProcessEngineException("Payload have to be " + VariableEvent.class.getName() + ", to evaluate condition.");
    }

    ActivityImpl activity = eventSubscription.getActivity();
    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    if (activityBehavior instanceof ConditionalEventBehavior) {
      ConditionalEventBehavior conditionalBehavior = (ConditionalEventBehavior) activityBehavior;
      conditionalBehavior.leaveOnSatisfiedCondition(eventSubscription, variableEvent);
    } else {
      throw new ProcessEngineException("Conditional Event has not correct behavior: " + activityBehavior);
    }
  }

}
