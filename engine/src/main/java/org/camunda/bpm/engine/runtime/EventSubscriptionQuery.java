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
   * "signal" selects signal event subscriptions, "compensation" selects compensation event subscriptions.**/
  EventSubscriptionQuery eventType(String eventType);

  /** Only select subscriptions that belong to an execution with the given id. **/
  EventSubscriptionQuery executionId(String executionId);

  /** Only select subscriptions that belong to a process instance with the given id. **/
  EventSubscriptionQuery processInstanceId(String processInstanceId);

  /** Only select subscriptions that belong to an activity with the given id. **/
  EventSubscriptionQuery activityId(String activityId);

  /** Order by event subscription creation date (needs to be followed by {@link #asc()} or {@link #desc()}). */
  EventSubscriptionQuery orderByCreated();

}