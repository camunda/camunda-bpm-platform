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
import org.camunda.bpm.client.spring.impl.client.ClientPostProcessor;
import org.camunda.bpm.client.spring.impl.subscription.SubscriptionPostProcessor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ClientProperties.class})
public class ClientAutoConfiguration {

  @Bean
  public static SubscriptionPostProcessor subscriptionPostprocessor() {
    return new SubscriptionPostProcessor(PropertiesAwareSpringTopicSubscription.class);
  }

  @Bean
  public static ClientPostProcessor clientPostProcessor() {
    return new ClientPostProcessor(PropertiesAwareClientFactory.class);
  }

}
