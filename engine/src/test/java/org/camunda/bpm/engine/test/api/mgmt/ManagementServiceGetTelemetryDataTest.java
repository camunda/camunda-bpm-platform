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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.management.Metrics.DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.FLOW_NODE_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.PROCESS_INSTANCES;

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.telemetry.ApplicationServer;
import org.camunda.bpm.engine.telemetry.Command;
import org.camunda.bpm.engine.telemetry.Metric;
import org.camunda.bpm.engine.telemetry.TelemetryData;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ManagementServiceGetTelemetryDataTest {

  protected static final String TELEMETRY_CONFIGURE_CMD_NAME = "TelemetryConfigureCmd";
  protected static final String IS_TELEMETRY_ENABLED_CMD_NAME = "IsTelemetryEnabledCmd";
  protected static final String GET_TELEMETRY_DATA_CMD_NAME = "GetTelemetryDataCmd";
  protected static final String GET_HISTORY_LEVEL_CMD_NAME = "GetHistoryLevelCmd";
  protected static final String GET_LICENSE_KEY_CMD_NAME = "GetLicenseKeyCmd";

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();


  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementServiceImpl managementService;
  protected RuntimeService runtimeService;

  protected TelemetryRegistry telemetryRegistry;
  protected MetricsRegistry metricsRegistry;

  protected TelemetryDataImpl defaultTelemetryData;
  protected TelemetryReporter defaultTelemetryReporter;

  @Before
  public void setup() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = (ManagementServiceImpl) engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();
    telemetryRegistry = configuration.getTelemetryRegistry();
    metricsRegistry = configuration.getMetricsRegistry();

    defaultTelemetryData = new TelemetryDataImpl(configuration.getTelemetryData());
    defaultTelemetryReporter = configuration.getTelemetryReporter();

    clearTelemetry();
  }

  @After
  public void tearDown() {
    if (Boolean.TRUE.equals(managementService.isTelemetryEnabled())) {
      managementService.toggleTelemetry(false);
    }

    clearTelemetry();

    configuration.setTelemetryData(defaultTelemetryData);
    configuration.setTelemetryReporter(defaultTelemetryReporter);
  }

  protected void clearTelemetry() {
    metricsRegistry.clearTelemetryMetrics();
    managementService.deleteMetrics(null);
    configuration.getTelemetryRegistry().clear();
  }

  @Test
  public void shouldReturnTelemetryData_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData).isNotNull();
    assertThat(telemetryData.getInstallation()).isNotEmpty();
  }

  @Test
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
    managementService.setLicenseKeyForTelemetry(new LicenseKeyDataImpl(null, null, null, null, null, "test license"));

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getLicenseKey().getRaw()).isEqualTo("test license");
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
  public void shouldNotResetCommandCount() {
    // given default telemetry data and empty telemetry registry
    // create command data
    managementService.isTelemetryEnabled();

    // when invoking getter twice
    managementService.getTelemetryData();
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then count should not reset
    assertThat(telemetryData.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(1);
  }

  @Test
  public void shouldStartWithMetricsCountZero() {
    // given default telemetry data and empty telemetry registry

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    Map<String, Metric> metrics = telemetryData.getProduct().getInternals().getMetrics();
    assertThat(metrics).containsOnlyKeys(FLOW_NODE_INSTANCES, PROCESS_INSTANCES, EXECUTED_DECISION_ELEMENTS, DECISION_INSTANCES);
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
  public void shouldCollectMetrics_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);

    engineRule.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertThat(telemetryData.getProduct().getInternals().getMetrics().get(PROCESS_INSTANCES).getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectMetrics_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);

    engineRule.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getMetrics().get(PROCESS_INSTANCES).getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectCommands_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);

    // trigger Command invocation
    managementService.isTelemetryEnabled();

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectCommands_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);

    // trigger Command invocation
    managementService.isTelemetryEnabled();

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(1);
  }

  @Test
  public void shouldThrowExceptionOnNullTelemetryReporter() {
    // given
    configuration.setTelemetryReporter(null);

    // when
    assertThatThrownBy(() -> managementService.getTelemetryData())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Error while retrieving telemetry data. Telemetry registry was not initialized.");
  }

  @Test
  public void shouldResetCollectedCommandsDataWhenTelemetryEnabled() {
    // given default telemetry data and empty telemetry registry

    // executed commands before telemetry is activated
    managementService.getHistoryLevel();
    managementService.getLicenseKey();

    managementService.toggleTelemetry(true);

    // execute commands after telemetry is activated
    managementService.getTelemetryData();
    managementService.getTelemetryData();
    managementService.isTelemetryEnabled();

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then command counts produced before telemetry was enabled should be deleted
    Map<String, Command> commands = telemetryData.getProduct().getInternals().getCommands();
    assertThat(commands.size()).isEqualTo(3);
    assertThat(commands.get(GET_TELEMETRY_DATA_CMD_NAME).getCount()).isEqualTo(2);
    assertThat(commands.get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(1);
    assertThat(commands.get(TELEMETRY_CONFIGURE_CMD_NAME).getCount()).isEqualTo(1);
    assertThat(commands.get(GET_HISTORY_LEVEL_CMD_NAME)).isNull();
    assertThat(commands.get(GET_LICENSE_KEY_CMD_NAME)).isNull();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldResetCollectedMetricsDataWhenTelemetryEnabled() {
    // given default telemetry data and empty telemetry registry
    // produce metrics before telemetry is enabled
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // activating telemetry
    managementService.toggleTelemetry(true);

    // produce metrics after telemetry is enabled
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then metrics produced before telemetry was enabled should be deleted
    Map<String, Metric> metrics = telemetryData.getProduct().getInternals().getMetrics();
    assertThat(metrics.size()).isEqualTo(4);
    assertThat(metrics.get(FLOW_NODE_INSTANCES).getCount()).isEqualTo(2);
    assertThat(metrics.get(PROCESS_INSTANCES).getCount()).isEqualTo(1);
    assertThat(metrics.get(DECISION_INSTANCES).getCount()).isEqualTo(0);
    assertThat(metrics.get(EXECUTED_DECISION_ELEMENTS).getCount()).isEqualTo(0);
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
    assertThat(initialTelemetryData.getProduct().getInternals().getDataCollectionStartDate()).isEqualTo(secondTelemetryData.getProduct().getInternals().getDataCollectionStartDate());
  }

  @Test
  public void shouldResetDataCollectionTimeFrameWhenTelemetryEnabled() {
    // given the collection date is set at engine startup
    Date dataCollectionStartDateBeforeToggle = managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();
    // pass at least one second between the two telemetry calls because MySQL has only second precision
    ClockUtil.offset(1000L);

    // when
    managementService.toggleTelemetry(true);

    // then
    Date dataCollectionStartDateAfterToggle = managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();

    assertThat(dataCollectionStartDateBeforeToggle).isBefore(dataCollectionStartDateAfterToggle);
  }

  @Test
  public void shouldNotResetCollectionTimeFrameAfterGetTelemetryWhenTelemetryEnabled() {
    // given default telemetry data and empty telemetry registry
    // activate telemetry
    managementService.toggleTelemetry(true);

    TelemetryData initialTelemetryData = managementService.getTelemetryData();

    // when fetching telemetry data again
    TelemetryData secondTelemetryData = managementService.getTelemetryData();

    // then the data collection time frame should not reset after the first call
    assertThat(initialTelemetryData.getProduct().getInternals().getDataCollectionStartDate()).isEqualTo(secondTelemetryData.getProduct().getInternals().getDataCollectionStartDate());
  }

  @Test
  public void shouldNotResetCollectionTimeFrameAfterToggleTelemetry() {
    // given default telemetry data and empty telemetry registry
    // telemetry activated
    managementService.toggleTelemetry(true);
    Date beforeToggleTelemetry = managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();

    // when
    managementService.toggleTelemetry(false);

    // then
    Date afterToggleTelemetry = managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();

    assertThat(beforeToggleTelemetry).isNotNull();
    assertThat(beforeToggleTelemetry).isEqualTo(afterToggleTelemetry);
  }

  @Test
  public void shouldNotResetCollectionTimeFrameOnActivateTelemetryWhenAlreadyActivated() {
    // given default telemetry data and empty telemetry registry
    // telemetry activated
    managementService.toggleTelemetry(true);
    Date beforeToggleTelemetry = managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();

    // when
    managementService.toggleTelemetry(true);
    Date afterToggleTelemetry = managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();

    // then
    assertThat(beforeToggleTelemetry).isNotNull();
    assertThat(beforeToggleTelemetry).isEqualTo(afterToggleTelemetry);
  }

}
