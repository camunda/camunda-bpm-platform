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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Database;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
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

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8082/pings";
  private static final String TELEMETRY_ENDPOINT_URL = "/pings";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(configuration ->
      configuration.setTelemetryEndpoint(TELEMETRY_ENDPOINT));

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8082);

  protected ProcessEngine standaloneProcessEngine;
  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = configuration.getManagementService();

    configuration.setTelemetryRegistry(new TelemetryRegistry());
  }

  @After
  public void tearDown() {
    managementService.toggleTelemetry(false);

    if (standaloneProcessEngine != null) {
      standaloneProcessEngine.close();
      ProcessEngines.unregister(standaloneProcessEngine);
    }
  }

  @Test
  public void shouldSendTelemetry() {
    // given
    managementService.toggleTelemetry(true);
    Data data = createDataToSend();
    String requestBody = new Gson().toJson(data);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
        .willReturn(aResponse()
                    .withStatus(HttpURLConnection.HTTP_ACCEPTED)));
    standaloneProcessEngine = processEngineConfiguration.buildProcessEngine();
  
    // when
    processEngineConfiguration.getTelemetryReporter().reportNow();
  
    // then
    String requestBody = new Gson().toJson(data);
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_URL))
              .withRequestBody(equalToJson(requestBody))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  @Test
  public void shouldSendTelemetryWhenHttpConnectorNotInitialized() {
    // given
    managementService.toggleTelemetry(true);
    Data data = createDataToSend();
    String requestBody = new Gson().toJson(data);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
            .willReturn(aResponse()
                        .withBody(requestBody)
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
        .willReturn(aResponse()
            .withBody(requestBody)
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    // when
    configuration.getTelemetryReporter().reportNow();

    // then
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_URL))
        .withRequestBody(equalToJson(requestBody))
        .withHeader("Content-Type",  equalTo("application/json")));
    assertThat(configuration.getTelemetryRegistry().getCommands().size()).isEqualTo(1);
    configuration.getTelemetryRegistry().getCommands();
  }

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.telemetry"}, level = "DEBUG")
  public void shouldLogTelemetrySent() {
    // given
    managementService.toggleTelemetry(true);
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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
    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_URL))
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

  protected Data createDataToSend() {
    Database database = new Database("mySpecialDb", "v.1.2.3");
    Internals internals = new Internals(database, new ApplicationServer("Apache Tomcat/10.0.1"));
    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);
    Product product = new Product("Runtime", "7.14.0", "special", internals);
    Data data = new Data("f5b19e2e-b49a-11ea-b3de-0242ac130004", product);
    return data;
  }

  protected Data adjustDataWithProductVersionAndAppServerInfo(Data telemetryData, String applicationServerVersion) {
    Data resultData = new Data(telemetryData.getInstallation(), telemetryData.getProduct());

    Product product = resultData.getProduct();
    product.setVersion("7.14.0");
    resultData.setProduct(product);

    Internals internals = resultData.getProduct().getInternals();

    internals.setApplicationServer(new ApplicationServer(applicationServerVersion));
    Map<String, Command> commands = getDefaultCommandCounts();
    internals.setCommands(commands);

    return resultData;
  }

  protected Data adjustDataWithCommandCounts(Data telemetryData) {
    Data resultData = adjustDataWithProductVersionAndAppServerInfo(telemetryData, "Wildfly 10");


    Map<String, Command> commands = resultData.getProduct().getInternals().getCommands();
    commands.put("GetHistoryLevelCmd", new Command(1));
    commands.put("GetLicenseKeyCmd", new Command(1));
    resultData.getProduct().getInternals().setCommands(commands);

    return telemetryData;
  }

  protected Map<String, Command> getDefaultCommandCounts() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("TelemetryConfigureCmd", new Command(1));
    commands.put("IsTelemetryEnabledCmd", new Command(1));
    return commands;
  }
}
