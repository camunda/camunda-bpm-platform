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
package org.camunda.bpm.client.topic;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.task.ExternalTaskHandler;

/**
 * <p>A fluent builder to configure the topic subscription</p>
 *
 * @author Tassilo Weidner
 */
public interface TopicSubscriptionBuilder {

  /**
   * @param lockDuration <ul>
   *                       <li> in milliseconds to lock the external tasks
   *                       <li> must be greater than zero
   *                       <li> the default lock duration is 20 seconds (20,000 milliseconds)
   *                       <li> overrides the lock duration configured on bootstrapping the client
   *                     </ul>
   * @return the builder
   */
  TopicSubscriptionBuilder lockDuration(long lockDuration);

  /**
   * @param handler which will be executed for the external task
   * @return the builder
   */
  TopicSubscriptionBuilder handler(ExternalTaskHandler handler);

  /**
   * @param variableNames of variables which are supposed to be retrieved
   * @return the builder
   */
  TopicSubscriptionBuilder variables(String... variableNames);

  /**
   * @param businessKey to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder businessKey(String businessKey);

  /**
   * @param processDefinitionId to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder processDefinitionId(String processDefinitionId);

  /**
   * @param processDefinitionIds to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder processDefinitionIdIn(String... processDefinitionIds);

  /**
   * @param processDefinitionKey to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder processDefinitionKey(String processDefinitionKey);

  /**
   * @param processDefinitionKeys to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder processDefinitionKeyIn(String... processDefinitionKeys);

  /**
   * @param processDefinitionKeys to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder processDefinitionVersionTag(String processDefinitionVersionTag);

  /**
   * Filter for external tasks without tenant
   * @return the builder
   */
  TopicSubscriptionBuilder withoutTenantId();

  /**
   * @param tenantIds to filter for external tasks that are supposed to be fetched and locked
   * @return the builder
   */
  TopicSubscriptionBuilder tenantIdIn(String... tenantIds);

  /**
   * Release the topic subscription for being executed asynchronously
   *
   * @throws ExternalTaskClientException
   * <ul>
   *   <li> if topic name is null or an empty string
   *   <li> if lock duration is not greater than zero
   *   <li> if external task handler is null
   *   <li> if topic name has already been subscribed
   * </ul>
   * @return the builder
   */
  TopicSubscription open();

}
