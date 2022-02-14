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
package org.camunda.bpm.client.spring.boot.starter.impl;

import org.camunda.bpm.client.spring.boot.starter.ClientProperties;
import org.camunda.bpm.client.spring.impl.subscription.SpringTopicSubscriptionImpl;
import org.camunda.bpm.client.spring.impl.subscription.SubscriptionConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;

import java.util.function.Predicate;

public class PropertiesAwareSpringTopicSubscription extends SpringTopicSubscriptionImpl {

  @Autowired
  protected ClientProperties clientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    mergeSubscriptionWithProperties();
    super.afterPropertiesSet();
  }

  @Override
  protected Predicate<ApplicationEvent> isEventThatCanStartSubscription() {
    return event -> event instanceof ApplicationStartedEvent;
  }

  protected void mergeSubscriptionWithProperties() {
    SubscriptionConfiguration merge = getSubscriptionConfiguration();

    String topicName = merge.getTopicName();
    SubscriptionConfiguration subscriptionProperties =
        clientProperties.findSubscriptionPropsByTopicName(topicName);

    if (subscriptionProperties != null) {
      if (subscriptionProperties.getAutoOpen() != null) {
        merge.setAutoOpen(subscriptionProperties.getAutoOpen());
      }
      if (subscriptionProperties.getLockDuration() != null) {
        merge.setLockDuration(subscriptionProperties.getLockDuration());
      }
      if (subscriptionProperties.getVariableNames() != null) {
        merge.setVariableNames(subscriptionProperties.getVariableNames());
      }
      if (subscriptionProperties.getBusinessKey() != null) {
        merge.setBusinessKey(subscriptionProperties.getBusinessKey());
      }
      if (subscriptionProperties.getProcessDefinitionId() != null) {
        merge.setProcessDefinitionId(subscriptionProperties.getProcessDefinitionId());
      }
      if (subscriptionProperties.getProcessDefinitionIdIn() != null) {
        merge.setProcessDefinitionIdIn(subscriptionProperties.getProcessDefinitionIdIn());
      }
      if (subscriptionProperties.getProcessDefinitionKey() != null) {
        merge.setProcessDefinitionKey(subscriptionProperties.getProcessDefinitionKey());
      }
      if (subscriptionProperties.getProcessDefinitionKeyIn() != null) {
        merge.setProcessDefinitionKeyIn(subscriptionProperties.getProcessDefinitionKeyIn());
      }
      if (subscriptionProperties.getProcessDefinitionVersionTag() != null) {
        merge.setProcessDefinitionVersionTag(subscriptionProperties.getProcessDefinitionVersionTag());
      }
      if (subscriptionProperties.getProcessVariables() != null) {
        merge.setProcessVariables(subscriptionProperties.getProcessVariables());
      }
      if (subscriptionProperties.getWithoutTenantId() != null) {
        merge.setWithoutTenantId(subscriptionProperties.getWithoutTenantId());
      }
      if (subscriptionProperties.getTenantIdIn() != null) {
        merge.setTenantIdIn(subscriptionProperties.getTenantIdIn());
      }
      if (subscriptionProperties.getIncludeExtensionProperties() != null) {
        merge.setIncludeExtensionProperties(subscriptionProperties.getIncludeExtensionProperties());
      }

      setSubscriptionConfiguration(merge);
    }
  }

}
