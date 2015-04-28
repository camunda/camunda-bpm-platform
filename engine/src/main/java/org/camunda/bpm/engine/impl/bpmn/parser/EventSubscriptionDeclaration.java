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

package org.camunda.bpm.engine.impl.bpmn.parser;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.event.SignalEventHandler;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;


/**
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Danny Gräf
 */
public class EventSubscriptionDeclaration implements Serializable {

  private static final long serialVersionUID = 1L;

  protected final String eventName;
  protected final String eventType;

  protected boolean async;
  protected String activityId = null;
  protected String eventScopeActivityId = null;
  protected boolean isStartEvent;
  protected Boolean isParallelMultiInstance = null;

  public EventSubscriptionDeclaration(String eventName, String eventType) {
    this.eventName = eventName;
    this.eventType = eventType;
  }

  public String getEventName() {
    return eventName;
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setEventScopeActivityId(String eventScopeActivityId) {
    this.eventScopeActivityId = eventScopeActivityId;
  }

  public String getEventScopeActivityId() {
    return eventScopeActivityId;
  }

  public boolean isStartEvent() {
    return isStartEvent;
  }

  public void setStartEvent(boolean isStartEvent) {
    this.isStartEvent = isStartEvent;
  }

  public String getEventType() {
    return eventType;
  }

  public EventSubscriptionEntity createSubscription(ExecutionEntity execution) {
    if (isStartEvent()) {
      return null;
    } else {
      return createEventSubscription(execution);
    }
  }

  /**
   * Creates and inserts a subscription entity depending on the message type of this declaration.
   * @param execution
   * @return subscription entity
   */
  private EventSubscriptionEntity createEventSubscription(ExecutionEntity execution) {
    EventSubscriptionEntity eventSubscriptionEntity = null;
    if (eventType.equals(MessageEventHandler.EVENT_HANDLER_TYPE)) {
      eventSubscriptionEntity = new MessageEventSubscriptionEntity(execution);
    } else if (eventType.equals(SignalEventHandler.EVENT_HANDLER_TYPE)) {
      eventSubscriptionEntity = new SignalEventSubscriptionEntity(execution);
    } else {
      throw new ProcessEngineException("Found event definition of unknown type: " + eventType);
    }

    eventSubscriptionEntity.setEventName(eventName);
    if (activityId != null) {
      ActivityImpl activity = execution.getProcessDefinition().findActivity(activityId);
      eventSubscriptionEntity.setActivity(activity);
    }

    eventSubscriptionEntity.insert();
    LegacyBehavior.removeLegacySubscriptionOnParent(execution, eventSubscriptionEntity);

    return eventSubscriptionEntity;
  }

  @SuppressWarnings("unchecked")
  public static List<EventSubscriptionDeclaration> getDeclarationsForScope(PvmScope scope) {
    Object result = scope.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if (result != null) {
      return (List<EventSubscriptionDeclaration>) result;
    } else {
      return Collections.emptyList();
    }
  }

}
