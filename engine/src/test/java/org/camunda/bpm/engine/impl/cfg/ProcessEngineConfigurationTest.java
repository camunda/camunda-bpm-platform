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
package org.camunda.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElExpression;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElProvider;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.el.ElProviderCompatible;
import org.junit.Test;

public class ProcessEngineConfigurationTest {

  @Test
  public void shouldEnableStandaloneTasksByDefault() {
    // when
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createStandaloneProcessEngineConfiguration();

    // then
    assertThat(engineConfiguration.isStandaloneTasksEnabled()).isTrue();
  }

  @Test
  public void shouldUseDefaultElProviderByJuelExpressionManager() {
    // when
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createStandaloneProcessEngineConfiguration();

    engineConfiguration.init();
    DmnEngine dmnEngine = engineConfiguration.getDmnEngine();

    // then
    ElProvider expected = ((ElProviderCompatible) engineConfiguration.getExpressionManager()).toElProvider();
    assertEquals(expected, ((DefaultDmnEngineConfiguration) dmnEngine.getConfiguration()).getElProvider());
  }

  @Test
  public void shouldUseProvidedElProvider() {
    // when
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createStandaloneProcessEngineConfiguration();

    ElProvider elProvider = new ElProvider() {
      @Override
      public ElExpression createExpression(String expression) {
        throw new UnsupportedOperationException();
      }
    };

    engineConfiguration.setElProvider(elProvider);

    engineConfiguration.init();
    DmnEngine dmnEngine = engineConfiguration.getDmnEngine();

    // then
    assertEquals(elProvider, ((DefaultDmnEngineConfiguration) dmnEngine.getConfiguration()).getElProvider());
  }
}
