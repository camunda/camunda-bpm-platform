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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

@RequiredDatabase(includes = DbSqlSessionFactory.H2) // it's h2-specific test
public class TelemetryConfigurationTest {

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  protected ProcessEngineConfigurationImpl inMemoryConfiguration;

  @After
  public void reset() {
    if (inMemoryConfiguration != null) {
      ProcessEngineImpl processEngineImpl = inMemoryConfiguration.getProcessEngine();
      processEngineImpl.close();
      processEngineImpl = null;
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
    assertThat(inMemoryConfiguration.getManagementService().isTelemetryEnabled()).isFalse();

    // the telemetry reporter is always scheduled
    assertThat(inMemoryConfiguration.isTelemetryReporterActivate()).isTrue();
    assertThat(inMemoryConfiguration.getTelemetryReporter().isScheduled()).isTrue();
    assertThat(inMemoryConfiguration.getTelemetryReporter().getInitialReportingDelaySeconds()).isEqualTo(TelemetryReporter.DEFAULT_INIT_REPORT_DELAY_SECONDS);
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
        .setDbMetricsReporterActivate(false);
    ProcessEngine processEngine = inMemoryConfiguration.buildProcessEngine();
    processEngine.getManagementService().setLicenseKey(testLicenseKey);
    processEngine.close();

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then the license key is picked up
    assertThat(inMemoryConfiguration.getTelemetryRegistry().getLicenseKey())
        .isEqualToComparingFieldByField(new LicenseKeyDataImpl(null, null, null, null, null, "my company;unlimited"));

    // force clean up
    inMemoryConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.persistence"}, level = "DEBUG")
  public void shouldNotLogDefaultTelemetryValue() {
    // given
    inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());

    // when
    inMemoryConfiguration.buildProcessEngine();

    // then
    assertThat(loggingRule.getFilteredLog("Creating the telemetry property in database with the value: ").size()).isZero();
  }

}
