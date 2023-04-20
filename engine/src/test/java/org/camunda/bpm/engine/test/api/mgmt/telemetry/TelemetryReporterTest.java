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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.FLOW_NODE_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.PROCESS_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.util.MetricsUtil;
import org.camunda.bpm.engine.impl.telemetry.CamundaIntegration;
import org.camunda.bpm.engine.impl.telemetry.PlatformTelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.DatabaseImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.JdkImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.ProductImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.telemetry.Command;
import org.camunda.bpm.engine.telemetry.Metric;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class TelemetryReporterTest {

  protected static final String DEPLOY_CMD = "DeployCmd";
  protected static final String GET_NEXT_ID_BLOCK_CMD = "GetNextIdBlockCmd";
  protected static final String BOOTSTRAP_ENGINE_CMD = "BootstrapEngineCommand";
  protected static final String START_PROCESS_INSTANCE_CMD = "StartProcessInstanceCmd";
  protected static final String GET_LICENSE_KEY_CMD = "GetLicenseKeyCmd";
  protected static final String GET_HISTORY_LEVEL_CMD = "GetHistoryLevelCmd";
  protected static final String IS_TELEMETRY_ENABLED_CMD = "IsTelemetryEnabledCmd";
  protected static final String TELEMETRY_CONFIGURE_CMD = "TelemetryConfigureCmd";
  protected static final String GET_TELEMETRY_DATA_CMD = "GetTelemetryDataCmd";

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8084/pings";
  protected static final String TELEMETRY_ENDPOINT_PATH = "/pings";
  protected static final String VALID_UUID_V4 = "cb07ce31-c8e3-4f5f-94c2-1b28175c2022";

  public static String DMN_FILE = "org/camunda/bpm/engine/test/api/mgmt/metrics/ExecutedDecisionElementsTest.dmn11.xml";
  public static VariableMap VARIABLES = Variables.createVariables().putValue("status", "").putValue("sum", 100);


  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(configuration ->
          configuration
            .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
            .setTelemetryReporterActivate(true)
      );

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8084);

  protected ProcessEngine standaloneProcessEngine;
  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected IdentityService identityService;
  protected TelemetryReporter standaloneReporter;

  protected TelemetryDataImpl defaultTelemetryData;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = configuration.getManagementService();
    runtimeService = configuration.getRuntimeService();
    taskService = configuration.getTaskService();
    identityService = configuration.getIdentityService();

    DefaultDmnEngineConfiguration dmnEngineConfiguration = configuration
        .getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();

    clearMetrics();

    // clean up the registry data
    configuration.getTelemetryRegistry().clear();

    defaultTelemetryData = new TelemetryDataImpl(configuration.getTelemetryData());
  }

  @After
  public void tearDown() {
    identityService.clearAuthentication();
    ClockUtil.resetClock();

    if (Boolean.TRUE.equals(managementService.isTelemetryEnabled())) {
      managementService.toggleTelemetry(false);
    }

    clearMetrics();

    if (standaloneReporter != null) {
      standaloneReporter.stop(false);
      standaloneReporter = null;
    }
    if (standaloneProcessEngine != null) {
      if (Boolean.TRUE.equals(standaloneProcessEngine.getManagementService().isTelemetryEnabled())) {
        standaloneProcessEngine.getManagementService().toggleTelemetry(false);
      }
      standaloneProcessEngine.close();
    }

    DefaultDmnEngineConfiguration dmnEngineConfiguration = configuration
        .getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();

    WireMock.resetAllRequests();

    configuration.setTelemetryData(defaultTelemetryData);
  }

  protected void clearMetrics() {
    Collection<Meter> meters = configuration.getMetricsRegistry().getDbMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
  }

  @Test
  public void shouldSendTelemetry() {
    // given
    managementService.toggleTelemetry(true);
    TelemetryDataImpl data = createDataToSend();
    String requestBody = toJson(data);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    standaloneReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                0,
                                                                1000,
                                                                data,
                                                                configuration.getTelemetryHttpConnector(),
                                                                configuration.getTelemetryRegistry(),
                                                                configuration.getMetricsRegistry(),
                                                                configuration.getTelemetryRequestTimeout());

    // when
    standaloneReporter.reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, true, true))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldReportDataWhenTelemetryInitialized() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setInitializeTelemetry(true)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
    standaloneProcessEngine = processEngineConfiguration.buildProcessEngine();

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenProductNameIsNull() {
    executeDataValidationTest(null, "7.15.0", "community", VALID_UUID_V4);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenProductNameIsEmpty() {
    executeDataValidationTest("", "7.15.0", "community", VALID_UUID_V4);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenProductVersionIsNull() {
    executeDataValidationTest("Runtime", null, "community", VALID_UUID_V4);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenProductVersionIsEmpty() {
    executeDataValidationTest("Runtime", "", "community", VALID_UUID_V4);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenProductEditionIsNull() {
    executeDataValidationTest("Runtime", "7.15.0", null, VALID_UUID_V4);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenProductEditionIsEmpty() {
    executeDataValidationTest("Runtime", "7.15.0", "", VALID_UUID_V4);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenInstallationIdIsNull() {
    executeDataValidationTest("Runtime", "7.15.0", "community", null);
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenInstallationIdIsEmpty() {
    executeDataValidationTest("Runtime", "7.15.0", "community", "");
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotReportWhenInstallationIdIsInvalid() {
    String invalidUUID = "f5b19e2e-b49a-11ea-b3de-0242ac130004";
    executeDataValidationTest("Runtime", "7.15.0", "community", invalidUUID);
  }

  @Test
  public void shouldNotReportWhenReporterDeactivated() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setTelemetryReporterActivate(false)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
    standaloneProcessEngine = processEngineConfiguration.buildProcessEngine();

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(0, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));
  }

  @Test
  public void shouldSendCompleteTelemetryData() {
    // given
    String applicationServerVersion = "Tomcat 10";
    PlatformTelemetryRegistry.setApplicationServer(applicationServerVersion);
    LicenseKeyDataImpl licenseKey = new LicenseKeyDataImpl("customer a", "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license");

    ProcessEngineConfigurationImpl configuration = createEngine(true);

    configuration.getRepositoryService().createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml").deploy();

    configuration.getTelemetryRegistry().setLicenseKey(licenseKey);
    configuration.getTelemetryData().getProduct().getInternals().getCamundaIntegration().add(CamundaIntegration.SPRING_BOOT_STARTER);
    ((ManagementServiceImpl) configuration.getManagementService()).addWebappToTelemetry("cockpit");
    ((ManagementServiceImpl) configuration.getManagementService()).addWebappToTelemetry("admin");
    // produce some data
    configuration.getManagementService().toggleTelemetry(true);
    configuration.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

    // setup
    TelemetryDataImpl initialTelemetryData = (TelemetryDataImpl) configuration.getManagementService().getTelemetryData();
    TelemetryDataImpl expectedData = simpleData(b -> {
      b.installation(initialTelemetryData.getInstallation());

      ProductImpl product = initialTelemetryData.getProduct();
      b.productName("Camunda BPM Runtime");
      b.productVersion(product.getVersion());
      b.productEdition("community");

      InternalsImpl internals = product.getInternals();
      b.database(internals.getDatabase().getVendor(), internals.getDatabase().getVersion());
      b.applicationServer(applicationServerVersion);
      b.licenseKey(licenseKey);
      b.addIntegration(CamundaIntegration.SPRING_BOOT_STARTER);
      b.dataCollectionStartDate(internals.getDataCollectionStartDate());

      b.countCommand(IS_TELEMETRY_ENABLED_CMD, 2);
      b.countCommand(TELEMETRY_CONFIGURE_CMD, 1);
      b.countCommand(GET_TELEMETRY_DATA_CMD, 1);
      b.countCommand(GET_NEXT_ID_BLOCK_CMD, 1);
      b.countCommand(START_PROCESS_INSTANCE_CMD, 1);
      b.countCommand(DEPLOY_CMD, 1);
      b.countCommand(GET_LICENSE_KEY_CMD, 1);
      b.countCommand(BOOTSTRAP_ENGINE_CMD, 1);

      b.countMetric(ROOT_PROCESS_INSTANCE_START, 1);
      b.countMetric(ACTIVTY_INSTANCE_START, 2);
      b.countMetric(EXECUTED_DECISION_ELEMENTS, 0);
      b.countMetric(FLOW_NODE_INSTANCES, 2);
      b.countMetric(EXECUTED_DECISION_INSTANCES, 0);
      b.countMetric(DECISION_INSTANCES, 0);
      b.countMetric(PROCESS_INSTANCES, 1);

      b.webapps("cockpit", "admin");
      b.jdk(internals.getJdk());
      b.telemetryEnabled(true);
    });

    // use JsonUtil to get the full JSON (not ignoring null values)
    String requestBody = JsonUtil.asString(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, false))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithApplicationServerInfo() {
    // given default telemetry data (no application server)
    managementService.toggleTelemetry(true);
    // set application server after initialization
    String applicationServerVersion = "Tomcat 10";
    PlatformTelemetryRegistry.setApplicationServer(applicationServerVersion);

    TelemetryDataImpl expectedData = simpleData(b -> b.applicationServer(applicationServerVersion));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithApplicationServerInfoWhenSentBeforeInitialization() {
    // given
    String applicationServerVersion = "Tomcat 10";
    PlatformTelemetryRegistry.setApplicationServer(applicationServerVersion);
    ProcessEngineConfigurationImpl configuration = createEngine(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    TelemetryDataImpl expectedData = simpleData( b -> b.applicationServer(applicationServerVersion));

    String requestBody = toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithLicenseInfo() {
    // given default telemetry data (no license key)
    managementService.toggleTelemetry(true);
    // set key after initialization
    LicenseKeyDataImpl licenseKey = new LicenseKeyDataImpl("customer a", "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license");
    configuration.getTelemetryRegistry().setLicenseKey(licenseKey);

    TelemetryDataImpl expectedData = simpleData(b-> b.licenseKey(licenseKey));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, true, true))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithOverriddenLicenseInfo() {
    // given default telemetry data (no license key)
    managementService.toggleTelemetry(true);
    // set key after initialization
    LicenseKeyDataImpl firstLicenseKey = new LicenseKeyDataImpl("customer a", "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license");
    configuration.getTelemetryRegistry().setLicenseKey(firstLicenseKey);

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
    // report once
    configuration.getTelemetryReporter().reportNow();
    configuration.getTelemetryRegistry().getCommands().clear();

    // change license key
    LicenseKeyDataImpl secondLicenseKey = new LicenseKeyDataImpl("customer b", "UNIFIED", "2029-08-01", false, Collections.singletonMap("cawemo", "true"), "new raw license");
    configuration.getTelemetryRegistry().setLicenseKey(secondLicenseKey);

    TelemetryDataImpl expectedData = simpleData(b-> b.licenseKey(secondLicenseKey));

    String requestBody = toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, true, true))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithRawLicenseInfoOnly() {
    // given default telemetry data (no license key)
    managementService.toggleTelemetry(true);
    // set key after initialization via management service
    String licenseKeyRaw = "raw license";
    managementService.setLicenseKey(licenseKeyRaw);

    TelemetryDataImpl expectedData = simpleData(b -> b.licenseKeyRaw(licenseKeyRaw));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, true, true))
              .withHeader("Content-Type",  equalTo("application/json")));

    // cleanup
    managementService.deleteLicenseKey();
  }

  @Test
  public void shouldSendTelemetryWithCommandCounts() {
    // given default telemetry data and empty telemetry registry
    managementService.toggleTelemetry(true);

    // execute commands
    managementService.getHistoryLevel();
    managementService.getLicenseKey();

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countCommand(GET_HISTORY_LEVEL_CMD, 1)
          .countCommand(GET_LICENSE_KEY_CMD, 1));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void shouldSendTelemetryWithRooProcessInstanceMetrics() {
    // given
    managementService.toggleTelemetry(true);

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }
    configuration.getDbMetricsReporter().reportNow();

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countMetric(ROOT_PROCESS_INSTANCE_START, 3)
          .countMetric(EXECUTED_DECISION_ELEMENTS, 0)
          .countMetric(EXECUTED_DECISION_INSTANCES, 0)
          .countMetric(ACTIVTY_INSTANCE_START, 6));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void shouldNotSendMetricsTwice() {
    // given
    managementService.toggleTelemetry(true);

    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }
    configuration.getDbMetricsReporter().reportNow();

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countMetric(ROOT_PROCESS_INSTANCE_START, 0)
          .countMetric(EXECUTED_DECISION_ELEMENTS, 0)
          .countMetric(EXECUTED_DECISION_INSTANCES, 0)
          .countMetric(ACTIVTY_INSTANCE_START, 0));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    configuration.getTelemetryReporter().reportNow();

    // when sending telemetry again
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionOkay.dmn11.xml" })
  public void shouldSendTelemetryWithExecutedDecisionInstanceMetrics() {
    // given
    managementService.toggleTelemetry(true);
    for (int i = 0; i < 2; i++) {
      runtimeService.startProcessInstanceByKey("testProcess");
    }

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countMetric(ROOT_PROCESS_INSTANCE_START, 2)
          .countMetric(EXECUTED_DECISION_ELEMENTS, 2)
          .countMetric(EXECUTED_DECISION_INSTANCES, 2)
          .countMetric(ACTIVTY_INSTANCE_START, 4)
          );

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/mgmt/metrics/ExecutedDecisionElementsTest.dmn11.xml" })
  public void shouldSendTelemetryWithExecutedDecisionElementMetrics() {
    // given
    BpmnModelInstance modelInstance = createProcessWithBusinessRuleTask("testProcess", "decision");

    testRule.deploy(configuration.getRepositoryService().createDeployment()
        .addModelInstance("process.bpmn", modelInstance)
        .addClasspathResource(DMN_FILE));
    managementService.toggleTelemetry(true);
    runtimeService.startProcessInstanceByKey("testProcess", VARIABLES);

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countMetric(ROOT_PROCESS_INSTANCE_START, 1)
          .countMetric(EXECUTED_DECISION_ELEMENTS, 16)
          .countMetric(EXECUTED_DECISION_INSTANCES, 1)
          .countMetric(ACTIVTY_INSTANCE_START, 3));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldSendTelemetryWithActivityInstanceMetrics() {
    // given
    managementService.toggleTelemetry(true);
    for (int i = 0; i < 4; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
      String taskId = taskService.createTaskQuery().singleResult().getId();
      taskService.complete(taskId);
    }

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countMetric(ROOT_PROCESS_INSTANCE_START, 4)
          .countMetric(EXECUTED_DECISION_ELEMENTS, 0)
          .countMetric(EXECUTED_DECISION_INSTANCES, 0)
          .countMetric(ACTIVTY_INSTANCE_START, 12));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldAddJdkInfoToTelemetryData() {
    // given
    TelemetryDataImpl telemetryData = configuration.getTelemetryData();

    // then
    JdkImpl jdkInfo = telemetryData.getProduct().getInternals().getJdk();
    assertThat(jdkInfo).isNotNull();
    JdkImpl expectedJdkInfo = ParseUtil.parseJdkDetails();
    assertThat(jdkInfo.getVersion()).isEqualTo(expectedJdkInfo.getVersion());
    assertThat(jdkInfo.getVendor()).isEqualTo(expectedJdkInfo.getVendor());
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogTelemetrySent() {
    // given
    managementService.toggleTelemetry(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Start telemetry sending task").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Sending telemetry data").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Telemetry request was successful.").size()).isOne();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogUnexpectedResponse() {
    // given
    managementService.toggleTelemetry(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_NOT_ACCEPTABLE)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule
        .getFilteredLog(
            "Unexpected response code " + HttpURLConnection.HTTP_NOT_ACCEPTABLE + " when sending telemetry data" )
        .size()).isEqualTo(3);
  }

  @Test
  public void shouldNotSendTelemetryWhenDisabled() {
    // given
    managementService.toggleTelemetry(false);
    TelemetryReporter telemetryReporter = configuration.getTelemetryReporter();

    // when
    telemetryReporter.reportNow();

    // then
    verify(0, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldNotSendTelemetryDataCollectedBeforeTelemetryEnabled() {
    // given
    TelemetryReporter telemetryReporter = configuration.getTelemetryReporter();

    // some executed commands
    managementService.isTelemetryEnabled();
    managementService.getHistoryLevel();
    managementService.getHistoryLevel();
    managementService.getHistoryLevel();

    // and collected metrics
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // enable telemetry
    managementService.toggleTelemetry(true);

    // execute another command and create another metric
    managementService.getHistoryLevel();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // set up stub
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    telemetryReporter.reportNow();

    // then
    TelemetryDataImpl expectedData = simpleData(
        b -> b.countCommand(GET_HISTORY_LEVEL_CMD, 1)
          .countMetric(Metrics.ROOT_PROCESS_INSTANCE_START, 1));

    String expectedBody = toJson(expectedData);

    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogTelemetryDisabled() {
    // given default configuration
    managementService.toggleTelemetry(false);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Sending telemetry is disabled").size()).isOne();
  }

  @Test
  public void shouldKeepReporterRunningAfterTelemetryIsDisabled() {
    // when
    managementService.toggleTelemetry(false);

    // then
    TelemetryReporter telemetryReporter = configuration.getTelemetryReporter();
    assertThat(telemetryReporter.isScheduled()).isTrue();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogErrorOnDebugWhenHttpConnectorNotInitialized() {
    // given
    managementService.toggleTelemetry(true);
    TelemetryDataImpl data = createDataToSend();
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    standaloneReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                0,
                                                                1000,
                                                                data,
                                                                null,
                                                                configuration.getTelemetryRegistry(),
                                                                configuration.getMetricsRegistry(),
                                                                configuration.getTelemetryRequestTimeout());

    // when
    standaloneReporter.reportNow();

    // then
    List<ILoggingEvent> warningLog = loggingRule.getFilteredLog("Could not send telemetry data. ");
    assertThat(warningLog.size()).isOne();
    assertThat(warningLog.get(0).toString()).contains("Set this logger to DEBUG/FINE for the full stacktrace.");
    assertThat(loggingRule.getFilteredLog("java.lang.NullPointerException occurred while sending telemetry data.").size()).isOne();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldRecordUserOperationLog() {
    // given
    configuration.getIdentityService().setAuthenticatedUserId("admin");

    // when
    managementService.toggleTelemetry(true);

    // then
    UserOperationLogEntry entry = configuration.getHistoryService().createUserOperationLogQuery().singleResult();
    assertThat(entry.getEntityType()).isEqualTo(EntityTypes.PROPERTY);
    assertThat(entry.getCategory()).isEqualTo(UserOperationLogEntry.CATEGORY_ADMIN);
    assertThat(entry.getOperationType()).isEqualTo( UserOperationLogEntry.OPERATION_TYPE_UPDATE);
    assertThat(entry.getProperty()).isEqualTo("name");
    assertThat(entry.getOrgValue()).isNull();
    assertThat(entry.getNewValue()).isEqualTo("camunda.telemetry.enabled");
  }

  @Test
  public void shouldMakeRetriesOnNonSuccessStatus() {
    // given
    managementService.toggleTelemetry(true);

    // execute commands
    managementService.getHistoryLevel();
    managementService.getLicenseKey();

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countCommand(GET_HISTORY_LEVEL_CMD, 1)
          .countCommand(GET_LICENSE_KEY_CMD, 1));

    String expectedRequestBody = toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    // the request is made and the data is not reset between requests
    verify(3, postRequestedFor(
        urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedRequestBody, true, true))
        );
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldNotMakeRetriesOnUnexpectedSuccessStatus() {
    // given
    managementService.toggleTelemetry(true);

    // execute commands
    managementService.getHistoryLevel();
    managementService.getLicenseKey();

    // ET is supposed to return 202
    // Another success response code should not lead to a retry but should be logged as an oddity
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_NO_CONTENT)));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countCommand(GET_HISTORY_LEVEL_CMD, 1)
          .countCommand(GET_LICENSE_KEY_CMD, 1));

    String expectedRequestBody = toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    // the request is made and the data is not reset between requests
    verify(1, postRequestedFor(
        urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedRequestBody, true, true))
        );

    assertThat(loggingRule.getFilteredLog("Telemetry request was sent, "
        + "but received an unexpected response success code: 204").size()).isOne();
  }

  @Test
  public void shouldMakeRetriesOnRequestFailure() {
    // given
    managementService.toggleTelemetry(true);

    // execute commands
    managementService.getHistoryLevel();
    managementService.getLicenseKey();

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    TelemetryDataImpl expectedData = simpleData(
        b -> b.countCommand(GET_HISTORY_LEVEL_CMD, 1)
          .countCommand(GET_LICENSE_KEY_CMD, 1));

    String expectedRequestBody = toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // the request is made and the data is not reset between requests
    verify(3, postRequestedFor(
        urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedRequestBody, true, true))
        );
  }

  @Test
  public void shouldSendTelemetryWhenDbMetricsDisabled() {
    // given
    boolean telemetryInitialized = true;
    StandaloneInMemProcessEngineConfiguration inMemoryConfiguration = new StandaloneInMemProcessEngineConfiguration();
    inMemoryConfiguration
        .setInitializeTelemetry(telemetryInitialized)
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setMetricsEnabled(false)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    standaloneProcessEngine = inMemoryConfiguration.buildProcessEngine();

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NO_CONTENT)));

    // when
    inMemoryConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendDataWithCamundaIntegration() {
    // given
    TelemetryDataImpl expectedData = simpleData(
        b -> b.addIntegration("wildfly-integration"));

    // creating a new object as during the report the object is being modified
    TelemetryDataImpl givenData = extendData(getTelemetryDataIgnoreCollectionTimeFrame(),
        b -> b.addIntegration("wildfly-integration"));
    managementService.toggleTelemetry(true);

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    // using a separate reporter to avoid modifying the telemetry data of other tests
    new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                          configuration.getTelemetryEndpoint(),
                          0,
                          configuration.getTelemetryReportingPeriod(),
                          givenData,
                          configuration.getTelemetryHttpConnector(),
                          configuration.getTelemetryRegistry(),
                          configuration.getMetricsRegistry(),
                          configuration.getTelemetryRequestTimeout())
        .reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendDataWithWebapps() {
    // given default telemetry data (no webapp data)
    managementService.toggleTelemetry(true);
    // set webapps after initialization
    Set<String> webapps = new HashSet<>(Arrays.asList("cockpit", "admin"));
    configuration.getTelemetryRegistry().setWebapps(webapps);

    TelemetryDataImpl expectedData = simpleData(
        b ->  b.webapps("cockpit", "admin"));

    String requestBody = toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, true, true))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendDataWithDataCollectionTimeFrame() {
    // given default telemetry data
    managementService.toggleTelemetry(true);

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    //then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        // we can only check that the property is included in the response i.e., it is not null
        .withRequestBody(WireMock.matchingJsonPath("$.product.internals.data-collection-start-date"))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldResetDataCollectionTimeFrameAfterSending() {
    // given default telemetry data
    managementService.toggleTelemetry(true);

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // we need to create this object before the reporter fires because the data collection time frame will be reset
    Date telemetryDataTimeFrameBeforeReported = configuration.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate();

    // when
    configuration.getTelemetryReporter().reportNow();

    //then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withHeader("Content-Type",  equalTo("application/json")));
    assertThat(managementService.getTelemetryData().getProduct().getInternals().getDataCollectionStartDate())
      .isAfter(telemetryDataTimeFrameBeforeReported);
  }


  // HELPER METHODS

  protected ProcessEngineConfigurationImpl createEngine(Boolean initTelemetry) {
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    buildEngine(processEngineConfiguration, initTelemetry);
    return processEngineConfiguration;
  }

  protected void buildEngine(ProcessEngineConfigurationImpl processEngineConfiguration, Boolean initTelemetry) {
    processEngineConfiguration
        .setProcessEngineName("standalone")
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    if (initTelemetry != null) {
      processEngineConfiguration.setInitializeTelemetry(initTelemetry);
    }
    standaloneProcessEngine = processEngineConfiguration.buildProcessEngine();
  }

  protected TelemetryDataImpl createDataToSend() {
    return createDataToSendWithCustomValues("Runtime", "7.14.0", "special", VALID_UUID_V4);
  }

  protected TelemetryDataImpl createDataToSendWithCustomValues(String name, String version, String edition, String installationId) {
    DatabaseImpl database = new DatabaseImpl("mySpecialDb", "v.1.2.3");
    JdkImpl jdk = ParseUtil.parseJdkDetails();
    InternalsImpl internals = new InternalsImpl(database, new ApplicationServerImpl("Apache Tomcat/10.0.1"), null, jdk);
    internals.setTelemetryEnabled(true);

    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);

    Map<String, Metric> metrics = new HashMap<>();
    internals.setMetrics(metrics);

    ProductImpl product = new ProductImpl(name, version, edition, internals);
    TelemetryDataImpl data = new TelemetryDataImpl(installationId, product);
    return data;
  }

  protected TelemetryDataImpl extendData(TelemetryDataImpl telemetryData, Consumer<TelemetryDataBuilder> configuration) {
    TelemetryDataBuilder builder = new TelemetryDataBuilder(telemetryData);

    configuration.accept(builder);

    return builder.data;
  }

  protected TelemetryDataImpl simpleData(Consumer<TelemetryDataBuilder> configuration) {
    TelemetryDataBuilder builder = new TelemetryDataBuilder();

    configuration.accept(builder);

    return builder.data;
  }

  protected static class TelemetryDataBuilder {
    protected TelemetryDataImpl data;

    // Constructors
    public TelemetryDataBuilder() {
      data = new TelemetryDataImpl(null, new ProductImpl(null, null, null, new InternalsImpl(null, null, null, null)));
    }

    public TelemetryDataBuilder(TelemetryDataImpl initialValues) {
      data = new TelemetryDataImpl(initialValues.getInstallation(), new ProductImpl(initialValues.getProduct()));
      data.getProduct().getInternals().setTelemetryEnabled(true);
    }

    public TelemetryDataBuilder installation(String installation) {
      data.setInstallation(installation);
      return this;
    }

    // Product
    public TelemetryDataBuilder productName(String name) {
      data.getProduct().setName(name);
      return this;
    }

    public TelemetryDataBuilder productVersion(String version) {
      data.getProduct().setVersion(version);
      return this;
    }

    public TelemetryDataBuilder productEdition(String edition) {
      data.getProduct().setEdition(edition);
      return this;
    }

    // Internals
    public TelemetryDataBuilder database(String vendor, String version) {
      DatabaseImpl database = new DatabaseImpl(vendor, version);
      data.getProduct().getInternals().setDatabase(database);
      return this;
    }

    public TelemetryDataBuilder applicationServer(String serverVersion) {
      data.getProduct().getInternals().setApplicationServer(new ApplicationServerImpl(serverVersion));
      return this;
    }

    public TelemetryDataBuilder licenseKey(LicenseKeyDataImpl key) {
      data.getProduct().getInternals().setLicenseKey(key);
      return this;
    }

    public TelemetryDataBuilder licenseKeyRaw(String rawKey) {
      LicenseKeyDataImpl key = new LicenseKeyDataImpl(null, null, null, null, null, rawKey);
      data.getProduct().getInternals().setLicenseKey(key);
      return this;
    }

    public TelemetryDataBuilder addIntegration(String integration) {
      if(data.getProduct().getInternals().getCamundaIntegration() == null) {
        data.getProduct().getInternals().setCamundaIntegration(new HashSet<>());
      }
      data.getProduct().getInternals().getCamundaIntegration().add(integration);
      return this;
    }

    public TelemetryDataBuilder dataCollectionStartDate(Date date) {
      data.getProduct().getInternals().setDataCollectionStartDate(date);
      return this;
    }

    public TelemetryDataBuilder countCommand(String name, int count) {
      data.getProduct().getInternals().putCommand(name, count);
      return this;
    }

    public TelemetryDataBuilder countMetric(String name, int count) {
      data.getProduct().getInternals().putMetric(name, count);

      // add public name as expected
      final String publicName = MetricsUtil.resolvePublicName(name);
      data.getProduct().getInternals().putMetric(publicName, count);

      return this;
    }

    public TelemetryDataBuilder webapps(String... webapps) {
      Set<String> webappSet = new HashSet<>();
      for (String webapp : webapps) {
        webappSet.add(webapp);
      }

      data.getProduct().getInternals().setWebapps(webappSet);
      return this;
    }

    public TelemetryDataBuilder jdk(JdkImpl jdk) {
      data.getProduct().getInternals().setJdk(jdk);
      return this;
    }

    public TelemetryDataBuilder telemetryEnabled(boolean enabled) {
      data.getProduct().getInternals().setTelemetryEnabled(enabled);
      return this;
    }
}

  protected Map<String, Command> getDefaultCommandCounts() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("TelemetryConfigureCmd", new CommandImpl(1));
    commands.put("IsTelemetryEnabledCmd", new CommandImpl(1));
    return commands;
  }

  protected TelemetryDataImpl initData(TelemetryDataImpl telemetryData) {
    TelemetryDataImpl data = new TelemetryDataImpl(telemetryData.getInstallation(), new ProductImpl(telemetryData.getProduct()));
    data.getProduct().getInternals().setTelemetryEnabled(true);
    return data;
  }

  protected Date addHour(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, 1);
    Date newDate = calendar.getTime();
    return newDate;
  }

  protected BpmnModelInstance createProcessWithBusinessRuleTask(String processId, String decisionRef) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processId)
        .startEvent()
        .businessRuleTask("task")
        .endEvent()
        .done();

    BusinessRuleTask task = modelInstance.getModelElementById("task");
    task.setCamundaDecisionRef(decisionRef);
    return modelInstance;
  }

  protected void executeDataValidationTest(String name, String version, String edition, String installationId) {
    managementService.toggleTelemetry(true);
    TelemetryDataImpl invalidData = createDataToSendWithCustomValues(name, version, edition, installationId);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
      .willReturn(aResponse()
        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    standaloneReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
      TELEMETRY_ENDPOINT,
      0,
      1000,
      invalidData,
      configuration.getTelemetryHttpConnector(),
      configuration.getTelemetryRegistry(),
      configuration.getMetricsRegistry(),
      configuration.getTelemetryRequestTimeout());

    // when
    standaloneReporter.reportNow();

    // then
    verify(0, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));
    String warnLogMessage = "Cannot send the telemetry data. Some of the data is invalid. " +
        "Set this logger to DEBUG/FINE to see more details.";
    String debugLogMessage = String.format("Cannot send the telemetry task data. The following values must be " +
        "non-empty Strings: '%s' (name), '%s' (version), '%s' (edition), '%s' (UUIDv4 installation id).",
      name,
      version,
      edition,
      installationId);
    assertThat(loggingRule.getFilteredLog(warnLogMessage)).hasSize(1);
    assertThat(loggingRule.getFilteredLog(debugLogMessage)).hasSize(1);
  }

  protected TelemetryDataImpl getTelemetryDataIgnoreCollectionTimeFrame() {
    TelemetryDataImpl telemetryData = new TelemetryDataImpl(configuration.getTelemetryData());
    telemetryData.getProduct().getInternals().setDataCollectionStartDate(null);
    return telemetryData;
  }

  protected String toJson(TelemetryDataImpl expectedData) {
    return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create().toJson(expectedData);
  }
}
