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
package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.query.Query;

/**
 * Allows querying of event subscriptions.
 *
 * @author Thorben Lindhauer
 */
public interface EventSubscriptionQuery extends Query<EventSubscriptionQuery, EventSubscription>{

  /** Only select subscriptions with the given id. **/
  EventSubscriptionQuery eventSubscriptionId(String id);

  /** Only select subscriptions for events with the given name. **/
  EventSubscriptionQuery eventName(String eventName);

  /** Only select subscriptions for events with the given type. "message" selects message event subscriptions,
   * "signal" selects signal event subscriptions, "compensate" selects compensation event subscriptions,
   * "conditional" selects conditional event subscriptions.**/
  EventSubscriptionQuery eventType(String eventType);

  /** Only select subscriptions that belong to an execution with the given id. **/
  EventSubscriptionQuery executionId(String executionId);

  /** Only select subscriptions that belong to a process instance with the given id. **/
  EventSubscriptionQuery processInstanceId(String processInstanceId);

  /** Only select subscriptions that belong to an activity with the given id. **/
  EventSubscriptionQuery activityId(String activityId);

  /** Only select subscriptions that belong to one of the given tenant ids. */
  EventSubscriptionQuery tenantIdIn(String... tenantIds);

  /** Only select subscriptions which have no tenant id. */
  EventSubscriptionQuery withoutTenantId();

  /**
   * Select subscriptions which have no tenant id. Can be used in combination
   * with {@link #tenantIdIn(String...)}.
   */
  EventSubscriptionQuery includeEventSubscriptionsWithoutTenantId();

  /** Order by event subscription creation date (needs to be followed by {@link #asc()} or {@link #desc()}). */
  EventSubscriptionQuery orderByCreated();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of subscriptions without tenant id is database-specific.
   */
  EventSubscriptionQuery orderByTenantId();

}