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

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class TelemetryConfigurationTest {

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

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
    assertThat(processEngineConfiguration.isInitializeTelemetry()).isFalse();
    assertThat(processEngineConfiguration.getManagementService().isTelemetryEnabled()).isFalse();
  }

  @Test
  public void shouldStartEngineWithTelemetryEnabled() {
    // given
    processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
                              .setInitializeTelemetry(true)
                              .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    processEngineConfiguration.buildProcessEngine();

    // then
    assertThat(processEngineConfiguration.isInitializeTelemetry()).isTrue();
    assertThat(processEngineConfiguration.getManagementService().isTelemetryEnabled()).isTrue();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.persistence"}, level = "DEBUG")
  public void shouldLogTelemetryPersistenceLog() {
    // given
    boolean telemetryInitialized = true;
    processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
                              .setInitializeTelemetry(telemetryInitialized)
                              .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
 
    // when
    processEngineConfiguration.buildProcessEngine();

    // then
    assertThat(loggingRule.getFilteredLog("No telemetry property found in the database").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Creating the telemetry property in database with the value: " + telemetryInitialized).size()).isOne();
  }

  @Test
  public void shouldStartEngineWithChangedTelemetryEndpoint() {
    // given
    String telemetryEndpoint = "http://localhost:8081/pings";
    processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
        .setTelemetryEndpoint(telemetryEndpoint)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    processEngineConfiguration.buildProcessEngine();

    // then
    assertThat(processEngineConfiguration.getTelemetryEndpoint()).isEqualTo(telemetryEndpoint);
  }

}
