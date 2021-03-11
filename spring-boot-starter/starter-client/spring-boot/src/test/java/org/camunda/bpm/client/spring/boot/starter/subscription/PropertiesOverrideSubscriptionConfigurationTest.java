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

import org.camunda.bpm.client.spring.boot.starter.subscription.configuration.FullSubscriptionConfiguration;
import org.camunda.bpm.client.spring.boot.starter.ParsePropertiesHelper;
import org.camunda.bpm.client.spring.impl.subscription.SubscriptionConfiguration;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@TestPropertySource(properties = {
    "camunda.bpm.client.subscriptions.topic-one.auto-open=false",
    "camunda.bpm.client.subscriptions.topic-one.lock-duration=555",
    "camunda.bpm.client.subscriptions.topic-one.variable-names=var-one,var-two",
    "camunda.bpm.client.subscriptions.topic-one.local-variables=true",
    "camunda.bpm.client.subscriptions.topic-one.business-key=business-key",
    "camunda.bpm.client.subscriptions.topic-one.process-definition-id=definition-id",
    "camunda.bpm.client.subscriptions.topic-one.process-definition-id-in=id-one,id-two",
    "camunda.bpm.client.subscriptions.topic-one.process-definition-key=key",
    "camunda.bpm.client.subscriptions.topic-one.process-definition-key-in=key-one,key-two",
    "camunda.bpm.client.subscriptions.topic-one.process-definition-version-tag=version-tag",
    "camunda.bpm.client.subscriptions.topic-one.process-variables.var-name-foo=var-val-foo",
    "camunda.bpm.client.subscriptions.topic-one.process-variables.var-name-bar=var-val-bar",
    "camunda.bpm.client.subscriptions.topic-one.without-tenant-id=true",
    "camunda.bpm.client.subscriptions.topic-one.tenant-id-in=tenant-id-one,tenant-id-two",
    "camunda.bpm.client.subscriptions.topic-one.include-extension-properties=true",
})
@ContextConfiguration(classes = {
    ParsePropertiesHelper.TestConfig.class,
    FullSubscriptionConfiguration.class
})
public class PropertiesOverrideSubscriptionConfigurationTest extends ParsePropertiesHelper {

  @Test
  public void shouldCheckTopicOneProperties() {
    SubscriptionConfiguration subscriptionOne = subscriptions.get("topic-one");

    assertThat(subscriptionOne.getAutoOpen()).isFalse();
    assertThat(subscriptionOne.getLockDuration()).isEqualTo(555);
    assertThat(subscriptionOne.getVariableNames()).contains("var-one", "var-two");
    assertThat(subscriptionOne.getLocalVariables()).isTrue();
    assertThat(subscriptionOne.getBusinessKey()).isEqualTo("business-key");
    assertThat(subscriptionOne.getProcessDefinitionId()).isEqualTo("definition-id");
    assertThat(subscriptionOne.getProcessDefinitionIdIn()).contains("id-one", "id-two");
    assertThat(subscriptionOne.getProcessDefinitionKey()).isEqualTo("key");
    assertThat(subscriptionOne.getProcessDefinitionKeyIn()).contains("key-one", "key-two");
    assertThat(subscriptionOne.getProcessDefinitionVersionTag()).isEqualTo("version-tag");
    assertThat(subscriptionOne.getProcessVariables())
        .contains(entry("var-name-foo", "var-val-foo"), entry("var-name-bar", "var-val-bar"));
    assertThat(subscriptionOne.getWithoutTenantId()).isTrue();
    assertThat(subscriptionOne.getTenantIdIn()).contains("tenant-id-one", "tenant-id-two");
    assertThat(subscriptionOne.getIncludeExtensionProperties()).isTrue();
  }

}
