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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.telemetry.node.Data;
import org.camunda.bpm.engine.impl.telemetry.node.Database;
import org.camunda.bpm.engine.impl.telemetry.node.Internals;
import org.camunda.bpm.engine.impl.telemetry.node.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;

public class TelemetryReporterTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8083/pings";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl configuration;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
  }

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8083);

  @Test
  public void shouldSendTelemetry() {
    // given
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT))
              .willReturn(aResponse()
                          .withStatus(HttpStatus.SC_ACCEPTED)));

    Data data = createDataToSend();
    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                data,
                                                                HttpClientBuilder.create().build());
    telemetryReporter.initTelemetrySendingTask();

    // when
    telemetryReporter.reportNow();

    // then
    String requestBody = new Gson().toJson(data);
    verify(postRequestedFor(urlEqualTo("/pings"))
              .withRequestBody(equalToJson(requestBody))
              .withHeader("Content-Type",  equalTo("application/json")));

  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogTelemetryLog() throws ClientProtocolException, IOException {
    // given
    HttpClient mockedClient = mock(HttpClient.class);
    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                createDataToSend(),
                                                                mockedClient);
    telemetryReporter.initTelemetrySendingTask();
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
  public void shouldNotSendTelemetryLog() throws ClientProtocolException, IOException {
    // given
    HttpClient mockedClient = mock(HttpClient.class);
    TelemetryReporter telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                TELEMETRY_ENDPOINT,
                                                                createDataToSend(),
                                                                mockedClient);
    telemetryReporter.initTelemetrySendingTask();
    when(mockedClient.execute(any())).thenReturn(null);

    // when
    telemetryReporter.reportNow();

    // then
    assertThat(loggingRule.getFilteredLog("Unexpect response while sending telemetry data").size()).isOne();
  }

  protected Data createDataToSend() {
    Database database = new Database("mySpecialDb", "v.1.2.3");
    Internals internals = new Internals(database);
    Product product = new Product("Runtime", "7.14", "special", internals);
    Data data = new Data("f5b19e2e-b49a-11ea-b3de-0242ac130004", product);
    return data;
  }
}
