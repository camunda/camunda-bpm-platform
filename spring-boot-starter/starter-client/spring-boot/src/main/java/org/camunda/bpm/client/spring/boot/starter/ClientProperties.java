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
package org.camunda.bpm.client.spring.boot.starter;

import org.camunda.bpm.client.spring.impl.client.ClientConfiguration;
import org.camunda.bpm.client.spring.impl.subscription.SubscriptionConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "camunda.bpm.client")
public class ClientProperties extends ClientConfiguration {

  @NestedConfigurationProperty
  protected Map<String, SubscriptionConfiguration> subscriptions = new HashMap<>();

  @NestedConfigurationProperty
  protected BasicAuthProperties basicAuth;

  public SubscriptionConfiguration findSubscriptionPropsByTopicName(String topic) {
    return subscriptions.get(topic);
  }

  public Map<String, SubscriptionConfiguration> getSubscriptions() {
    return subscriptions;
  }

  public void setSubscriptions(Map<String, SubscriptionConfiguration> subscriptions) {
    this.subscriptions = subscriptions;
  }

  public BasicAuthProperties getBasicAuth() {
    return basicAuth;
  }

  public void setBasicAuth(BasicAuthProperties basicAuth) {
    this.basicAuth = basicAuth;
  }

}