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
package org.camunda.bpm.spring.boot.starter.runlistener;

import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersionTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PropertiesListenerTest {

  @Rule
  public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock
  private ConfigurableEnvironment environment;

  @Mock
  private ApplicationEnvironmentPreparedEvent event;

  @Mock
  private MutablePropertySources mutablePropertySources;

  @Captor
  private ArgumentCaptor<PropertiesPropertySource> propertiesPropertySource;

  @Before
  public void setUp() throws Exception {
    when(event.getEnvironment()).thenReturn(environment);
    when(environment.getPropertySources()).thenReturn(mutablePropertySources);
  }

  @Test
  public void addPropertiesPropertySource() throws Exception {
    final CamundaBpmVersion version = new CamundaBpmVersion();

    new PropertiesListener(version).onApplicationEvent(event);

    verify(mutablePropertySources).addFirst(propertiesPropertySource.capture());

    assertThat(propertiesPropertySource.getValue()).isEqualTo(version.getPropertiesPropertySource());
  }
}
