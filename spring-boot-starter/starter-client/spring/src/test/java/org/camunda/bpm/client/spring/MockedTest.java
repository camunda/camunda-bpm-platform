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

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ExternalTaskClient.class})
public abstract class MockedTest {

  protected static ExternalTaskClient client;
  protected static ExternalTaskClientBuilder clientBuilder;
  protected static TopicSubscriptionBuilder subscriptionBuilder;

  protected static MockedStatic<ExternalTaskClient> mockedStatic;

  @BeforeClass
  public static void mockClient() {
    mockedStatic = mockStatic(ExternalTaskClient.class);
    clientBuilder = mock(ExternalTaskClientBuilder.class, RETURNS_SELF);
    PowerMockito.when(ExternalTaskClient.create()).thenReturn(clientBuilder);
    client = mock(ExternalTaskClient.class);
    PowerMockito.when(clientBuilder.build()).thenReturn(client);
    subscriptionBuilder = mock(TopicSubscriptionBuilder.class, RETURNS_SELF);
    PowerMockito.when(client.subscribe(anyString())).thenReturn(subscriptionBuilder);
    TopicSubscription topicSubscription = mock(TopicSubscription.class);
    when(subscriptionBuilder.open()).thenReturn(topicSubscription);
  }

  @AfterClass
  public static void close() {
    mockedStatic.close();
  }

}
