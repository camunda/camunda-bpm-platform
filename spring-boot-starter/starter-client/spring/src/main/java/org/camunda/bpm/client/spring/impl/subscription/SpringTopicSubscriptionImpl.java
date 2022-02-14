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
package org.camunda.bpm.client.spring.impl.subscription;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.camunda.bpm.client.spring.event.SubscriptionInitializedEvent;
import org.camunda.bpm.client.spring.impl.client.util.ClientLoggerUtil;
import org.camunda.bpm.client.spring.impl.subscription.util.SubscriptionLoggerUtil;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SpringTopicSubscriptionImpl
    implements SpringTopicSubscription, InitializingBean {

  protected static final SubscriptionLoggerUtil LOG = ClientLoggerUtil.SUBSCRIPTION_LOGGER;

  protected SubscriptionConfiguration subscriptionConfiguration;
  protected ExternalTaskHandler externalTaskHandler;

  protected TopicSubscriptionBuilder topicSubscriptionBuilder;
  protected TopicSubscription topicSubscription;

  @Autowired
  protected ExternalTaskClient client;

  @Autowired
  protected ApplicationEventPublisher applicationEventPublisher;

  protected Predicate<ApplicationEvent> isEventThatCanStartSubscription() {
    return event -> event instanceof ContextRefreshedEvent;
  }

  @EventListener
  public void start(ApplicationEvent event) {
    if (isEventThatCanStartSubscription().test(event)) {
      initialize();
    }
  }

  public void initialize() {
    String topicName = subscriptionConfiguration.getTopicName();
    topicSubscriptionBuilder = client.subscribe(topicName)
        .handler(externalTaskHandler);

    List<String> variableNames = subscriptionConfiguration.getVariableNames();
    if (variableNames != null) {
      topicSubscriptionBuilder.variables(toArray(variableNames));
    }
    Long lockDuration = subscriptionConfiguration.getLockDuration();
    if (lockDuration != null) {
      topicSubscriptionBuilder.lockDuration(lockDuration);
    }
    Boolean localVariables = subscriptionConfiguration.getLocalVariables();
    if (localVariables != null && localVariables) {
      topicSubscriptionBuilder.localVariables(true);
    }
    String businessKey = subscriptionConfiguration.getBusinessKey();
    if (businessKey != null) {
      topicSubscriptionBuilder.businessKey(businessKey);
    }
    String processDefinitionId = subscriptionConfiguration.getProcessDefinitionId();
    if (processDefinitionId != null) {
      topicSubscriptionBuilder.processDefinitionId(processDefinitionId);
    }
    List<String> processDefinitionIdIn = subscriptionConfiguration.getProcessDefinitionIdIn();
    if (processDefinitionIdIn != null) {
      topicSubscriptionBuilder.processDefinitionIdIn(toArray(processDefinitionIdIn));
    }
    String processDefinitionKey = subscriptionConfiguration.getProcessDefinitionKey();
    if (processDefinitionKey != null) {
      topicSubscriptionBuilder.processDefinitionKey(processDefinitionKey);
    }
    List<String> processDefinitionKeyIn = subscriptionConfiguration.getProcessDefinitionKeyIn();
    if (processDefinitionKeyIn != null) {
      topicSubscriptionBuilder.processDefinitionKeyIn(toArray(processDefinitionKeyIn));
    }
    String processDefinitionVersionTag = subscriptionConfiguration.getProcessDefinitionVersionTag();
    if (processDefinitionVersionTag != null) {
      topicSubscriptionBuilder.processDefinitionVersionTag(processDefinitionVersionTag);
    }
    Map<String, Object> processVariablesEqualsIn = subscriptionConfiguration.getProcessVariables();
    if (processVariablesEqualsIn != null) {
      topicSubscriptionBuilder.processVariablesEqualsIn(processVariablesEqualsIn);
    }
    Boolean withoutTenantId = subscriptionConfiguration.getWithoutTenantId();
    if (withoutTenantId != null && withoutTenantId) {
      topicSubscriptionBuilder.withoutTenantId();
    }
    List<String> tenantIdIn = subscriptionConfiguration.getTenantIdIn();
    if (tenantIdIn != null) {
      topicSubscriptionBuilder.tenantIdIn(toArray(tenantIdIn));
    }
    Boolean includeExtensionProperties = subscriptionConfiguration.getIncludeExtensionProperties();
    if (includeExtensionProperties != null && includeExtensionProperties) {
      topicSubscriptionBuilder.includeExtensionProperties(true);
    }
    if(isAutoOpen()) {
      open();
    }
    publishInitializedEvent(topicName);
  }

  protected void publishInitializedEvent(String topicName) {
    SubscriptionInitializedEvent event = new SubscriptionInitializedEvent(this);
    applicationEventPublisher.publishEvent(event);

    LOG.initialized(topicName);
  }

  @Override
  public void open() {
    String topicName = subscriptionConfiguration.getTopicName();

    if (topicSubscriptionBuilder != null) {
      topicSubscription = topicSubscriptionBuilder.open();
      LOG.opened(topicName);
    } else {
      throw LOG.notInitializedException(topicName);
    }
  }

  @Override
  public boolean isOpen() {
    return topicSubscription != null;
  }

  public void closeInternally() {
    if (topicSubscription != null) {
      topicSubscription.close();
      topicSubscription = null;

      String topicName = subscriptionConfiguration.getTopicName();
      LOG.closed(topicName);
    }
  }

  public void close() {
    String topicName = subscriptionConfiguration.getTopicName();
    if (topicSubscriptionBuilder == null) {
      throw LOG.notInitializedException(topicName);
    }

    if (topicSubscription != null) {
      closeInternally();
    } else {
      throw LOG.notOpenedException(topicName);
    }
  }

  @Override
  public boolean isAutoOpen() {
    return subscriptionConfiguration.getAutoOpen();
  }

  public void setExternalTaskHandler(ExternalTaskHandler externalTaskHandler) {
    this.externalTaskHandler = externalTaskHandler;
  }

  public SubscriptionConfiguration getSubscriptionConfiguration() {
    return subscriptionConfiguration;
  }

  public void setSubscriptionConfiguration(SubscriptionConfiguration subscriptionConfiguration) {
    this.subscriptionConfiguration = subscriptionConfiguration;
  }

  @Override
  public String getTopicName() {
    return subscriptionConfiguration.getTopicName();
  }

  @Override
  public Long getLockDuration() {
    return subscriptionConfiguration.getLockDuration();
  }

  @Override
  public ExternalTaskHandler getExternalTaskHandler() {
    return externalTaskHandler;
  }

  @Override
  public List<String> getVariableNames() {
    return subscriptionConfiguration.getVariableNames();
  }

  @Override
  public boolean isLocalVariables() {
    return subscriptionConfiguration.getLocalVariables();
  }

  @Override
  public String getBusinessKey() {
    return subscriptionConfiguration.getBusinessKey();
  }

  @Override
  public String getProcessDefinitionId() {
    return subscriptionConfiguration.getProcessDefinitionId();
  }

  @Override
  public List<String> getProcessDefinitionIdIn() {
    return subscriptionConfiguration.getProcessDefinitionIdIn();
  }

  @Override
  public String getProcessDefinitionKey() {
    return subscriptionConfiguration.getProcessDefinitionKey();
  }

  @Override
  public List<String> getProcessDefinitionKeyIn() {
    return subscriptionConfiguration.getProcessDefinitionKeyIn();
  }

  @Override
  public String getProcessDefinitionVersionTag() {
    return subscriptionConfiguration.getProcessDefinitionVersionTag();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return subscriptionConfiguration.getProcessVariables();
  }

  @Override
  public boolean isWithoutTenantId() {
    return subscriptionConfiguration.getWithoutTenantId();
  }

  @Override
  public List<String> getTenantIdIn() {
    return subscriptionConfiguration.getTenantIdIn();
  }

  @Override
  public boolean isIncludeExtensionProperties() {
    return subscriptionConfiguration.getIncludeExtensionProperties();
  }

  protected String[] toArray(List<String> list) {
    return list.toArray(new String[0]);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }

}