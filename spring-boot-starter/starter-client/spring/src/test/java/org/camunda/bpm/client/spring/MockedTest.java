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

import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public abstract class MockedTest {

  protected static ExternalTaskClient client;
  protected static ExternalTaskClientBuilder clientBuilder;
  protected static TopicSubscriptionBuilder subscriptionBuilder;

  protected static MockedStatic<ExternalTaskClient> mockedStatic;
  
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @BeforeClass
  public static void mockClient() {
    assumeTrue(jdkSupportsMockito());

    mockedStatic = mockStatic(ExternalTaskClient.class);
    clientBuilder = mock(ExternalTaskClientBuilder.class, RETURNS_SELF);
    mockedStatic.when(() -> ExternalTaskClient.create()).thenReturn(clientBuilder);
    client = mock(ExternalTaskClient.class);
    mockedStatic.when(() -> clientBuilder.build()).thenReturn(client);
    subscriptionBuilder = mock(TopicSubscriptionBuilder.class, RETURNS_SELF);
    mockedStatic.when(() -> client.subscribe(anyString())).thenReturn(subscriptionBuilder);
    TopicSubscription topicSubscription = mock(TopicSubscription.class);
    when(subscriptionBuilder.open()).thenReturn(topicSubscription);
  }

  @AfterClass
  public static void close() {
    if(jdkSupportsMockito()) {
      mockedStatic.close();
    }
  }

  protected static boolean jdkSupportsMockito() {
    String jvmVendor = System.getProperty("java.vm.vendor");
    String javaVersion = System.getProperty("java.version");

    boolean isIbmJDK = jvmVendor != null && jvmVendor.contains("IBM");
    boolean isJava8 = javaVersion != null && javaVersion.startsWith("1.8");

    return !(isIbmJDK && isJava8);
  }

}
