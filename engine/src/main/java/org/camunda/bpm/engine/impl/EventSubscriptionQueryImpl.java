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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

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

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected boolean includeEventSubscriptionsWithoutTenantId = false;

  public EventSubscriptionQueryImpl() {
  }

  public EventSubscriptionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public EventSubscriptionQuery eventSubscriptionId(String id) {
    ensureNotNull("event subscription id", id);
    this.eventSubscriptionId = id;
    return this;
  }

  public EventSubscriptionQuery eventName(String eventName) {
    ensureNotNull("event name", eventName);
    this.eventName = eventName;
    return this;
  }

  public EventSubscriptionQueryImpl executionId(String executionId) {
    ensureNotNull("execution id", executionId);
    this.executionId = executionId;
    return this;
  }

  public EventSubscriptionQuery processInstanceId(String processInstanceId) {
    ensureNotNull("process instance id", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public EventSubscriptionQueryImpl activityId(String activityId) {
    ensureNotNull("activity id", activityId);
    this.activityId = activityId;
    return this;
  }

  public EventSubscriptionQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public EventSubscriptionQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public EventSubscriptionQuery includeEventSubscriptionsWithoutTenantId() {
    this.includeEventSubscriptionsWithoutTenantId  = true;
    return this;
  }

  public EventSubscriptionQueryImpl eventType(String eventType) {
    ensureNotNull("event type", eventType);
    this.eventType = eventType;
    return this;
  }

  public EventSubscriptionQuery orderByCreated() {
    return orderBy(EventSubscriptionQueryProperty.CREATED);
  }

  public EventSubscriptionQuery orderByTenantId() {
    return orderBy(EventSubscriptionQueryProperty.TENANT_ID);
  }

  //results //////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionManager()
      .findEventSubscriptionCountByQueryCriteria(this);
  }

  @Override
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
