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
import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.FLOW_NODE_INSTANCES;
import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.ROOT_PROCESS_INSTANCES;
import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.UNIQUE_TASK_WORKERS;
import static org.junit.Assert.fail;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Database;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Metric;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;

public class TelemetryReporterTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8081/pings";
  private static final String TELEMETRY_ENDPOINT_PATH = "/pings";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(configuration ->
          configuration.setTelemetryEndpoint(TELEMETRY_ENDPOINT)
            );

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8081);

  protected ProcessEngine standaloneProcessEngine;
  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = configuration.getManagementService();
    runtimeService = configuration.getRuntimeService();
    taskService = configuration.getTaskService();

    DefaultDmnEngineConfiguration dmnEngineConfiguration = configuration
        .getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();

    clearMetrics();

    // clean up the recorded commands
    configuration.setTelemetryRegistry(new TelemetryRegistry());
  }

  @After
  public void tearDown() {
    ClockUtil.resetClock();
    managementService.toggleTelemetry(false);

    clearMetrics();

    if (standaloneProcessEngine != null) {
      standaloneProcessEngine.close();
      ProcessEngines.unregister(standaloneProcessEngine);
    }

    DefaultDmnEngineConfiguration dmnEngineConfiguration = configuration
        .getDmnEngineConfiguration();
    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  protected void clearMetrics() {
    Collection<Meter> meters = configuration.getMetricsRegistry().getMeters().values();
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
                        .withBody(requestBody)
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                data,
                                                                configuration.getTelemetryHttpConnector());

    // when
    telemetryReporter.reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldReportDataWhenTelemetryInitialized() {
    // given
    Data data = createDataToSend();
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration
        .setTelemetryEndpoint(TELEMETRY_ENDPOINT)
        .setInitializeTelemetry(true)
        .setTelemetryData(data)
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
    standaloneProcessEngine = processEngineConfiguration.buildProcessEngine();
  
    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();
  
    // then
    String requestBody = new Gson().toJson(data);
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWithApplicationServerInfo() {
    // given default telemetry data (no application server)
    managementService.toggleTelemetry(true);
    // set application server after initialization
    String applicationServerVersion = "Tomcat 10";
    configuration.getTelemetryRegistry().setApplicationServer(applicationServerVersion);

    Data expectedData = adjustDataWithProductVersionAndAppServerInfo(configuration.getTelemetryData(), applicationServerVersion);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withBody(requestBody)
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
              .withRequestBody(equalToJson(requestBody))
              .withHeader("Content-Type",  equalTo("application/json")));
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
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    if (configuration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {
      assertThat(configuration.getTelemetryRegistry().getCommands().size()).isEqualTo(3);
    } else if (configuration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_NONE.getId()) {
      assertThat(configuration.getTelemetryRegistry().getCommands().size()).isEqualTo(2);
    } else {
      fail("Unexpected history level.");
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void shouldSetStartReportTimeWhenTelemetryEnabled() {
    // given
    Date currentTime = ClockUtil.getCurrentTime();

    // when
    managementService.toggleTelemetry(true);

    // then
    assertThat(configuration.getTelemetryRegistry().getStartReportTime()).isInSameSecondWindowAs(currentTime);

  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void shouldSendTelemetryWithRooProcessInstanceMetrics() {
    // given
    managementService.toggleTelemetry(true);
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }
    configuration.getDbMetricsReporter().reportNow();

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 3, 0, 6, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    Map<String, Metric> metrics = configuration.getTelemetryData().getProduct().getInternals().getMetrics();
    assertThat(metrics.get(ROOT_PROCESS_INSTANCES).getCount()).isEqualTo(3);
  }


  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void shouldSendTelemetryOnceWithRooProcessInstanceMetrics() {
    // given
    managementService.toggleTelemetry(false);
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }
    configuration.getDbMetricsReporter().reportNow();
    ClockUtil.setCurrentTime(addHour(new Date()));
    managementService.toggleTelemetry(true);
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }
    configuration.getDbMetricsReporter().reportNow();
    ClockUtil.setCurrentTime(addHour(new Date()));

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 3, 0, 6, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    Map<String, Metric> metrics = configuration.getTelemetryData().getProduct().getInternals().getMetrics();
    assertThat(metrics.get(ROOT_PROCESS_INSTANCES).getCount()).isEqualTo(3);
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

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 2, 2, 4, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    Map<String, Metric> metrics = configuration.getTelemetryData().getProduct().getInternals().getMetrics();
    assertThat(metrics.get(EXECUTED_DECISION_INSTANCES).getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldSendTelemetryWithFlowNodeInstanceMetrics() {
    // given
    managementService.toggleTelemetry(true);
    for (int i = 0; i < 4; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
      String taskId = taskService.createTaskQuery().singleResult().getId();
      taskService.complete(taskId);
    }

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 4, 0, 12, 0);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    Map<String, Metric> metrics = configuration.getTelemetryData().getProduct().getInternals().getMetrics();
    assertThat(metrics.get(FLOW_NODE_INSTANCES).getCount()).isEqualTo(12);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldSendTelemetryWithTaskWorkersMetrics() {
    // given
    managementService.toggleTelemetry(true);
    for (int i = 0; i < 3; i++) {
      String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
      String taskId = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult().getId();
      taskService.setAssignee(taskId, "user" + i);
    }

    Data expectedData = adjustDataWithMetricCounts(configuration.getTelemetryData(), 3, 0, 6, 3);

    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    Map<String, Metric> metrics = configuration.getTelemetryData().getProduct().getInternals().getMetrics();
    assertThat(metrics.get(UNIQUE_TASK_WORKERS).getCount()).isEqualTo(3);
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
    assertThat(loggingRule.getFilteredLog("Telemetry data sent").size()).isOne();
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
            "Unexpected response while sending telemetry data. Status code: " + HttpURLConnection.HTTP_NOT_ACCEPTABLE)
        .size()).isOne();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogTelemetryDisabled() {
    // given default configuration
    managementService.toggleTelemetry(false);

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Sending telemetry is disabled").size()).isPositive();
    // it might have two logs:
    // first during process engine start
    // second during #reportNow call
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogErrorOnDebugWhenHttpConnectorNotInitialized() {
    // given
    managementService.toggleTelemetry(true);
    Data data = createDataToSend();
    String requestBody = new Gson().toJson(data);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
            .willReturn(aResponse()
                        .withBody(requestBody)
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                data,
                                                                null);

    // when
    telemetryReporter.reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("'java.lang.NullPointerException' exception occurred while sending telemetry data").size()).isOne();
  }


  protected Data createDataToSend() {
    Database database = new Database("mySpecialDb", "v.1.2.3");
    Internals internals = new Internals(database, new ApplicationServer("Apache Tomcat/10.0.1"));

    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);

    Map<String, Metric> metrics = getDefaultMetrics();
    internals.setMetrics(metrics);

    Product product = new Product("Runtime", "7.14.0", "special", internals);
    Data data = new Data("f5b19e2e-b49a-11ea-b3de-0242ac130004", product);
    return data;
  }

  protected Data adjustDataWithProductVersionAndAppServerInfo(Data telemetryData, String applicationServerVersion) {
    Data result = new Data(telemetryData.getInstallation(), telemetryData.getProduct());

    Product product = result.getProduct();
    product.setVersion("7.14.0");
    result.setProduct(product);

    Internals internals = result.getProduct().getInternals();

    internals.setApplicationServer(new ApplicationServer(applicationServerVersion));
    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);

    Map<String, Metric> metrics = getDefaultMetrics();
    result.getProduct().getInternals().setMetrics(metrics);

    return result;
  }

  protected Data adjustDataWithCommandCounts(Data telemetryData) {
    Data result = adjustDataWithProductVersionAndAppServerInfo(telemetryData, "Wildfly 10");

    Map<String, Command> commands = result.getProduct().getInternals().getCommands();
    commands.put("GetHistoryLevelCmd", new Command(1));
    commands.put("GetLicenseKeyCmd", new Command(1));
    result.getProduct().getInternals().setCommands(commands);

    return result;
  }

  protected Map<String, Command> getDefaultCommandCounts() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("TelemetryConfigureCmd", new Command(1));
    commands.put("IsTelemetryEnabledCmd", new Command(1));
    return commands;
  }

  protected Map<String, Metric> getDefaultMetrics() {
    return assembleMetrics(0, 0, 0, 0);
  }

  protected Data adjustDataWithMetricCounts(Data telemetryData, long processCount, long decisionCount, long flowNodeCount, long workerCount) {
    Data result = adjustDataWithProductVersionAndAppServerInfo(telemetryData, "JBoss EAP 7.2.0.GA");

    Internals internals = result.getProduct().getInternals();
    Map<String, Metric> metrics = assembleMetrics(processCount, decisionCount, flowNodeCount, workerCount);
    internals.setMetrics(metrics);

    // to clean up the recorded commands
    configuration.getTelemetryRegistry().getCommands().clear();

    Map<String, Command> commands = new HashMap<>();
    commands.put("IsTelemetryEnabledCmd", new Command(1));
    internals.setCommands(commands);

    return result;
  }


  protected Map<String, Metric> assembleMetrics(long processCount, long decisionCount, long flowNodeCount, long workerCount) {
    Map<String, Metric> metrics = new HashMap<>();
    metrics.put(ROOT_PROCESS_INSTANCES, new Metric(processCount));
    metrics.put(EXECUTED_DECISION_INSTANCES, new Metric(decisionCount));
    metrics.put(FLOW_NODE_INSTANCES, new Metric(flowNodeCount));
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

}
