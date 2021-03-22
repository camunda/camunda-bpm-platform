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
package org.camunda.bpm.integrationtest.functional.telemetry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.junit.Assert.assertNotNull;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Database;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Metric;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@RunWith(Arquillian.class)
public class TelemetryConnectPluginTest extends AbstractFoxPlatformIntegrationTest {

  ProcessEngine engineConnect;
  ProcessEngineConfigurationImpl configuration;
  WireMockServer wireMockServer;
  TelemetryReporter telemetryReporter;

  @Before
  public void setEngines() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    engineConnect = engineService.getProcessEngine("engine-connect-1");
    configuration = (ProcessEngineConfigurationImpl) engineConnect.getProcessEngineConfiguration();

    // clean up the recorded commands
    configuration.getTelemetryRegistry().clear();

  }

  @After
  public void tearDown() {
    configuration.getManagementService().toggleTelemetry(false);
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
    if (telemetryReporter!=null) {
      telemetryReporter.stop(false);
      telemetryReporter = null;
    }
  }

  @Deployment(name="myDeployment")
  public static WebArchive createDeployment() {
    final WebArchive webArchive =
        initWebArchiveDeployment("paConnectPlugin.war", "org/camunda/bpm/integrationtest/telemetry/processes-connectPlugin.xml")
        .addAsLibraries(Maven.resolver()
            .offline()
            .loadPomFromFile("pom.xml")
            .resolve("com.github.tomakehurst:wiremock-standalone")
            .withTransitivity()
            .as(JavaArchive.class));

    TestContainer.addContainerSpecificProcessEngineConfigurationClass(webArchive);
    return webArchive;
  }

  @Test
  @OperateOnDeployment("myDeployment")
  public void shouldInitializeHttpConnector() {
    // when

    // then
    assertNotNull(configuration.getTelemetryHttpConnector());
  }

  @Test
  @OperateOnDeployment("myDeployment")
  public void shouldSendTelemetryData() {
    // given
    configuration.getManagementService().toggleTelemetry(true);
    wireMockServer = new WireMockServer(WireMockConfiguration.options().port(18090));
    wireMockServer.start();
    Data data = createDataToSend();

    String requestBody = JsonUtil.asString(data);
    wireMockServer.stubFor(post(urlEqualTo("/pings"))
            .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_ACCEPTED)));

    telemetryReporter = new TelemetryReporter(configuration.getCommandExecutorTxRequired(),
                                                                configuration.getTelemetryEndpoint(),
                                                                0,
                                                                configuration.getTelemetryReportingPeriod(),
                                                                data,
                                                                configuration.getTelemetryHttpConnector(),
                                                                configuration.getTelemetryRegistry(),
                                                                configuration.getMetricsRegistry(),
                                                                configuration.getTelemetryRequestTimeout());

    // when
    telemetryReporter.reportNow();

    // then
    wireMockServer.verify(postRequestedFor(urlEqualTo("/pings"))
              .withRequestBody(equalToJson(requestBody, true, true))
              .withHeader("Content-Type",  equalTo("application/json")));
  }

  protected Data createDataToSend() {
    Database database = new Database("mySpecialDb", "v.1.2.3");
    Internals internals = new Internals(database, new ApplicationServer("Apache Tomcat/10.0.1"), null, ParseUtil.parseJdkDetails());

    Map<String, Command> commands = configuration.getTelemetryData().getProduct().getInternals().getCommands();
    internals.setCommands(commands);

    Map<String, Metric> metrics = getDefaultMetrics();
    internals.setMetrics(metrics);
    internals.setTelemetryEnabled(true);

    internals.setWebapps(Collections.emptySet());
    Product product = new Product("Runtime", "7.14.0", "special", internals);
    Data data = new Data("cb07ce31-c8e3-4f5f-94c2-1b28175c2022", product);
    return data;
  }

  protected Map<String, Metric> getDefaultMetrics() {
    return assembleMetrics(0, 0, 0);
  }
  protected Map<String, Metric> assembleMetrics(long processCount, long decisionCount, long flowNodeCount) {
    Map<String, Metric> metrics = new HashMap<>();
    metrics.put(ACTIVTY_INSTANCE_START, new Metric(processCount));
    metrics.put(EXECUTED_DECISION_INSTANCES, new Metric(decisionCount));
    metrics.put(ACTIVTY_INSTANCE_START, new Metric(flowNodeCount));
    return metrics;
  }

}
