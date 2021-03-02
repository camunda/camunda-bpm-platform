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
package org.camunda.bpm.client.topic.impl;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionImpl implements TopicSubscription {

  protected String topicName;
  protected Long lockDuration;
  protected ExternalTaskHandler externalTaskHandler;
  protected TopicSubscriptionManager topicSubscriptionManager;
  protected List<String> variableNames;
  protected boolean localVariables;
  protected String businessKey;
  protected String processDefinitionId;
  protected List<String> processDefinitionIdIn;
  protected String processDefinitionKey;
  protected List<String> processDefinitionKeyIn;
  protected String processDefinitionVersionTag;
  protected Map<String, Object> processVariables;
  protected boolean withoutTenantId;
  protected List<String> tenantIdIn;
  protected boolean includeExtensionProperties;

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

  public boolean isLocalVariables() {
    return localVariables;
  }

  public void setLocalVariables(boolean localVariables) {
    this.localVariables = localVariables;
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

  public String getProcessDefinitionVersionTag() {
    return processDefinitionVersionTag;
  }

  public void setProcessDefinitionVersionTag(String processDefinitionVersionTag) {
    this.processDefinitionVersionTag = processDefinitionVersionTag;
  }

  public HashMap<String, Object> getProcessVariables() {
    return (HashMap<String, Object>) processVariables;
  }

  public void setProcessVariables(Map<String, Object> processVariables) {
    if (this.processVariables == null) {
      this.processVariables = new HashMap<>();
    }
    for (Map.Entry<String, Object> processVariable : processVariables.entrySet()) {
      this.processVariables.put(processVariable.getKey(), processVariable.getValue());
    }
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

  public boolean isIncludeExtensionProperties() {
    return includeExtensionProperties;
  }

  public void setIncludeExtensionProperties(boolean includeExtensionProperties) {
    this.includeExtensionProperties = includeExtensionProperties;
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
