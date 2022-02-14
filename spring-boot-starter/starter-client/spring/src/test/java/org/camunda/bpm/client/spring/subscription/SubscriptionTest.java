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
package org.camunda.bpm.client.spring.subscription;

import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.camunda.bpm.client.spring.MockedTest;
import org.camunda.bpm.client.spring.configuration.FullConfiguration;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@ContextConfiguration(classes = {
    FullConfiguration.class
})
@DirtiesContext // context cannot be reused since the mocks need to be reinitialized completely
public class SubscriptionTest extends MockedTest {

  @Autowired
  @Qualifier("handlerSubscription")
  protected SpringTopicSubscription subscription;

  @Autowired
  protected ExternalTaskHandler handler;

  @Test
  public void shouldVerifyTopicSubscription() {
    assertThat(subscription.getTopicName()).isEqualTo("topic-name");
    assertThat(subscription.getLockDuration()).isEqualTo(1111);
    assertThat(subscription.getExternalTaskHandler()).isEqualTo(handler);
    assertThat(subscription.getVariableNames()).contains("variable-one", "variable-two");
    assertThat(subscription.isLocalVariables()).isTrue();
    assertThat(subscription.getBusinessKey()).isEqualTo("business-key");
    assertThat(subscription.getProcessDefinitionId()).isEqualTo("process-definition-id");
    assertThat(subscription.getProcessDefinitionIdIn()).contains("id-one", "id-two");
    assertThat(subscription.getProcessDefinitionKey()).isEqualTo("key");
    assertThat(subscription.getProcessDefinitionKeyIn()).contains("key-one", "key-two");
    assertThat(subscription.getProcessDefinitionVersionTag()).isEqualTo("version-tag");
    assertThat(subscription.getProcessVariables())
        .contains(entry("var-name-foo", "var-val-foo"), entry("var-name-bar", "var-val-bar"));
    assertThat(subscription.isWithoutTenantId()).isTrue();
    assertThat(subscription.getTenantIdIn()).contains("tenant-id-one", "tenant-id-two");
    assertThat(subscription.isIncludeExtensionProperties()).isTrue();
  }

}
