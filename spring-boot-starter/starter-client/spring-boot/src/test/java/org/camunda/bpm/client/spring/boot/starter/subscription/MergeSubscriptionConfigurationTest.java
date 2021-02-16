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
package org.camunda.bpm.client.spring.boot.starter.subscription;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.camunda.bpm.client.spring.boot.starter.subscription.configuration.FullSubscriptionConfiguration;
import org.camunda.bpm.client.spring.boot.starter.ParsePropertiesHelper;
import org.camunda.bpm.client.spring.boot.starter.impl.ClientAutoConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "camunda.bpm.client.subscriptions.topic-one.auto-open=false",
    "camunda.bpm.client.subscriptions.topic-one.variable-names=var-one,var-two",
    "camunda.bpm.client.subscriptions.topic-one.business-key=business-key",
    "camunda.bpm.client.subscriptions.topic-one.process-definition-id=id-one",
})
@ContextConfiguration(classes = {
    ParsePropertiesHelper.TestConfig.class,
    ClientAutoConfiguration.class,
    FullSubscriptionConfiguration.class
})
public class MergeSubscriptionConfigurationTest extends ParsePropertiesHelper {

  @MockBean
  public ExternalTaskClient externalTaskClient;

  @Autowired
  public SpringTopicSubscription subscription;

  @Test
  public void shouldCheckTopicOneProperties() {
    // annotated properties
    assertThat(subscription.getLockDuration()).isEqualTo(1111);
    assertThat(subscription.isLocalVariables()).isTrue();
    assertThat(subscription.getProcessDefinitionIdIn())
        .contains("annotated-id-one", "annotated-id-two");
    assertThat(subscription.getProcessDefinitionKeyIn())
        .contains("annotated-key-one", "annotated-key-two");

    // overridden properties
    assertThat(subscription.isAutoOpen()).isFalse();
    assertThat(subscription.getVariableNames()).contains("var-one", "var-two");
    assertThat(subscription.getBusinessKey()).isEqualTo("business-key");
    assertThat(subscription.getProcessDefinitionId()).isEqualTo("id-one");
  }

}
