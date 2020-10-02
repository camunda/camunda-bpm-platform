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
import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.UNIQUE_TASK_WORKERS;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Database;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Jdk;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyData;
import org.camunda.bpm.engine.impl.telemetry.dto.Metric;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ParseUtil;
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
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;

/**
 * Uses Wiremock so should be run as part of {@link TelemetrySuiteTest}.
 */
public class TelemetryReporterTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8084/pings";
  private static final String TELEMETRY_ENDPOINT_PATH = "/pings";

  public static String DMN_FILE = "org/camunda/bpm/engine/test/api/mgmt/metrics/ExecutedDecisionElementsTest.dmn11.xml";
  public static VariableMap VARIABLES = Variables.createVariables().putValue("status", "").putValue("sum", 100);


  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(configuration ->
          configuration
            .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
            .setTelemetryReporterActivate(true)
            .setProcessEngineBootstrapCommand(new BootstrapEngineCommand() {
              @Override
              protected void initializeInitialTelemetryMessage() {
                sendInitialTelemetryMessage = false;
              }
            })
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

  protected Data defaultTelemetryData;

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

    defaultTelemetryData = new Data(configuration.getTelemetryData());
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
    Data data = createDataToSend();
    String requestBody = new Gson().toJson(data);
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
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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
  public void shouldNotReportInitialDataWhenReporterActivatedAndInitTelemetryUndefinedDuringProcessEngineClose() {
    // given
    createEngineWithInitMessage(null);

    // when
    standaloneProcessEngine.close();
    standaloneProcessEngine = null;

    // then
    verify(0, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));
  }

  @Test
  public void shouldNotReportInitialDataWhenReporterActivatedAndInitTelemetryDisabledDuringProcessEngineClose() {
    // given
    createEngineWithInitMessage(false);

    // when
    standaloneProcessEngine.close();
    standaloneProcessEngine = null;

    // then
    verify(0, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));
  }

  @Test
  public void shouldNotReportInitialDataWhenReporterActivatedAndInitTelemetryEnabledDuringProcessEngineClose() {
    // given
    createEngineWithInitMessage(true);

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    standaloneProcessEngine.close();
    standaloneProcessEngine = null;

    // then
    verify(1, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withHeader("Content-Type",  equalTo("application/json")));  }

  @Test
  public void shouldReportInitialDataWhenReporterActivatedAndInitTelemetryUndefined() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(null);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), null);
    String requestBody = new Gson().toJson(expectedData);

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }


  @Test
  public void shouldReportInitialDataWhenReporterActivatedAndInitTelemetryDisabled() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(false);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), false);
    String requestBody = new Gson().toJson(expectedData);

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldReportInitialDataWhenReporterActivatedAndInitTelemetryEnabled() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), true);
    String requestBody = new Gson().toJson(expectedData);

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldReportInitialDataOnlyOnceInitTelemetryUndefinedReportPlusClose() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(null);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), null);
    String requestBody = new Gson().toJson(expectedData);

    processEngineConfiguration.getTelemetryReporter().reportNow();

    // when
    standaloneProcessEngine.close();
    standaloneProcessEngine = null;

    // then
    verify(1, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }


  @Test
  public void shouldReportInitialDataOnlyOnceInitTelemetryDisabledReportPlusClose() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(false);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), false);
    String requestBody = new Gson().toJson(expectedData);

    processEngineConfiguration.getTelemetryReporter().reportNow();

    // when
    standaloneProcessEngine.close();
    standaloneProcessEngine = null;

    // then
    verify(1, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldReportInitialDataOnlyOnceInitTelemetryEnabledReportPlusClose() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), true);
    String requestBody = new Gson().toJson(expectedData);

    processEngineConfiguration.getTelemetryReporter().reportNow();

    // when
    standaloneProcessEngine.close();
    standaloneProcessEngine = null;

    // then
    verify(3, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
    assertThat(loggingRule.getFilteredLog("Sending initial telemetry data").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Initial telemetry request was successful.").size()).isOne();
  }

  @Test
  public void shouldReportInitialDataOnlyOnceWhenReportingTwice() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(false);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    Data expectedData = createInitialDataToSend(processEngineConfiguration.getTelemetryData(), false);
    String requestBody = new Gson().toJson(expectedData);

    processEngineConfiguration.getTelemetryReporter().reportNow();

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    verify(1, postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldNotReportInitialDataWhenReporterDeactivated() {
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
  public void shouldSendTelemetryWithApplicationServerInfo() {
    // given default telemetry data (no application server)
    managementService.toggleTelemetry(true);
    // set application server after initialization
    String applicationServerVersion = "Tomcat 10";
    configuration.getTelemetryRegistry().setApplicationServer(applicationServerVersion);

    Data expectedData = adjustDataWithAppServerInfo(configuration.getTelemetryData(), applicationServerVersion);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithLicenseInfo() {
    // given default telemetry data (no license key)
    managementService.toggleTelemetry(true);
    // set key after initialization
    LicenseKeyData licenseKey = new LicenseKeyData("customer a", "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license");
    configuration.getTelemetryRegistry().setLicenseKey(licenseKey);

    Data expectedData = adjustDataWithLicenseInfo(configuration.getTelemetryData(), licenseKey);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithOverriddenLicenseInfo() {
    // given default telemetry data (no license key)
    managementService.toggleTelemetry(true);
    // set key after initialization
    LicenseKeyData licenseKey = new LicenseKeyData("customer a", "UNIFIED", "2029-09-01", false, Collections.singletonMap("camundaBPM", "true"), "raw license");
    configuration.getTelemetryRegistry().setLicenseKey(licenseKey);

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
    // report once
    configuration.getTelemetryReporter().reportNow();
    configuration.getTelemetryRegistry().getCommands().clear();

    // change license key
    licenseKey = new LicenseKeyData("customer b", "UNIFIED", "2029-08-01", false, Collections.singletonMap("cawemo", "true"), "new raw license");
    configuration.getTelemetryRegistry().setLicenseKey(licenseKey);

    Data expectedData = adjustDataWithLicenseInfo(configuration.getTelemetryData(), licenseKey);

    String requestBody = new Gson().toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithRawLicenseInfoOnly() {
    // given default telemetry data (no license key)
    managementService.toggleTelemetry(true);
    // set key after initialization via management service
    String licenseKeyRaw = "raw license";
    managementService.setLicenseKey(licenseKeyRaw);

    Data expectedData = adjustDataWithRawLicenseInfo(configuration.getTelemetryData(), licenseKeyRaw);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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

    Data expectedData = adjustDataWithCommandCounts(configuration.getTelemetryData());

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 3, 0, 0, 6, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 0, 0, 0, 0, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    configuration.getTelemetryReporter().reportNow();

    // when sending telemetry again
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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
    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 2, 2, 2, 4, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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
    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 1, 16, 1, 3, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
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
    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 4, 0, 0, 12, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldSendTelemetryWithTaskWorkersMetrics() {
    // given
    managementService.toggleTelemetry(true);

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    for (int i = 0; i < 3; i++) {
      String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
      String taskId = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult().getId();
      taskService.setAssignee(taskId, "user" + i);
    }

    ClockUtil.setCurrentTime(addHour(ClockUtil.getCurrentTime()));

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 3, 0, 0, 6, 3);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldAddJdkInfoToTelemetryData() {
    // given
    Data telemetryData = configuration.getTelemetryData();

    // then
    Jdk jdkInfo = telemetryData.getProduct().getInternals().getJdk();
    assertThat(jdkInfo).isNotNull();
    Jdk expectedJdkInfo = ParseUtil.parseJdkDetails();
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
  public void shouldLogInitialTelemetrySent() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(false);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Start telemetry sending task").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Sending initial telemetry data").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Initial telemetry request was successful.").size()).isOne();
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
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogUnexpectedResponseOnInitialMessage() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = createEngineWithInitMessage(false);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_NOT_ACCEPTABLE)));

    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule
        .getFilteredLog(
            "Unexpected response code " + HttpURLConnection.HTTP_NOT_ACCEPTABLE + " when sending initial telemetry data" )
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
    Data data = createDataToSend();
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
    assertThat(loggingRule.getFilteredLog("Could not send telemetry data. "
        + "Reason: NullPointerException with message 'null'. "
        + "Set this logger to DEBUG/FINE for the full stacktrace.").size()).isOne();
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

    Data expectedData = adjustDataWithCommandCounts(configuration.getTelemetryData());
    String expectedRequestBody = new Gson().toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    // the request is made and the data is not reset between requests
    verify(3, postRequestedFor(
        urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedRequestBody, JSONCompareMode.LENIENT))
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

    Data expectedData = adjustDataWithCommandCounts(configuration.getTelemetryData());
    String expectedRequestBody = new Gson().toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    // the request is made and the data is not reset between requests
    verify(1, postRequestedFor(
        urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedRequestBody, JSONCompareMode.LENIENT))
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

    Data expectedData = adjustDataWithCommandCounts(configuration.getTelemetryData());
    String expectedRequestBody = new Gson().toJson(expectedData);

    // when
    configuration.getTelemetryReporter().reportNow();

    // the request is made and the data is not reset between requests
    verify(3, postRequestedFor(
        urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(expectedRequestBody, JSONCompareMode.LENIENT))
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
    Data expectedData = createDataWithCamundaIntegration(configuration.getTelemetryData(), "wildfly-integration");
    // creating a new object as during the report the object is being modified
    Data givenData = createDataWithCamundaIntegration(configuration.getTelemetryData(), "wildfly-integration");
    managementService.toggleTelemetry(true);

    String requestBody = new Gson().toJson(expectedData);
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
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  protected ProcessEngineConfigurationImpl createEngineWithInitMessage(Boolean initTelemetry) {
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
        .setProcessEngineName("standalone")
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    if (initTelemetry != null) {
      processEngineConfiguration.setInitializeTelemetry(initTelemetry);
    }
    standaloneProcessEngine = processEngineConfiguration.buildProcessEngine();
    return processEngineConfiguration;
  }

  protected Data createDataToSend() {
    Database database = new Database("mySpecialDb", "v.1.2.3");
    Jdk jdk = ParseUtil.parseJdkDetails();
    Internals internals = new Internals(database, new ApplicationServer("Apache Tomcat/10.0.1"), null, jdk);
    internals.setTelemetryEnabled(true);

    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);

    Map<String, Metric> metrics = getDefaultMetrics();
    internals.setMetrics(metrics);

    Product product = new Product("Runtime", "7.14.0", "special", internals);
    Data data = new Data("f5b19e2e-b49a-11ea-b3de-0242ac130004", product);
    return data;
  }

  protected Data createDataWithCamundaIntegration(Data telemetryData, String integrationString) {
    Internals internals = new Internals(null, null, null, null);
    Data data = new Data(telemetryData.getInstallation(), new Product("dummy", "dummy", "dummy", internals));
    HashSet<String> integration = new HashSet<>();
    integration.add(integrationString);
    internals.setCamundaIntegration(integration);
    return data;
  }

  protected Data createInitialDataToSend(Data telemetryData, Boolean telemetryEnabled) {
    Data result = initData(telemetryData);
    Internals internals = new Internals();
    internals.setTelemetryEnabled(telemetryEnabled);
    result.getProduct().setInternals(internals);

    return result;
  }

  protected Data adjustDataWithAppServerInfo(Data telemetryData, String applicationServerVersion) {
    Data result = initData(telemetryData);

    Internals internals = result.getProduct().getInternals();

    internals.setApplicationServer(new ApplicationServer(applicationServerVersion));
    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);

    Map<String, Metric> metrics = getDefaultMetrics();
    result.getProduct().getInternals().setMetrics(metrics);

    return result;
  }

  protected Data adjustDataWithCommandCounts(Data telemetryData) {
    Data result = initData(telemetryData);

    Map<String, Command> commands = result.getProduct().getInternals().getCommands();
    commands.put("GetHistoryLevelCmd", new Command(1));
    commands.put("GetLicenseKeyCmd", new Command(1));
    result.getProduct().getInternals().setCommands(commands);

    return result;
  }

  protected Data adjustDataWithLicenseInfo(Data telemetryData, LicenseKeyData licenseKeyData) {
    Data result = initData(telemetryData);

    Internals internals = result.getProduct().getInternals();
    internals.setLicenseKey(licenseKeyData);

    return result;
  }

  protected Data adjustDataWithRawLicenseInfo(Data telemetryData, String licenseKeyRaw) {
    Data result = initData(telemetryData);

    Internals internals = result.getProduct().getInternals();
    internals.setLicenseKey(new LicenseKeyData(null, null, null, null, null, licenseKeyRaw));

    return result;
  }

  protected Map<String, Command> getDefaultCommandCounts() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("TelemetryConfigureCmd", new Command(1));
    commands.put("IsTelemetryEnabledCmd", new Command(1));
    return commands;
  }

  protected Map<String, Metric> getDefaultMetrics() {
    return assembleMetrics(0, 0, 0, 0, 0);
  }

  protected Data adjustDataWithMetricCounts(Data telemetryData, long processCount, long decisionElementsCount, long decisionInstancesCount, long activityInstanceCount, long workerCount) {
    Data result = initData(telemetryData);

    Internals internals = result.getProduct().getInternals();
    Map<String, Metric> metrics = assembleMetrics(processCount, decisionElementsCount, decisionInstancesCount, activityInstanceCount, workerCount);
    internals.setMetrics(metrics);

    // to clean up the recorded commands
    configuration.getTelemetryRegistry().getCommands().clear();

    Map<String, Command> commands = new HashMap<>();
    commands.put("IsTelemetryEnabledCmd", new Command(1));
    internals.setCommands(commands);

    return result;
  }

  protected Data initData(Data telemetryData) {
    Data data = new Data(telemetryData.getInstallation(), new Product(telemetryData.getProduct()));
    data.getProduct().getInternals().setTelemetryEnabled(true);
    return data;
  }

  protected Map<String, Metric> assembleMetrics(long processCount, long decisionElementsCount, long decisionInstancesCount, long activityInstanceCount, long workerCount) {
    Map<String, Metric> metrics = new HashMap<>();
    metrics.put(ROOT_PROCESS_INSTANCE_START, new Metric(processCount));
    metrics.put(EXECUTED_DECISION_ELEMENTS, new Metric(decisionElementsCount));
    metrics.put(EXECUTED_DECISION_INSTANCES, new Metric(decisionInstancesCount));
    metrics.put(ACTIVTY_INSTANCE_START, new Metric(activityInstanceCount));
    metrics.put(UNIQUE_TASK_WORKERS, new Metric(workerCount));
    return metrics;
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


}
