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
package org.camunda.bpm.client.spring.impl.subscription.util;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.exception.NotInitializedException;
import org.camunda.bpm.client.spring.exception.NotOpenedException;
import org.camunda.bpm.client.spring.impl.util.LoggerUtil;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.beans.factory.config.BeanDefinition;

public class SubscriptionLoggerUtil extends LoggerUtil {

  public void handlerBeansFound(Class<ExternalTaskHandler> externalTaskHandlerClass,
                                String[] beanNamesForType) {
    logDebug("001", "Handler beans found for {}: {}",
        externalTaskHandlerClass, beanNamesForType);
  }

  public void beanRegistered(String subscriptionBeanName, String handlerBeanName) {
    logDebug("002", "Subscription bean '{}' registered for handler bean '{}'",
        subscriptionBeanName, handlerBeanName);
  }

  public void notFound(BeanDefinition beanDefinition) {
    logInfo("003", "Subscription not found for handler bean {}", beanDefinition);
  }

  public void found(ExternalTaskSubscription annotation, BeanDefinition beanDefinition) {
    logDebug("004", "Subscription found {} for handler bean {}", annotation,
        beanDefinition);
  }

  public void initialized(String topicName) {
    logDebug("005", "Subscription for topic name {} initialized", topicName);
  }

  public void opened(String topicName) {
    logDebug("006", "Subscription for topic name {} opened", topicName);
  }

  public void closed(String topicName) {
    logDebug("007", "Subscription for topic name {} closed", topicName);
  }

  public NotInitializedException notInitializedException(String topicName) {
    return new NotInitializedException(exceptionMessage(
        "008", "Subscription with topic name '{}' has yet not  been initialized", topicName));
  }

  public NotOpenedException notOpenedException(String topicName) {
    return new NotOpenedException(exceptionMessage(
        "009", "Subscription with topic name '{}' has yet not  been opened", topicName));
  }

}
