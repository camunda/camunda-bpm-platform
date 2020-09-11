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

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8082/pings";

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  protected ProcessEngineConfigurationImpl inMemoryConfiguration;

  @After
  public void reset() {
    if (inMemoryConfiguration != null) {
    inMemoryConfiguration.getManagementService().toggleTelemetry(false);
      ProcessEngineImpl processEngineImpl = inMemoryConfiguration.getProcessEngine();
      processEngineImpl.close();
      processEngineImpl = null;
    }
  }

  @Test
  public void shouldStartEngineTelemetryDisabled() {
    // given
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName())
        .setInitializeTelemetry(false);

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(inMemoryConfiguration.isInitializeTelemetry()).isFalse();
    assertThat(inMemoryConfiguration.getManagementService().isTelemetryEnabled()).isFalse();
    // the telemetry reporter is always scheduled
    assertThat(inMemoryConfiguration.getTelemetryReporter().isScheduled()).isTrue();
  }

  @Test
  public void shouldStartEngineWithTelemetryEnabled() {
    // given
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName())
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setInitializeTelemetry(true);

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(inMemoryConfiguration.isInitializeTelemetry()).isTrue();
    assertThat(inMemoryConfiguration.getManagementService().isTelemetryEnabled()).isTrue();
  }

  @Test
  public void shouldStartEngineWithChangedTelemetryEndpoint() {
    // given
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName())
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT);

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(inMemoryConfiguration.getTelemetryEndpoint()).isEqualTo(TELEMETRY_ENDPOINT);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.persistence"}, level = "DEBUG")
  public void shouldLogDefaultTelemetryValue() {
    // given
    Boolean telemetryInitializedValue = null;
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(loggingRule.getFilteredLog("Creating the telemetry property in database with the value: " + telemetryInitializedValue).size()).isOne();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.persistence"}, level = "DEBUG")
  public void shouldLogTelemetryPersistenceLog() {
    // given
    boolean telemetryInitialized = true;
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setInitializeTelemetry(telemetryInitialized)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(loggingRule.getFilteredLog("No telemetry property found in the database").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Creating the telemetry property in database with the value: " + telemetryInitialized).size()).isOne();
  }

}
