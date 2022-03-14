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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class TelemetryConfigurationTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8086/pings";

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  protected ProcessEngineConfigurationImpl inMemoryConfiguration;

  WireMockServer wireMockServer;

  @After
  public void reset() {
    if (inMemoryConfiguration != null) {
      if (Boolean.TRUE.equals(inMemoryConfiguration.getManagementService().isTelemetryEnabled())) {
        inMemoryConfiguration.getManagementService().toggleTelemetry(false);
      }
      ProcessEngineImpl processEngineImpl = inMemoryConfiguration.getProcessEngine();
      processEngineImpl.close();
      processEngineImpl = null;
    }

    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void shouldStartEngineWithTelemetryDefaults() {
    // given
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(inMemoryConfiguration.isInitializeTelemetry()).isNull();
    assertThat(inMemoryConfiguration.getManagementService().isTelemetryEnabled()).isNull();

    // the telemetry reporter is always scheduled
    assertThat(inMemoryConfiguration.isTelemetryReporterActivate()).isTrue();
    assertThat(inMemoryConfiguration.getTelemetryReporter().isScheduled()).isTrue();
    assertThat(inMemoryConfiguration.getTelemetryReporter().getInitialReportingDelaySeconds()).isEqualTo(TelemetryReporter.EXTENDED_INIT_REPORT_DELAY_SECONDS);
  }

  @Test
  public void shouldStartEngineWithTelemetryDisabled() {
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
    assertThat(inMemoryConfiguration.isTelemetryReporterActivate()).isTrue();
    assertThat(inMemoryConfiguration.getTelemetryReporter().isScheduled()).isTrue();
    assertThat(inMemoryConfiguration.getTelemetryReporter().getInitialReportingDelaySeconds()).isEqualTo(TelemetryReporter.DEFAULT_INIT_REPORT_DELAY_SECONDS);
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
    assertThat(inMemoryConfiguration.getTelemetryReporter().getInitialReportingDelaySeconds()).isEqualTo(TelemetryReporter.DEFAULT_INIT_REPORT_DELAY_SECONDS);
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
  public void shouldStartEngineWithTelemetryEnabledAndLicenseKeyAlreadyPresent() {
    // given license key persisted
    String testLicenseKey = "signature=;my company;unlimited";
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda-test" + getClass().getSimpleName())
        // keep data alive at process engine close
        .setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE)
        .setInitializeTelemetry(false)
        .setDbMetricsReporterActivate(false);
    ProcessEngine processEngine = inMemoryConfiguration.buildProcessEngine();
    processEngine.getManagementService().setLicenseKey(testLicenseKey);
    processEngine.close();

    // when an engine with telemetry is started
    inMemoryConfiguration
        .setInitializeTelemetry(true)
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT);
    inMemoryConfiguration.buildProcessEngine();

    // then the license key is picked up
    assertThat(inMemoryConfiguration.getTelemetryRegistry().getLicenseKey())
        .isEqualToComparingFieldByField(new LicenseKeyDataImpl(null, null, null, null, null, "my company;unlimited"));

    // force clean up
    inMemoryConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
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

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "INFO")
  public void shouldThrowAnException() {
    // given
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8085));
    wireMockServer.start();
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName())
        .setInitializeTelemetry(true)
        .setTelemetryRequestRetries(0)
        .setTelemetryRequestTimeout(1)
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT);
    inMemoryConfiguration.buildProcessEngine();
    wireMockServer.stubFor(post(urlEqualTo("/pings"))
        .willReturn(aResponse().withStatus(202)));

    // when
    inMemoryConfiguration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule
        .getFilteredLog("Could not send telemetry data. Reason: "
            + "ConnectorRequestException with message 'HTCL-02007 Unable to execute HTTP request'")
        .size())
        .isOne();
  }

}
