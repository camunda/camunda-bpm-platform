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

import org.camunda.bpm.client.spring.configuration.DefaultConfiguration;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ContextConfiguration(classes = {DefaultConfiguration.class})
@DirtiesContext // context cannot be reused since the mocks need to be reinitialized completely
public class DefaultConfigurationTest extends MockedTest {

  @Autowired
  @Qualifier("handler")
  protected ExternalTaskHandler handler;

  @Test
  public void shouldVerifyClientDefaults() {
    verify(clientBuilder, never()).baseUrl(anyString());
    verify(clientBuilder, never()).workerId(anyString());
    verify(clientBuilder, never()).maxTasks(anyInt());
    verify(clientBuilder, never()).usePriority(anyBoolean());
    verify(clientBuilder, never()).useCreateTime(anyBoolean());
    verify(clientBuilder, never()).orderByCreateTime();
    verify(clientBuilder, never()).asyncResponseTimeout(anyLong());
    verify(clientBuilder, never()).disableAutoFetching();
    verify(clientBuilder, never()).disableBackoffStrategy();
    verify(clientBuilder, never()).lockDuration(anyLong());
    verify(clientBuilder, never()).dateFormat(anyString());
    verify(clientBuilder, never()).defaultSerializationFormat(anyString());
    verify(clientBuilder).build();
    verifyNoMoreInteractions(clientBuilder);
  }

  @Test
  public void shouldVerifySubscriptionDefaults() {
    verify(client).subscribe("topic-name");
    verify(subscriptionBuilder).handler(handler);
    verify(subscriptionBuilder, never()).variables(any());
    verify(subscriptionBuilder, never()).lockDuration(anyLong());
    verify(subscriptionBuilder, never()).localVariables(anyBoolean());
    verify(subscriptionBuilder, never()).businessKey(anyString());
    verify(subscriptionBuilder, never()).processDefinitionId(anyString());
    verify(subscriptionBuilder, never()).processDefinitionIdIn(any());
    verify(subscriptionBuilder, never()).processDefinitionKey(anyString());
    verify(subscriptionBuilder, never()).processDefinitionKeyIn(any());
    verify(subscriptionBuilder, never()).processDefinitionVersionTag(anyString());
    verify(subscriptionBuilder, never()).processVariablesEqualsIn(anyMap());
    verify(subscriptionBuilder, never()).withoutTenantId();
    verify(subscriptionBuilder, never()).tenantIdIn(any());
    verify(subscriptionBuilder, never()).includeExtensionProperties(anyBoolean());
    verify(subscriptionBuilder).open();
    verifyNoMoreInteractions(subscriptionBuilder);
  }

}