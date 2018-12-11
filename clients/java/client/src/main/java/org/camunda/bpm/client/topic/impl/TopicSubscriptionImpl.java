/*
 * Copyright © 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.client.topic.impl;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionImpl implements TopicSubscription {

  protected String topicName;
  protected Long lockDuration;
  protected ExternalTaskHandler externalTaskHandler;
  protected TopicSubscriptionManager topicSubscriptionManager;
  protected List<String> variableNames;
  protected String businessKey;
  protected String processDefinitionId;
  protected List<String> processDefinitionIdIn;
  protected String processDefinitionKey;
  protected List<String> processDefinitionKeyIn;
  protected boolean withoutTenantId;
  protected List<String> tenantIdIn;

  public TopicSubscriptionImpl(String topicName, Long lockDuration, ExternalTaskHandler externalTaskHandler,
                               TopicSubscriptionManager topicSubscriptionManager, List<String> variableNames,
                               String businessKey) {
    this.topicName = topicName;
    this.lockDuration = lockDuration;
    this.externalTaskHandler = externalTaskHandler;
    this.topicSubscriptionManager = topicSubscriptionManager;
    this.variableNames = variableNames;
    this.businessKey = businessKey;
  }

  public String getTopicName() {
    return topicName;
  }

  public Long getLockDuration() {
    return lockDuration;
  }

  public ExternalTaskHandler getExternalTaskHandler() {
    return externalTaskHandler;
  }

  @Override
  public void close() {
    topicSubscriptionManager.unsubscribe(this);
  }

  public List<String> getVariableNames() {
    return variableNames;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public TopicSubscription setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public List<String> getProcessDefinitionIdIn() {
    return processDefinitionIdIn;
  }

  public TopicSubscription setProcessDefinitionIdIn(List<String> processDefinitionIds) {
    this.processDefinitionIdIn = processDefinitionIds;
    return this;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public TopicSubscription setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public List<String> getProcessDefinitionKeyIn() {
    return processDefinitionKeyIn;
  }

  public TopicSubscription setProcessDefinitionKeyIn(List<String> processDefinitionKeys) {
    this.processDefinitionKeyIn = processDefinitionKeys;
    return this;
  }

  public boolean isWithoutTenantId() {
    return withoutTenantId;
  }

  public void setWithoutTenantId(boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  public List<String> getTenantIdIn() {
    return tenantIdIn;
  }

  public TopicSubscription setTenantIdIn(List<String> tenantIds) {
    this.tenantIdIn = tenantIds;
    return this;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TopicSubscriptionImpl other = (TopicSubscriptionImpl) obj;
    if (topicName == null) {
      if (other.topicName != null)
        return false;
    } else if (!topicName.equals(other.topicName)) {
      return false;
    }
    return true;
  }

}
