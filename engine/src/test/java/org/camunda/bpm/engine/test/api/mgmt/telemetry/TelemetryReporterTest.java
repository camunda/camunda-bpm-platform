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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Database;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class TelemetryReporterTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8082/pings";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8082);

  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = configuration.getManagementService();
  }

  @After
  public void tearDown() {
    managementService.toggleTelemetry(false);
  }

  @Test
  public void shouldSendTelemetry() {
    // given
    managementService.toggleTelemetry(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT))
              .willReturn(aResponse()
                          .withStatus(HttpStatus.SC_ACCEPTED)));

    Data data = createDataToSend();
    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                data,
                                                                HttpClientBuilder.create().build());

    // when
    telemetryReporter.reportNow();

    // then
    String requestBody = new Gson().toJson(data);
    verify(postRequestedFor(urlEqualTo("/pings"))
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
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT))
        .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_ACCEPTED)));
    processEngineConfiguration.buildProcessEngine();
  
    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();
  
    // then
    String requestBody = new Gson().toJson(data);
    verify(postRequestedFor(urlEqualTo("/pings"))
              .withRequestBody(equalToJson(requestBody))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogTelemetrySent() throws ClientProtocolException, IOException {
    // given
    managementService.toggleTelemetry(true);
    HttpClient mockedClient = mock(HttpClient.class);
    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                createDataToSend(),
                                                                mockedClient);
    HttpResponse mockedResponse = mock(HttpResponse.class);
    when(mockedClient.execute(any())).thenReturn(mockedResponse);
    StatusLine mockedStatus = mock(StatusLine.class);
    when(mockedResponse.getStatusLine()).thenReturn(mockedStatus);
    when(mockedStatus.getStatusCode()).thenReturn(HttpStatus.SC_ACCEPTED);

    // when
    telemetryReporter.reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Start telemetry sending task").size()).isOne();
    assertThat(loggingRule.getFilteredLog("Telemetry data sent").size()).isOne();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogUnexpectedResponse() throws ClientProtocolException, IOException {
    // given
    managementService.toggleTelemetry(true);
    HttpClient mockedClient = mock(HttpClient.class);
    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                createDataToSend(),
                                                                mockedClient);
    when(mockedClient.execute(any())).thenReturn(null);

    // when
    telemetryReporter.reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Unexpect response while sending telemetry data").size()).isOne();
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

  protected Data createDataToSend() {
    Database database = new Database("mySpecialDb", "v.1.2.3");
    Internals internals = new Internals(database);
    Product product = new Product("Runtime", "7.14", "special", internals);
    Data data = new Data("f5b19e2e-b49a-11ea-b3de-0242ac130004", product);
    return data;
  }
}
