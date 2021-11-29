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
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.DatabaseImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.JdkImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.ProductImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.management.Metrics;
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

  protected static final String INSTALLATION_ID = "cb07ce31-c8e3-4f5f-94c2-1b28175c2022";
  protected static final String PRODUCT_NAME = "Runtime";
  protected static final String PRODUCT_VERSION = "7.14.0";
  protected static final String PRODUCT_EDITION = "special";
  protected static final String DB_VENDOR = "mySpecialDb";
  protected static final String DB_VERSION = "v.1.2.3";
  protected static final String APP_SERVER_VENDOR = "Apache Tomcat";
  protected static final String APP_SERVER_VERSION = "Apache Tomcat/10.0.1";
  protected static final String TELEMETRY_CONFIGURE_CMD_NAME = "TelemetryConfigureCmd";
  protected static final String IS_TELEMETRY_ENABLED_CMD_NAME = "IsTelemetryEnabledCmd";
  protected static final String GET_TELEMETRY_DATA_CMD_NAME = "GetTelemetryDataCmd";
  protected static final String LICENSE_CUSTOMER_NAME = "customer a";

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();


  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;
  protected RuntimeService runtimeService;

  protected TelemetryRegistry telemetryRegistry;
  protected MetricsRegistry metricsRegistry;

  protected TelemetryDataImpl defaultTelemetryData;
  protected TelemetryReporter defaultTelemetryReporter;

  @Before
  public void setup() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();
    telemetryRegistry = configuration.getTelemetryRegistry();
    metricsRegistry = configuration.getMetricsRegistry();

    clearMetrics();
    configuration.getTelemetryRegistry().clear();

    defaultTelemetryData = new TelemetryDataImpl(configuration.getTelemetryData());
    defaultTelemetryReporter = configuration.getTelemetryReporter();
  }

  @After
  public void tearDown() {
    if (Boolean.TRUE.equals(managementService.isTelemetryEnabled())) {
      managementService.toggleTelemetry(false);
    }

    clearMetrics();
    configuration.getTelemetryRegistry().clear();

    configuration.setTelemetryData(defaultTelemetryData);
    configuration.setTelemetryReporter(defaultTelemetryReporter);
  }

  protected void clearMetrics() {
    Collection<Meter> meters = configuration.getMetricsRegistry().getTelemetryMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
  }

  protected void createTestData() {
    DatabaseImpl database = new DatabaseImpl(DB_VENDOR, DB_VERSION);
    JdkImpl jdk = ParseUtil.parseJdkDetails();
    // app server and license key are set through TelemetryRegistry
    InternalsImpl internals = new InternalsImpl(database, null, null, jdk);
    internals.setCommands(createTestCommands());
    internals.setMetrics(createTestMetrics());

    ProductImpl product = new ProductImpl(PRODUCT_NAME, PRODUCT_VERSION, PRODUCT_EDITION, internals);
    TelemetryDataImpl testData = new TelemetryDataImpl(INSTALLATION_ID, product);

    configuration.setTelemetryData(testData);

    TelemetryRegistry registry = configuration.getTelemetryRegistry();
    registry.setApplicationServer(APP_SERVER_VERSION);
    registry.setLicenseKey(new LicenseKeyDataImpl(LICENSE_CUSTOMER_NAME, "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license"));
    registry.setWebapps(Stream.of("cockpit", "admin").collect(Collectors.toCollection(HashSet::new)));

    configuration.setTelemetryReporter(
        new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
            configuration.getTelemetryEndpoint(),
            configuration.getTelemetryRequestRetries(),
            configuration.getTelemetryReportingPeriod(),
            testData,
            configuration.getTelemetryHttpConnector(),
            configuration.getTelemetryRegistry(),
            configuration.getMetricsRegistry(),
            configuration.getTelemetryRequestTimeout()));
  }

  private Map<String, Metric> createTestMetrics() {
    Map<String, Metric> metrics = new HashMap<>();
    metrics.put(ROOT_PROCESS_INSTANCE_START, new MetricImpl(2));
    metrics.put(ACTIVTY_INSTANCE_START, new MetricImpl(4));
    metrics.put(EXECUTED_DECISION_ELEMENTS, new MetricImpl(8));
    metrics.put(EXECUTED_DECISION_INSTANCES, new MetricImpl(16));

    for (Entry<String, Metric> entry : metrics.entrySet()) {
      metricsRegistry.markTelemetryOccurrence(entry.getKey(), entry.getValue().getCount());
    }

    return metrics;
  }

  private Map<String, Command> createTestCommands() {
    Map<String, Command> commands = new HashMap<>();
    commands.put(GET_TELEMETRY_DATA_CMD_NAME, new CommandImpl(3));
    commands.put(IS_TELEMETRY_ENABLED_CMD_NAME, new CommandImpl(6));

    for (Entry<String, Command> entry : commands.entrySet()) {
      telemetryRegistry.markOccurrence(entry.getKey(), entry.getValue().getCount());
    }

    return commands;
  }

  @Test
  public void shouldReturnTelemetryData_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);
    createTestData();

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertTelemetryData(telemetryData, true);
  }

  @Test
  public void shouldReturnTelemetryData_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);
    createTestData();

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertTelemetryData(telemetryData, false);
  }

  @Test
  public void shouldReturnCommands() {
    // given
    TelemetryRegistry telemetryRegistry = configuration.getTelemetryRegistry();
    // create command data
    telemetryRegistry.markOccurrence(GET_TELEMETRY_DATA_CMD_NAME, 10);
    telemetryRegistry.markOccurrence(IS_TELEMETRY_ENABLED_CMD_NAME, 20);
    telemetryRegistry.markOccurrence(TELEMETRY_CONFIGURE_CMD_NAME, 30);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    Map<String, Command> commands = telemetryData.getProduct().getInternals().getCommands();
    assertThat(commands).containsOnlyKeys(GET_TELEMETRY_DATA_CMD_NAME, IS_TELEMETRY_ENABLED_CMD_NAME, TELEMETRY_CONFIGURE_CMD_NAME);
    assertThat(commands.get(GET_TELEMETRY_DATA_CMD_NAME).getCount()).isEqualTo(10);
    assertThat(commands.get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(20);
    assertThat(commands.get(TELEMETRY_CONFIGURE_CMD_NAME).getCount()).isEqualTo(30);
  }

  @Test
  public void shouldReturnMetrics() {
    // given
    configuration.initDatabaseType();
    MetricsRegistry metricsRegistry = configuration.getMetricsRegistry();
    // create metrics data
    metricsRegistry.markTelemetryOccurrence(ACTIVTY_INSTANCE_START, 5);
    metricsRegistry.markTelemetryOccurrence(ROOT_PROCESS_INSTANCE_START, 15);
    metricsRegistry.markTelemetryOccurrence(EXECUTED_DECISION_ELEMENTS, 25);
    metricsRegistry.markTelemetryOccurrence(EXECUTED_DECISION_INSTANCES, 35);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    Map<String, Metric> metrics = telemetryData.getProduct().getInternals().getMetrics();
    assertThat(metrics).containsOnlyKeys(ACTIVTY_INSTANCE_START, ROOT_PROCESS_INSTANCE_START, EXECUTED_DECISION_ELEMENTS, EXECUTED_DECISION_INSTANCES);
    assertThat(metrics.get(ACTIVTY_INSTANCE_START).getCount()).isEqualTo(5);
    assertThat(metrics.get(ROOT_PROCESS_INSTANCE_START).getCount()).isEqualTo(15);
    assertThat(metrics.get(EXECUTED_DECISION_ELEMENTS).getCount()).isEqualTo(25);
    assertThat(metrics.get(EXECUTED_DECISION_INSTANCES).getCount()).isEqualTo(35);
  }

  @Test
  public void shouldNotResetCommandCount() {
    // given
    TelemetryRegistry telemetryRegistry = configuration.getTelemetryRegistry();
    // create command data
    telemetryRegistry.markOccurrence(IS_TELEMETRY_ENABLED_CMD_NAME, 10);

    TelemetryData firstTelemetryData = managementService.getTelemetryData();

    // when
    TelemetryData secondTelemetryData = managementService.getTelemetryData();

    // then
    assertThat(firstTelemetryData.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(10);
    assertThat(secondTelemetryData.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(10);
  }

  @Test
  public void shouldNotResetMetricsCount() {
    // given
    MetricsRegistry metricsRegistry = configuration.getMetricsRegistry();
    // create command data
    metricsRegistry.markTelemetryOccurrence(ACTIVTY_INSTANCE_START, 5);

    TelemetryData firstTelemetryData = managementService.getTelemetryData();

    // when
    TelemetryData secondTelemetryData = managementService.getTelemetryData();

    // then
    assertThat(firstTelemetryData.getProduct().getInternals().getMetrics().get(ACTIVTY_INSTANCE_START).getCount()).isEqualTo(5);
    assertThat(secondTelemetryData.getProduct().getInternals().getMetrics().get(ACTIVTY_INSTANCE_START).getCount()).isEqualTo(5);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectMetrics_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);
    TelemetryData telemetryDataBeforePiStart = managementService.getTelemetryData();

    engineRule.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataBeforePiStart.getProduct().getInternals().getMetrics().get(ROOT_PROCESS_INSTANCE_START).getCount()).isEqualTo(0);
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getMetrics().get(ROOT_PROCESS_INSTANCE_START).getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectMetrics_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);
    TelemetryData telemetryDataBeforePiStart = managementService.getTelemetryData();

    engineRule.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    Metric rootPiStartMetricBeforePiStart = telemetryDataBeforePiStart.getProduct().getInternals().getMetrics().get(ROOT_PROCESS_INSTANCE_START);
    Metric rootPiStartMetricAfterPiStart = telemetryDataAfterPiStart.getProduct().getInternals().getMetrics().get(ROOT_PROCESS_INSTANCE_START);

    assertThat(rootPiStartMetricBeforePiStart.getCount()).isEqualTo(0);
    assertThat(rootPiStartMetricAfterPiStart.getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectCommands_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);

    TelemetryData telemetryDataBeforePiStart = managementService.getTelemetryData();
    // trigger Command invocation
    managementService.isTelemetryEnabled();

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataBeforePiStart.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME)).isNull();
    assertThat(telemetryDataAfterPiStart.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCollectCommands_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);
    TelemetryData telemetryDataBeforePiStart = managementService.getTelemetryData();

    // trigger Command invocation
    managementService.isTelemetryEnabled();

    // when
    TelemetryData telemetryDataAfterPiStart = managementService.getTelemetryData();

    // then
    assertThat(telemetryDataBeforePiStart.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME)).isNull();
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

    // then
    Map<String, Command> commands = telemetryData.getProduct().getInternals().getCommands();
    assertThat(commands.size()).isEqualTo(3);
    assertThat(commands.get("GetTelemetryDataCmd").getCount()).isEqualTo(2);
    assertThat(commands.get("IsTelemetryEnabledCmd").getCount()).isEqualTo(1);
    assertThat(commands.get("TelemetryConfigureCmd").getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldResetCollectedMetricsDataWhenTelemetryEnabled() {
    // given default telemetry data and empty telemetry registry
    // and some counted metrics
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // activating telemetry
    managementService.toggleTelemetry(true);

    // more collected metrics commands
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    Map<String, Metric> metrics = telemetryData.getProduct().getInternals().getMetrics();
    assertThat(metrics.size()).isEqualTo(4);
    assertThat(metrics.get(Metrics.ACTIVTY_INSTANCE_START).getCount()).isEqualTo(2);
    assertThat(metrics.get(Metrics.ROOT_PROCESS_INSTANCE_START).getCount()).isEqualTo(1);
    assertThat(metrics.get(Metrics.EXECUTED_DECISION_INSTANCES).getCount()).isEqualTo(0);
    assertThat(metrics.get(Metrics.EXECUTED_DECISION_ELEMENTS).getCount()).isEqualTo(0);
  }

  protected void assertTelemetryData(TelemetryData data, boolean telemetryEnabled) {
    assertThat(data).isNotNull();

    assertThat(data.getInstallation()).isEqualTo(INSTALLATION_ID);

    assertThat(data.getProduct().getName()).isEqualTo(PRODUCT_NAME);

    assertThat(data.getProduct().getVersion()).isEqualTo(PRODUCT_VERSION);
    assertThat(data.getProduct().getEdition()).isEqualTo(PRODUCT_EDITION);

    assertThat(data.getProduct().getInternals().getDatabase().getVendor()).isEqualTo(DB_VENDOR);
    assertThat(data.getProduct().getInternals().getDatabase().getVersion()).isEqualTo(DB_VERSION);

    assertThat(data.getProduct().getInternals().getApplicationServer().getVendor()).isEqualTo(APP_SERVER_VENDOR);
    assertThat(data.getProduct().getInternals().getApplicationServer().getVersion()).isEqualTo(APP_SERVER_VERSION);

    assertThat(data.getProduct().getInternals().getJdk().getVendor()).isNotNull();
    assertThat(data.getProduct().getInternals().getJdk().getVersion()).isNotNull();

    assertThat(data.getProduct().getInternals().getLicenseKey().getCustomer()).isEqualTo(LICENSE_CUSTOMER_NAME);

    assertThat(data.getProduct().getInternals().getCommands()).containsKeys(GET_TELEMETRY_DATA_CMD_NAME, IS_TELEMETRY_ENABLED_CMD_NAME);
    assertThat(data.getProduct().getInternals().getCommands().get(GET_TELEMETRY_DATA_CMD_NAME).getCount()).isEqualTo(3);
    assertThat(data.getProduct().getInternals().getCommands().get(IS_TELEMETRY_ENABLED_CMD_NAME).getCount()).isEqualTo(6);
    if(telemetryEnabled) {
      assertThat(data.getProduct().getInternals().getCommands()).containsKeys(TELEMETRY_CONFIGURE_CMD_NAME);
      assertThat(data.getProduct().getInternals().getCommands().get(TELEMETRY_CONFIGURE_CMD_NAME).getCount()).isEqualTo(1);
    }

    assertThat(data.getProduct().getInternals().getMetrics()).containsOnlyKeys(ROOT_PROCESS_INSTANCE_START, ACTIVTY_INSTANCE_START, EXECUTED_DECISION_ELEMENTS, EXECUTED_DECISION_INSTANCES);
    assertThat(data.getProduct().getInternals().getMetrics().get(ROOT_PROCESS_INSTANCE_START).getCount()).isEqualTo(2);
    assertThat(data.getProduct().getInternals().getMetrics().get(ACTIVTY_INSTANCE_START).getCount()).isEqualTo(4);
    assertThat(data.getProduct().getInternals().getMetrics().get(EXECUTED_DECISION_ELEMENTS).getCount()).isEqualTo(8);
    assertThat(data.getProduct().getInternals().getMetrics().get(EXECUTED_DECISION_INSTANCES).getCount()).isEqualTo(16);

    assertThat(data.getProduct().getInternals().getWebapps()).containsExactlyInAnyOrder("cockpit", "admin");
  }
}
