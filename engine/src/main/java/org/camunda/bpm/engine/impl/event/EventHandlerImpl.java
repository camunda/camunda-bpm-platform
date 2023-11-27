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

import java.util.Map;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ActivityNewScopeVariablesTuple;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Christopher Zell
 */
public class EventHandlerImpl implements EventHandler {

  private final EventType eventType;

  public EventHandlerImpl(EventType eventType) {
    this.eventType = eventType;
  }

  public void handleIntermediateEvent(EventSubscriptionEntity eventSubscription,
                                      Object payload,
                                      Object localPayload,
                                      Object payloadToTriggeredScope,
                                      CommandContext commandContext) {

    PvmExecutionImpl execution = eventSubscription.getExecution();
    ActivityImpl activity = eventSubscription.getActivity();

    ensureNotNull("Error while sending signal for event subscription '" + eventSubscription.getId() + "': "
      + "no activity associated with event subscription", "activity", activity);

    if (payload instanceof Map) {
      execution.setVariables((Map<String, Object>)payload);
    }

    if (localPayload instanceof Map) {
      execution.setVariablesLocal((Map<String, Object>) localPayload);
    }

    if (payloadToTriggeredScope instanceof Map) {
      if (ActivityTypes.INTERMEDIATE_EVENT_MESSAGE.equals(activity.getProperty(BpmnProperties.TYPE.getName()))) {
        execution.setVariablesLocal((Map<String, Object>) payloadToTriggeredScope);
      } else {
        ActivityNewScopeVariablesTuple tuple = new ActivityNewScopeVariablesTuple(activity.getId(),
                                                                                  (Map<String, Object>) payloadToTriggeredScope);
        execution.getProcessInstance().setPayloadForTriggeredScope(tuple);
      }

    }

    if(activity.equals(execution.getActivity())) {
      execution.signal("signal", null);
    }
    else {
      // hack around the fact that the start event is referenced by event subscriptions for event subprocesses
      // and not the subprocess itself
      if (activity.getActivityBehavior() instanceof EventSubProcessStartEventActivityBehavior) {
        activity = (ActivityImpl) activity.getFlowScope();
      }

      execution.executeEventHandlerActivity(activity);
    }
  }

  @Override
  public void handleEvent(EventSubscriptionEntity eventSubscription,
                          Object payload,
                          Object localPayload,
                          Object payloadToTriggeredScope,
                          String businessKey,
                          CommandContext commandContext) {
    handleIntermediateEvent(eventSubscription, payload, localPayload, payloadToTriggeredScope, commandContext);
  }

  @Override
  public String getEventHandlerType() {
    return eventType.name();
  }
}
