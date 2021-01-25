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
package org.camunda.bpm.engine.test.standalone.telemetry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.UNIQUE_TASK_WORKERS;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Metric;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;

public class TelemetryTaskWorkerMetricsTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8083/pings";
  private static final String TELEMETRY_ENDPOINT_PATH = "/pings";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(configuration ->
      configuration.setTelemetryEndpoint(TELEMETRY_ENDPOINT)
                   .setJdbcUrl("jdbc:h2:mem:TelemetryTaskWorkerMetricsTest")
                   .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
                   .setHistoryLevel(HistoryLevel.HISTORY_LEVEL_NONE));

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8083);

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


    clearMetrics();

    // clean up the recorded commands
    configuration.setTelemetryRegistry(new TelemetryRegistry());
  }

  @After
  public void tearDown() {
    managementService.toggleTelemetry(false);
    configuration.setTaskMetricsEnabled(false);
    clearMetrics();
  }

  protected void clearMetrics() {
    Collection<Meter> meters = configuration.getMetricsRegistry().getDbMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
    managementService.deleteTaskMetrics(null);
  }


  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldNotSendTelemetryWithTaskWorkersMetrics() {
    // given default telemetry data
    managementService.toggleTelemetry(true);

    Data expectedData = adjustData(configuration.getTelemetryData(), 3, 0, 6, 0);
    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    for (int i = 0; i < 3; i++) {
      String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
      String taskId = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult().getId();
      taskService.setAssignee(taskId, "user" + i);
    }

    configuration.getTelemetryRegistry().getCommands().clear();

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldSendTelemetryWithTaskWorkersMetricsIfEnabled() {
    // given default telemetry data and task metrics enabled
    configuration.setTaskMetricsEnabled(true);
    managementService.toggleTelemetry(true);

    Data expectedData = adjustData(configuration.getTelemetryData(), 3, 0, 6, 3);
    String requestBody = new Gson().toJson(expectedData);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    for (int i = 0; i < 3; i++) {
      String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
      String taskId = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult().getId();
      taskService.setAssignee(taskId, "user" + i);
    }

    configuration.getTelemetryRegistry().getCommands().clear();

    ClockUtil.offset(5000L);// move the clock a bit to ensure we fetch all UTWs

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withRequestBody(equalToJson(requestBody, JSONCompareMode.LENIENT))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  protected Data adjustData(Data telemetryData, int processCount, int decisionCount, int flowNodeCount, int workerCount) {
    Data result = new Data(telemetryData);

    Internals internals = result.getProduct().getInternals();

    Map<String, Metric> metrics = new HashMap<>();
    metrics.put(ROOT_PROCESS_INSTANCE_START, new Metric(processCount));
    metrics.put(EXECUTED_DECISION_ELEMENTS, new Metric(decisionCount));
    metrics.put(EXECUTED_DECISION_INSTANCES, new Metric(decisionCount));
    metrics.put(ACTIVTY_INSTANCE_START, new Metric(flowNodeCount));
    metrics.put(UNIQUE_TASK_WORKERS, new Metric(workerCount));
    internals.setMetrics(metrics);

    internals.setTelemetryEnabled(true);

    return result;
  }
}
