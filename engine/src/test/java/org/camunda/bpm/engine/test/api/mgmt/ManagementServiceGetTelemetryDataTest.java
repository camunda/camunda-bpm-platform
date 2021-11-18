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
import static org.assertj.core.api.Assertions.entry;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.DatabaseImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.JdkImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.ProductImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.telemetry.dto.Command;
import org.camunda.bpm.engine.telemetry.dto.Metric;
import org.camunda.bpm.engine.telemetry.dto.TelemetryData;
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
  protected static final CommandImpl TELEMETRY_CONFIGURE_CMD = new CommandImpl(56);
  protected static final String IS_TELEMETRY_ENABLED_CMD_NAME = "IsTelemetryEnabledCmd";
  protected static final CommandImpl IS_TELEMETRY_ENABLED_CMD = new CommandImpl(78);
  protected static final String GET_TELEMETRY_DATA_CMD_NAME = "GetTelemetryDataCmd";
  protected static final CommandImpl GET_TELEMETRY_DATA_CMD = new CommandImpl(452);
  protected static final String LICENSE_CUSTOMER_NAME = "customer a";
  protected static final MetricImpl ROOT_PROCESS_INSTANCE_START_METRIC = new MetricImpl(3);
  protected static final MetricImpl ACTIVTY_INSTANCE_START_METRIC = new MetricImpl(110);
  protected static final MetricImpl EXECUTED_DECISION_ELEMENTS_METRIC = new MetricImpl(1678);
  protected static final MetricImpl EXECUTED_DECISION_INSTANCES_METRIC = new MetricImpl(267);

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();


  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;

  protected TelemetryDataImpl defaultTelemetryData;

  @Before
  public void setup() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();

    clearMetrics();

    configuration.getTelemetryRegistry().clear();

    defaultTelemetryData = new TelemetryDataImpl(configuration.getTelemetryData());
    configuration.setTelemetryData(createTestData());
  }

  @After
  public void tearDown() {
    if (Boolean.TRUE.equals(managementService.isTelemetryEnabled())) {
      managementService.toggleTelemetry(false);
    }

    clearMetrics();

    configuration.setTelemetryData(defaultTelemetryData);
  }

  protected void clearMetrics() {
    Collection<Meter> meters = configuration.getMetricsRegistry().getDbMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
  }

  protected TelemetryDataImpl createTestData() {
    DatabaseImpl database = new DatabaseImpl(DB_VENDOR, DB_VERSION);
    JdkImpl jdk = ParseUtil.parseJdkDetails();
    InternalsImpl internals = new InternalsImpl(database, new ApplicationServerImpl(APP_SERVER_VERSION), null, jdk);
    internals.setTelemetryEnabled(true);

    LicenseKeyDataImpl licenseKey = new LicenseKeyDataImpl(LICENSE_CUSTOMER_NAME, "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license");
    internals.setLicenseKey(licenseKey);

    Map<String, Command> commands = new HashMap<>();
    commands.put(TELEMETRY_CONFIGURE_CMD_NAME, TELEMETRY_CONFIGURE_CMD);
    commands.put(IS_TELEMETRY_ENABLED_CMD_NAME, IS_TELEMETRY_ENABLED_CMD);
    commands.put(GET_TELEMETRY_DATA_CMD_NAME, GET_TELEMETRY_DATA_CMD);
    internals.setCommands(commands);

    Map<String, Metric> metrics = new HashMap<>();
    metrics.put(ACTIVTY_INSTANCE_START, ACTIVTY_INSTANCE_START_METRIC);
    metrics.put(ROOT_PROCESS_INSTANCE_START, ROOT_PROCESS_INSTANCE_START_METRIC);
    metrics.put(EXECUTED_DECISION_ELEMENTS, EXECUTED_DECISION_ELEMENTS_METRIC);
    metrics.put(EXECUTED_DECISION_INSTANCES, EXECUTED_DECISION_INSTANCES_METRIC);
    internals.setMetrics(metrics);

    internals.setWebapps(Stream.of("cockpit", "admin").collect(Collectors.toCollection(HashSet::new)));

    ProductImpl product = new ProductImpl(PRODUCT_NAME, PRODUCT_VERSION, PRODUCT_EDITION, internals);
    return new TelemetryDataImpl(INSTALLATION_ID, product);
  }

  @Test
  public void shouldReturnTelemetryData_TelemetryEnabled() {
    // given
    managementService.toggleTelemetry(true);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertTelemetryData(telemetryData);
  }

  @Test
  public void shouldReturnTelemetryData_TelemetryDisabled() {
    // given
    managementService.toggleTelemetry(false);

    // when
    TelemetryData telemetryData = managementService.getTelemetryData();

    // then
    assertTelemetryData(telemetryData);
  }

  protected void assertTelemetryData(TelemetryData data) {
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
    assertThat(data.getProduct().getInternals().isTelemetryEnabled()).isTrue();
    assertThat(data.getProduct().getInternals().getCommands()).containsExactly(
        entry(TELEMETRY_CONFIGURE_CMD_NAME, TELEMETRY_CONFIGURE_CMD),
        entry(IS_TELEMETRY_ENABLED_CMD_NAME, IS_TELEMETRY_ENABLED_CMD),
        entry(GET_TELEMETRY_DATA_CMD_NAME, GET_TELEMETRY_DATA_CMD));
    assertThat(data.getProduct().getInternals().getMetrics()).hasSize(4);
    assertThat(data.getProduct().getInternals().getMetrics()).containsExactly(
        entry(ROOT_PROCESS_INSTANCE_START, ROOT_PROCESS_INSTANCE_START_METRIC),
        entry(ACTIVTY_INSTANCE_START, ACTIVTY_INSTANCE_START_METRIC),
        entry(EXECUTED_DECISION_ELEMENTS, EXECUTED_DECISION_ELEMENTS_METRIC),
        entry(EXECUTED_DECISION_INSTANCES, EXECUTED_DECISION_INSTANCES_METRIC)
        );
    assertThat(data.getProduct().getInternals().getWebapps()).containsExactlyInAnyOrder("cockpit", "admin");
  }
}
