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
package org.camunda.bpm.client.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ExtendedConfiguration.class })
public class ExtendedContextTest {

  @Autowired
  protected ExternalTaskClient externalTaskClient;

  @Autowired
  protected List<SpringTopicSubscription> topicSubscriptions;

  @Test
  public void startup() {
    assertThat(externalTaskClient).isNotNull();
    Assertions.assertThat(topicSubscriptions).hasSize(2);
    topicSubscriptions.stream().forEach(subscription -> assertThat(subscription.isOpen()));
  }

  @Test
  public void testSubscription() {
    testSubscription(externalTaskClient);
  }

  protected void testSubscription(ExternalTaskClient taskClient) {
    ExternalTaskClientImpl clientImpl = (ExternalTaskClientImpl) taskClient;
    List<org.camunda.bpm.client.topic.TopicSubscription> subscriptions = clientImpl.getTopicSubscriptionManager().getSubscriptions();
    assertThat(subscriptions).hasSize(2);
    org.camunda.bpm.client.topic.TopicSubscription subscription = subscriptions.iterator().next();
    assertThat(subscriptions).extracting("topicName").containsExactlyInAnyOrder(
        "methodSubscription", "testClassSubscription");
  }
}
