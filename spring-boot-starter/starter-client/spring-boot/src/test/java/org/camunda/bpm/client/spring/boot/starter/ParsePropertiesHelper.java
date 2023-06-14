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

import org.camunda.bpm.client.spring.impl.subscription.SubscriptionConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ParsePropertiesHelper.TestConfig.class)
public abstract class ParsePropertiesHelper {

  @EnableConfigurationProperties(ClientProperties.class)
  public static class TestConfig {
  }

  @Autowired
  protected ClientProperties properties;

  protected Map<String, SubscriptionConfiguration> subscriptions;
  protected BasicAuthProperties basicAuth;

  @PostConstruct
  public void init() {
    subscriptions = properties.getSubscriptions();
    basicAuth = properties.getBasicAuth();
  }

}