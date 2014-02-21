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

package org.camunda.bpm.engine.impl;

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;


/**
 * @author Daniel Meyer
 */
public class EventSubscriptionQueryImpl
                extends AbstractQuery<EventSubscriptionQuery, EventSubscription>
                implements Serializable, EventSubscriptionQuery {

  private static final long serialVersionUID = 1L;

  protected String eventSubscriptionId;
  protected String eventName;
  protected String eventType;
  protected String executionId;
  protected String processInstanceId;
  protected String activityId;

  public EventSubscriptionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public EventSubscriptionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public EventSubscriptionQuery eventSubscriptionId(String id) {
    assertParamNotNull("event subscription id", id);
    this.eventSubscriptionId = id;
    return this;
  }

  public EventSubscriptionQuery eventName(String eventName) {
    assertParamNotNull("event name", eventName);
    this.eventName = eventName;
    return this;
  }

  public EventSubscriptionQueryImpl executionId(String executionId) {
    assertParamNotNull("execution id", executionId);
    this.executionId = executionId;
    return this;
  }

  public EventSubscriptionQuery processInstanceId(String processInstanceId) {
    assertParamNotNull("process instance id", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public EventSubscriptionQueryImpl activityId(String activityId) {
    assertParamNotNull("activity id", activityId);
    this.activityId = activityId;
    return this;
  }

  public EventSubscriptionQueryImpl eventType(String eventType) {
    assertParamNotNull("event type", eventType);
    this.eventType = eventType;
    return this;
  }

  public EventSubscriptionQuery orderByCreated() {
    return orderBy(EventSubscriptionQueryProperty.CREATED);
  }

  //results //////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionManager()
      .findEventSubscriptionCountByQueryCriteria(this);
  }

  public List<EventSubscription> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionManager()
      .findEventSubscriptionsByQueryCriteria(this,page);
  }

  //getters //////////////////////////////////////////


  public String getEventSubscriptionId() {
    return eventSubscriptionId;
  }
  public String getEventName() {
    return eventName;
  }
  public String getEventType() {
    return eventType;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getActivityId() {
    return activityId;
  }

}
