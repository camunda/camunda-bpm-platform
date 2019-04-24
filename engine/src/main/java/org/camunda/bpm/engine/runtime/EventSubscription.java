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

import java.util.Date;

/**
 * A message event subscription exists, if an {@link Execution} waits for an event like a message.
 *
 * @author Thorben Lindhauer
 */
public interface EventSubscription {

  /**
   * The unique identifier of the event subscription.
   */
  String getId();

  /**
   * The event subscriptions type. "message" identifies message event subscriptions,
   * "signal" identifies signal event subscription, "compensation" identifies event subscriptions
   * used for compensation events.
   */
  String getEventType();

  /**
   * The name of the event this subscription belongs to as defined in the process model.
   */
  String getEventName();

  /**
   * The execution that is subscribed on the referenced event.
   */
  String getExecutionId();

  /**
   * The process instance this subscription belongs to.
   */
  String getProcessInstanceId();

  /**
   * The identifier of the activity that this event subscription belongs to.
   * This could for example be the id of a receive task.
   */
  String getActivityId();

  /**
   * The id of the tenant this event subscription belongs to. Can be <code>null</code>
   * if the subscription belongs to no single tenant.
   */
  String getTenantId();

  /**
   * The time this event subscription was created.
   */
  Date getCreated();
}
