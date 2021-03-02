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

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionBuilderImpl implements TopicSubscriptionBuilder {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String topicName;
  protected Long lockDuration;
  protected List<String> variableNames;
  protected boolean localVariables;
  protected String businessKey;
  protected String processDefinitionId;
  protected List<String> processDefinitionIds;
  protected String processDefinitionKey;
  protected List<String> processDefinitionKeys;
  protected String processDefinitionVersionTag;
  protected Map<String, Object> processVariables;
  protected boolean withoutTenantId;
  protected List<String> tenantIds;
  protected ExternalTaskHandler externalTaskHandler;
  protected TopicSubscriptionManager topicSubscriptionManager;
  protected boolean includeExtensionProperties;


  public TopicSubscriptionBuilderImpl(String topicName, TopicSubscriptionManager topicSubscriptionManager) {
    this.topicName = topicName;
    this.variableNames = null; // if not null, no variables are retrieved by default
    this.lockDuration = null;
    this.topicSubscriptionManager = topicSubscriptionManager;
  }

  public TopicSubscriptionBuilder lockDuration(long lockDuration) {
    this.lockDuration = lockDuration;
    return this;
  }

  public TopicSubscriptionBuilder handler(ExternalTaskHandler externalTaskHandler) {
    this.externalTaskHandler = externalTaskHandler;
    return this;
  }

  public TopicSubscriptionBuilder variables(String... variableNames) {
    ensureNotNull(variableNames, "variableNames");
    this.variableNames = Arrays.asList(variableNames);
    return this;
  }

  public TopicSubscriptionBuilder localVariables(boolean localVariables) {
    this.localVariables = localVariables;
    return this;
  }
  
  public TopicSubscriptionBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public TopicSubscriptionBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public TopicSubscriptionBuilder processDefinitionIdIn(String... processDefinitionIds) {
    ensureNotNull(processDefinitionIds, "processDefinitionIds");
    this.processDefinitionIds = Arrays.asList(processDefinitionIds);
    return this;
  }

  public TopicSubscriptionBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public TopicSubscriptionBuilder processDefinitionKeyIn(String... processDefinitionKeys) {
    ensureNotNull(processDefinitionKeys, "processDefinitionKeys");
    this.processDefinitionKeys = Arrays.asList(processDefinitionKeys);
    return this;
  }

  public TopicSubscriptionBuilder processDefinitionVersionTag(String processDefinitionVersionTag) {
    ensureNotNull(processDefinitionVersionTag, "processDefinitionVersionTag");
    this.processDefinitionVersionTag = processDefinitionVersionTag;
    return this;
  }

  public TopicSubscriptionBuilder processVariablesEqualsIn(Map<String, Object> processVariables) {
    ensureNotNull(processVariables, "processVariables");
    if (this.processVariables == null) {
      this.processVariables = new HashMap<>();
    }
    for (Map.Entry<String, Object> processVariable : processVariables.entrySet()) {
      ensureNotNull(processVariable.getKey(), "processVariableName");
      this.processVariables.put(processVariable.getKey(), processVariable.getValue());
    }
    return this;
  }

  public TopicSubscriptionBuilder processVariableEquals(String name, Object value) {
    ensureNotNull(name, "processVariableName");
    if (this.processVariables == null) {
      this.processVariables = new HashMap<>();
    }
    this.processVariables.put(name, value);
    return this;
  }

  public TopicSubscriptionBuilder withoutTenantId() {
    withoutTenantId = true;
    return this;
  }

  public TopicSubscriptionBuilder tenantIdIn(String... tenantIds) {
    ensureNotNull(tenantIds, "tenantIds");
    this.tenantIds = Arrays.asList(tenantIds);
    return this;
  }

  public TopicSubscriptionBuilder includeExtensionProperties(boolean includeExtensionProperties) {
    this.includeExtensionProperties = includeExtensionProperties;
    return this;
  }

  public TopicSubscription open() {
    if (topicName == null) {
      throw LOG.topicNameNullException();
    }

    if (lockDuration != null && lockDuration <= 0L) {
      throw LOG.lockDurationIsNotGreaterThanZeroException(lockDuration);
    }

    if (externalTaskHandler == null) {
      throw LOG.externalTaskHandlerNullException();
    }

    TopicSubscriptionImpl subscription = new TopicSubscriptionImpl(topicName, lockDuration, externalTaskHandler, topicSubscriptionManager, variableNames, businessKey);
    if (processDefinitionId != null) {
      subscription.setProcessDefinitionId(processDefinitionId);
    }
    if (processDefinitionIds != null) {
      subscription.setProcessDefinitionIdIn(processDefinitionIds);
    }
    if (processDefinitionKey != null) {
      subscription.setProcessDefinitionKey(processDefinitionKey);
    }
    if (processDefinitionKeys != null) {
      subscription.setProcessDefinitionKeyIn(processDefinitionKeys);
    }
    if (withoutTenantId) {
      subscription.setWithoutTenantId(withoutTenantId);
    }
    if (tenantIds != null) {
      subscription.setTenantIdIn(tenantIds);
    }
    if(processDefinitionVersionTag != null) {
      subscription.setProcessDefinitionVersionTag(processDefinitionVersionTag);
    }
    if (processVariables != null) {
      subscription.setProcessVariables(processVariables);
    }
    if (localVariables) {
      subscription.setLocalVariables(localVariables);
    }
    if(includeExtensionProperties) {
      subscription.setIncludeExtensionProperties(includeExtensionProperties);
    }
    topicSubscriptionManager.subscribe(subscription);

    return subscription;
  }

  protected void ensureNotNull(Object tenantIds, String parameterName) {
    if (tenantIds == null) {
      throw LOG.passNullValueParameter(parameterName);
    }
  }

}
