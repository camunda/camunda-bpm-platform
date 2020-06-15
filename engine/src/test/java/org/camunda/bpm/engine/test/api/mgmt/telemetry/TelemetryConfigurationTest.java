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
package org.camunda.bpm.engine.test.api.mgmt.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.util.TelemetryHelper.fetchConfigurationProperty;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.After;
import org.junit.Test;

public class TelemetryConfigurationTest {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @After
  public void reset() {
    ProcessEngineImpl processEngineImpl = processEngineConfiguration.getProcessEngine();
    processEngineImpl.close();
    processEngineImpl = null;
  }

  @Test
  public void shouldHaveDisabledTelemetryByDefault() {
    // given
    processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
                              .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    processEngineConfiguration.buildProcessEngine();

    // then
    assertThat(processEngineConfiguration.isTelemetryEnabled()).isFalse();
    assertThat(fetchConfigurationProperty(processEngineConfiguration)).isNull();
  }

  @Test
  public void shouldStartEngineWithTelemetryEnabled() {
    // given
    processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
                              .setTelemetryEnabled(true)
                              .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    processEngineConfiguration.buildProcessEngine();

    // then
    assertThat(processEngineConfiguration.isTelemetryEnabled()).isTrue();
    assertThat(Boolean.parseBoolean(fetchConfigurationProperty(processEngineConfiguration).getValue())).isTrue();
  }

}
