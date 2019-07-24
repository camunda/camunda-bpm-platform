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
package org.camunda.bpm.client.topic.impl.dto;

import org.camunda.bpm.client.topic.TopicSubscription;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class TopicRequestDto {

  protected String topicName;
  protected long lockDuration;
  protected List<String> variables;
  protected String businessKey;
  protected String processDefinitionId;
  protected List<String> processDefinitionIdIn;
  protected String processDefinitionKey;
  protected List<String> processDefinitionKeyIn;
  protected String processDefinitionVersionTag;
  protected boolean withoutTenantId;
  protected List<String> tenantIdIn;

  public TopicRequestDto(String topicName, long lockDuration, List<String> variables, String businessKey) {
    this.topicName = topicName;
    this.lockDuration = lockDuration;
    this.variables = variables;
    this.businessKey = businessKey;
  }

  public String getTopicName() {
    return topicName;
  }

  public long getLockDuration() {
    return lockDuration;
  }

  public List<String> getVariables() {
    return variables;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public List<String> getProcessDefinitionIdIn() {
    return processDefinitionIdIn;
  }

  public void setProcessDefinitionIdIn(List<String> processDefinitionIds) {
    this.processDefinitionIdIn = processDefinitionIds;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public List<String> getProcessDefinitionKeyIn() {
    return processDefinitionKeyIn;
  }

  public void setProcessDefinitionKeyIn(List<String> processDefinitionKeys) {
    this.processDefinitionKeyIn = processDefinitionKeys;
  }

  public String getProcessDefinitionVersionTag() {
    return processDefinitionVersionTag;
  }

  public void setProcessDefinitionVersionTag(String processDefinitionVersionTag) {
    this.processDefinitionVersionTag = processDefinitionVersionTag;
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

  public void setTenantIdIn(List<String> tenantIdIn) {
    this.tenantIdIn = tenantIdIn;
  }

  public static TopicRequestDto fromTopicSubscription(TopicSubscription topicSubscription, long clientLockDuration) {
    Long lockDuration = topicSubscription.getLockDuration();

    if (lockDuration == null) {
      lockDuration = clientLockDuration;
    }

    String topicName = topicSubscription.getTopicName();
    List<String> variables = topicSubscription.getVariableNames();
    String businessKey = topicSubscription.getBusinessKey();

    TopicRequestDto topicRequestDto = new TopicRequestDto(topicName, lockDuration, variables, businessKey);
    if (topicSubscription.getProcessDefinitionId() != null) {
      topicRequestDto.setProcessDefinitionId(topicSubscription.getProcessDefinitionId());
    }
    if (topicSubscription.getProcessDefinitionIdIn() != null) {
      topicRequestDto.setProcessDefinitionIdIn(topicSubscription.getProcessDefinitionIdIn());
    }
    if (topicSubscription.getProcessDefinitionKey() != null) {
      topicRequestDto.setProcessDefinitionKey(topicSubscription.getProcessDefinitionKey());
    }
    if (topicSubscription.getProcessDefinitionKeyIn() != null) {
      topicRequestDto.setProcessDefinitionKeyIn(topicSubscription.getProcessDefinitionKeyIn());
    }
    if (topicSubscription.isWithoutTenantId()) {
      topicRequestDto.setWithoutTenantId(topicSubscription.isWithoutTenantId());
    }
    if (topicSubscription.getTenantIdIn() != null) {
      topicRequestDto.setTenantIdIn(topicSubscription.getTenantIdIn());
    }
    if(topicSubscription.getProcessDefinitionVersionTag() != null) {
      topicRequestDto.setProcessDefinitionVersionTag(topicSubscription.getProcessDefinitionVersionTag());
    }
    return topicRequestDto;
  }

}
