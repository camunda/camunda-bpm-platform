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
package org.camunda.bpm.client.spring;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.spring.event.SubscriptionInitializedEvent;
import org.camunda.bpm.client.spring.exception.NotInitializedException;
import org.camunda.bpm.client.spring.exception.NotOpenedException;
import org.camunda.bpm.client.topic.TopicSubscription;

/**
 * <p>
 * Represents a topic subscription of the External Task Client.
 * <p>
 * Existence of this bean means that the subscription has been created
 * but might have not been fully initialized or released to the External Task Client.
 * The {@link SubscriptionInitializedEvent} is emitted as soon as the subscription has been
 * fully initialized.
 * <p>
 * You can check if the subscription has been released to the External Task Client
 * with {@link #isOpen()}
 */
public interface SpringTopicSubscription extends TopicSubscription {

  /**
   * @return <ul>
   * <li>{@code true} when the topic subscription is automatically released for execution
   * <li>{@code false} when you need to call {@link #open()} to release the topic for execution
   */
  boolean isAutoOpen();

  /**
   * Releases the topic subscription for asynchronous execution when {@link #isAutoOpen()} is
   * {@code false}
   *
   * @throws ExternalTaskClientException <ul>
   *                                     <li> if topic name is {@code null} or an empty string
   *                                     <li> if lock duration is not greater than zero
   *                                     <li> if external task handler is null
   *                                     <li> if topic name has already been subscribed
   *                                     </ul>
   * @throws NotInitializedException     if called before fully initialized
   */
  void open();

  /**
   * @return <ul>
   * <li>{@code true} when the topic subscription is already released for execution
   * <li>{@code false} when the topic subscription is not already released for execution;
   * call {@link #open()} to release the topic for execution
   */
  boolean isOpen();

  /**
   * Delegates to {@link TopicSubscription#close()}.
   *
   * @throws NotInitializedException if called before fully initialized
   * @throws NotOpenedException      if called before subscription has been opened
   * @see TopicSubscription#close()
   */
  @Override
  void close();

}
