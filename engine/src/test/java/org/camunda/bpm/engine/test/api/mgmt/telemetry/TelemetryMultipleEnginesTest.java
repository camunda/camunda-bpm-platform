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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.metrics.reporter.DbMetricsReporter;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.JsonTestUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.telemetry.Metric;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.gson.Gson;

/**
 * Simulates cluster setups where multiple engines are supposed to send telemetry.
 *
 * Note: This test assumes that the default engine is configured against the wiremock endpoint on port 8081.
 */
public class TelemetryMultipleEnginesTest {

  protected static final String TELEMETRY_ENDPOINT_PATH = "/pings";
  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8081" + TELEMETRY_ENDPOINT_PATH;

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8081);

  @ClassRule
  public static ProcessEngineBootstrapRule secondEngineRule = new ProcessEngineBootstrapRule(config ->
      config.setTelemetryEndpoint(TELEMETRY_ENDPOINT));

  @ClassRule
  public static ProcessEngineBootstrapRule defaultEngineRule = new ProcessEngineBootstrapRule(config ->
      config.setTelemetryEndpoint(TELEMETRY_ENDPOINT));

  protected ProcessEngine defaultEngine;
  protected ProcessEngine secondEngine;
  private TelemetryReporter defaultTelemetryReporter;
  private TelemetryReporter secondTelemetryReporter;

  @Before
  public void init() {
    defaultEngine = defaultEngineRule.getProcessEngine();
    secondEngine = secondEngineRule.getProcessEngine();

    defaultTelemetryReporter = getTelemetryReporter(defaultEngine);
    secondTelemetryReporter = getTelemetryReporter(secondEngine);

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
  }

  /**
   * When telemetry is toggled on one engine, then the other engine should
   * pick that up and also start sending telemetry.
   */
  @Test
  public void shouldPickUpTelemetryActivation() {
    // when
    defaultEngine.getManagementService().toggleTelemetry(true);

    // when
    secondTelemetryReporter.reportNow();

    // then
    // the second engine reports its metrics
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldReportMetricsPerEngine() {
    // given
    clearMetrics();

    defaultEngine.getManagementService().toggleTelemetry(true);
    secondEngine.getManagementService().toggleTelemetry(true);

    MetricsRegistry defaultMetricsRegistry = getMetricsRegistry(defaultEngine);
    MetricsRegistry secondMetricsRegistry = getMetricsRegistry(secondEngine);

    defaultMetricsRegistry.markOccurrence(Metrics.EXECUTED_DECISION_INSTANCES);
    secondMetricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START);

    persistMetrics();

    // when
    defaultTelemetryReporter.reportNow();
    secondTelemetryReporter.reportNow();

    // then
    List<LoggedRequest> requests = wireMockRule.findAll(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));

    assertThat(requests).hasSize(2);

    Gson gson = JsonTestUtil.createTelemetryDataMapper();

    LoggedRequest defaultRequest = requests.get(0);
    TelemetryDataImpl defaultRequestBody = gson.fromJson(defaultRequest.getBodyAsString(), TelemetryDataImpl.class);
    assertReportedMetrics(defaultRequestBody, 0, 1, 0);

    LoggedRequest secondRequest = requests.get(1);
    TelemetryDataImpl secondRequestBody = gson.fromJson(secondRequest.getBodyAsString(), TelemetryDataImpl.class);
    assertReportedMetrics(secondRequestBody, 1, 0, 0);
  }

  @Test
  public void shouldUpdateTelemetryFlagDuringReporting() {
    // given
    MetricsRegistry secondMetricsRegistry = getMetricsRegistry(secondEngine);
    secondMetricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START);

    defaultEngine.getManagementService().toggleTelemetry(true);

    secondMetricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START);

    // when
    secondTelemetryReporter.reportNow();

    // then
    // the metrics collected before the next reporting cycle were reset
    Meter rootPiMeter = secondMetricsRegistry.getTelemetryMeters().get(Metrics.ROOT_PROCESS_INSTANCE_START);
    assertThat(rootPiMeter.get()).isEqualTo(0);

    List<LoggedRequest> requests = wireMockRule.findAll(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH)));

    assertThat(requests).hasSize(1);

    Gson gson = JsonTestUtil.createTelemetryDataMapper();

    LoggedRequest defaultRequest = requests.get(0);
    TelemetryDataImpl defaultRequestBody = gson.fromJson(defaultRequest.getBodyAsString(), TelemetryDataImpl.class);
    assertReportedMetrics(defaultRequestBody, 0, 0, 0);
  }

  private TelemetryReporter getTelemetryReporter(ProcessEngine engine) {

    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl)
        engine.getProcessEngineConfiguration();
    return engineConfiguration.getTelemetryReporter();
  }

  private MetricsRegistry getMetricsRegistry(ProcessEngine engine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
    return processEngineConfiguration.getMetricsRegistry();
  }

  private void assertReportedMetrics(
      TelemetryDataImpl data,
      int expectedRootInstances,
      int expectedDecisionInstances,
      int expectedFlowNodeInstances) {
    InternalsImpl internals = data.getProduct().getInternals();

    assertMetric(internals, Metrics.ROOT_PROCESS_INSTANCE_START, expectedRootInstances);
    assertMetric(internals, Metrics.EXECUTED_DECISION_INSTANCES, expectedDecisionInstances);
    assertMetric(internals, Metrics.ACTIVTY_INSTANCE_START, expectedFlowNodeInstances);
  }

  private void assertMetric(InternalsImpl internals, String name, int expectedCount) {

    Map<String, Metric> metrics = internals.getMetrics();

    Metric rootMetric = metrics.get(name);
    assertThat(rootMetric).isNotNull();
    assertThat(rootMetric.getCount()).describedAs("metric " + name).isEqualTo(expectedCount);
  }

  private void persistMetrics() {
    persistMetrics(defaultEngine);
    persistMetrics(secondEngine);
  }

  private void persistMetrics(ProcessEngine engine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
    DbMetricsReporter metricsReporter = processEngineConfiguration.getDbMetricsReporter();
    metricsReporter.reportNow();
  }

  @After
  public void tearDown() {
    WireMock.resetAllRequests();
    clearMetrics();

    // deactivating telemetry on both engines, so that the in-memory state
    // is also updated
    defaultEngine.getManagementService().toggleTelemetry(false);
    secondEngine.getManagementService().toggleTelemetry(false);
  }

  protected void clearMetrics() {
    clearMetrics(defaultEngine);
    clearMetrics(secondEngine);
  }

  protected void clearMetrics(ProcessEngine engine) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
    MetricsRegistry metricsRegistry = processEngineConfiguration.getMetricsRegistry();

    Collection<Meter> meters = metricsRegistry.getTelemetryMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }

    engine.getManagementService().deleteMetrics(null);
  }
}
