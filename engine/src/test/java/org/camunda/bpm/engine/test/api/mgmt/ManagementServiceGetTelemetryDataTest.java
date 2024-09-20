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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.management.Metrics.DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.FLOW_NODE_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.PROCESS_INSTANCES;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.telemetry.ApplicationServer;
import org.camunda.bpm.engine.telemetry.Metric;
import org.camunda.bpm.engine.telemetry.TelemetryData;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ManagementServiceGetTelemetryDataTest {

  protected static final String IS_TELEMETRY_ENABLED_CMD_NAME = "IsTelemetryEnabledCmd";

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementServiceImpl managementService;

  protected MetricsRegistry metricsRegistry;

  protected TelemetryDataImpl defaultTelemetryData;

  @Before
  public void setup() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = (ManagementServiceImpl) engineRule.getManagementService();
    metricsRegistry = configuration.getMetricsRegistry();

    defaultTelemetryData = new TelemetryDataImpl(configuration.getTelemetryData());

    clearTelemetry();
  }

  @After
  public void tearDown() {
    clearTelemetry();

    configuration.setTelemetryData(defaultTelemetryData);
  }

  protected void clearTelemetry() {
    metricsRegistry.clearDiagnosticsMetrics();
    managementService.deleteMetrics(null);
    configuration.getDiagnosticsRegistry().clear();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void shouldReturnTelemetryData_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData).isNotNull();
    assertThat(telemetryData.getInstallation()).isNotEmpty();
  }

  @Test
  public void shouldReturnLicenseKey() {
    // given
    managementService.setLicenseKeyForDiagnostics(new LicenseKeyDataImpl("customer a", "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license"));

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getCustomer()).isEqualTo("customer a");
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getType()).isEqualTo("UNIFIED");
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getValidUntil()).isEqualTo("2029-09-01");
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getFeatures()).isEqualTo(Collections.singletonMap("camundaBPM", "true"));
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getRaw()).isEqualTo("raw license");
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().isUnlimited()).isFalse();
  }

  @Test
  public void shouldReturnLicenseKeyRaw() {
    // given
    managementService.setLicenseKeyForDiagnostics(new LicenseKeyDataImpl(null, null, null, null, null, "test license"));

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getRaw()).isEqualTo("test license");
  }

  @Test
  public void shouldReturnProductInfo() {
    // given default configuration

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getName()).isEqualTo("Camunda BPM Runtime");
    assertThat(telemetryData.getProduct().getEdition()).isEqualTo("community");
    assertThat(telemetryData.getProduct().getVersion()).isEqualTo(ParseUtil.parseProcessEngineVersion(true).getVersion());
  }

  @Test
  public void shouldReturnDatabaseInfo() {
    // given default configuration

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getDatabase().getVendor())
        .isEqualTo(engineRule.getProcessEngineConfiguration().getDatabaseVendor());
    assertThat(telemetryData.getProduct().getInternals().getDatabase().getVersion())
        .isEqualTo(engineRule.getProcessEngineConfiguration().getDatabaseVersion());
  }

  @Test
  public void shouldReturnJDKInfo() {
    // given default configuration

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getJdk().getVendor())
        .isEqualTo(ParseUtil.parseJdkDetails().getVendor());
    assertThat(telemetryData.getProduct().getInternals().getJdk().getVersion())
        .isEqualTo(ParseUtil.parseJdkDetails().getVersion());
  }

  @Test
  public void shouldReturnWebapps() {
    // given
    managementService.addWebappToTelemetry("cockpit");
    managementService.addWebappToTelemetry("admin");

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getWebapps()).containsExactlyInAnyOrder("cockpit", "admin");
  }

  @Test
  public void shouldReturnApplicationServerInfo() {
    // given
    managementService.addApplicationServerInfoToTelemetry("Apache Tomcat/10.0.1");

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    ApplicationServer applicationServer = telemetryData.getProduct().getInternals().getApplicationServer();
    assertThat(applicationServer.getVendor()).isEqualTo("Apache Tomcat");
    assertThat(applicationServer.getVersion()).isEqualTo("Apache Tomcat/10.0.1");
  }

  @Test
  public void shouldStartWithCommandCountZero() {
    // given default telemetry data and empty telemetry registry

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getCommands()).isEmpty();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void shouldNotResetCommandCount() {
    // given default telemetry data and empty telemetry registry
    // create command data
    managementService.isTelemetryEnabled();

    // when invoking getter twice
    managementService.getTelemetryData();
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then count should not reset
    assertThat(telemetryData.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount())
        .isEqualTo(1);
  }

  @Test
  public void shouldStartWithMetricsCountZero() {
    // given default telemetry data and empty telemetry registry

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    Map<String, Metric> metrics = telemetryData.getProduct().getInternals().getMetrics();
    assertThat(metrics).containsOnlyKeys(FLOW_NODE_INSTANCES, PROCESS_INSTANCES, EXECUTED_DECISION_ELEMENTS,
        DECISION_INSTANCES);
    assertThat(metrics.get(FLOW_NODE_INSTANCES).getCount()).isZero();
    assertThat(metrics.get(PROCESS_INSTANCES).getCount()).isZero();
    assertThat(metrics.get(EXECUTED_DECISION_ELEMENTS).getCount()).isZero();
    assertThat(metrics.get(DECISION_INSTANCES).getCount()).isZero();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldNotResetMetricsCount() {
    // given default telemetry data and empty telemetry registry
    // create metrics data
    engineRule.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // when invoking getter twice
    managementService.getTelemetryData();
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then count should not reset
    assertThat(telemetryData.getProduct().getInternals().getMetrics().get(FLOW_NODE_INSTANCES).getCount()).isEqualTo(2);
    assertThat(telemetryData.getProduct().getInternals().getMetrics().get(PROCESS_INSTANCES).getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectMetrics_TelemetryDisabled() {
    // given default configuration

    engineRule.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getMetrics().get(PROCESS_INSTANCES).getCount())
        .isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @SuppressWarnings("deprecation")
  public void shouldCollectCommands_TelemetryDisabled() {
    // given default configuration

    // trigger Command invocation
    managementService.isTelemetryEnabled();

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME)
        .getCount()).isEqualTo(1);
  }

  @Test
  public void shouldSetDataCollectionTimeFrameToEngineStartTimeWhenTelemetryDisabled() {
    // given default telemetry data and empty telemetry registry
    // current time after engine startup but before fetching telemetry data
    Date beforeGetTelemetry = ClockUtil.getCurrentTime();
    // move clock by one second to pass some time before fetching telemetry
    ClockUtil.offset(1000L);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getDataCollectionStartDate()).isBefore(beforeGetTelemetry);
  }

  @Test
  public void shouldNotResetCollectionTimeFrameAfterGetTelemetryWhenTelemetryDisabled() {
    // given default telemetry data and empty telemetry registry
    TelemetryData initialTelemetryData = managementService.getTelemetryData();

    // when fetching telemetry data again
    TelemetryData secondTelemetryData = managementService.getTelemetryData();

    // then the data collection time frame should not reset after the first call
    assertThat(initialTelemetryData.getProduct().getInternals().getDataCollectionStartDate())
        .isEqualTo(secondTelemetryData.getProduct().getInternals().getDataCollectionStartDate());
  }

  @Test
  public void shouldNotResetCollectionTimeFrameAfterGetTelemetry() {
    // given default telemetry data and empty telemetry registry
    // and default configuration

    TelemetryData initialTelemetryData = managementService.getTelemetryData();

    // when fetching telemetry data again
    TelemetryData secondTelemetryData = managementService.getTelemetryData();

    // then the data collection time frame should not reset after the first call
    assertThat(initialTelemetryData.getProduct().getInternals().getDataCollectionStartDate())
        .isEqualTo(secondTelemetryData.getProduct().getInternals().getDataCollectionStartDate());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void shouldNotResetCollectionTimeFrameAfterToggleTelemetry() {
    // given default telemetry data and empty telemetry registry
    // and default configuration
    Date beforeToggleTelemetry = managementService.getTelemetryData().getProduct().getInternals()
        .getDataCollectionStartDate();

    // when
    managementService.toggleTelemetry(false);

    // then
    Date afterToggleTelemetry = managementService.getTelemetryData().getProduct().getInternals()
        .getDataCollectionStartDate();

    assertThat(beforeToggleTelemetry).isNotNull();
    assertThat(beforeToggleTelemetry).isEqualTo(afterToggleTelemetry);
  }

}
