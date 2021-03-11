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
package org.camunda.bpm.client.spring.boot.starter.client;

import org.camunda.bpm.client.spring.boot.starter.ParsePropertiesHelper;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {"camunda.bpm.client.foo=bar"})
public class DefaultClientConfigurationTest extends ParsePropertiesHelper {

  @Test
  public void shouldCheckProperties() {
    assertThat(properties.getBaseUrl()).isNull();
    assertThat(properties.getWorkerId()).isNull();
    assertThat(properties.getMaxTasks()).isNull();
    assertThat(properties.getUsePriority()).isNull();
    assertThat(properties.getDefaultSerializationFormat()).isNull();
    assertThat(properties.getDateFormat()).isNull();
    assertThat(properties.getAsyncResponseTimeout()).isNull();
    assertThat(properties.getLockDuration()).isNull();
    assertThat(properties.getDisableAutoFetching()).isNull();
    assertThat(properties.getDisableBackoffStrategy()).isNull();
    assertThat(properties.getBasicAuth()).isNull();
    assertThat(subscriptions).isEmpty();
  }

}
