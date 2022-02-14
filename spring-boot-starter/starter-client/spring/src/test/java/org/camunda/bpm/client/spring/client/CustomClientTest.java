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
package org.camunda.bpm.client.spring.client;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.MockedTest;
import org.camunda.bpm.client.spring.client.configuration.CustomClientConfiguration;
import org.camunda.bpm.client.spring.configuration.SimpleSubscriptionConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ContextConfiguration(classes = {
    SimpleSubscriptionConfiguration.class,
    CustomClientConfiguration.class
})
public class CustomClientTest extends MockedTest {

  @Autowired
  public List<ExternalTaskClient> clients;

  @Test
  public void shouldVerifyCustomClientPresent() {
    verify(clientBuilder).baseUrl("http://localhost:8080/engine-rest");
    verify(clientBuilder).workerId("custom-client");
    verify(clientBuilder).build();
    verifyNoMoreInteractions(clientBuilder);

    assertThat(clients.size()).isEqualTo(1);
  }

}