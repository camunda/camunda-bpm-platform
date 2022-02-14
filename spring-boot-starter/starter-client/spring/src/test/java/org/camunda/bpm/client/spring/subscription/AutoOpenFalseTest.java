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
import org.camunda.bpm.client.spring.subscription.configuration.AutoOpenFalseConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
    AutoOpenFalseConfiguration.class
})
public class AutoOpenFalseTest extends MockedTest {

  @Autowired
  protected SpringTopicSubscription subscription;

  @Test
  public void shouldOpenTopicSubscription() {
    // assume
    assertThat(subscription.isOpen()).isFalse();
    assertThat(subscription.isAutoOpen()).isFalse();

    // when opened initially
    subscription.open();

    // then
    assertThat(subscription.isAutoOpen()).isFalse();
    assertThat(subscription.isOpen()).isTrue();

    // when closed
    subscription.close();

    // then
    assertThat(subscription.isOpen()).isFalse();

    // when reopened
    subscription.open();

    // then
    assertThat(subscription.isOpen()).isTrue();
  }

}
