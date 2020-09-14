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

import java.net.HttpURLConnection;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Simulates cluster setups where multiple engines are supposed to send telemetry.
 *
 * Uses Wiremock so should be run as part of {@link TelemetrySuiteTest}.
 */
public class TelemetryMultipleEnginesSuiteElement {

  protected static final String TELEMETRY_ENDPOINT_PATH = "/pings";
  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8081" + TELEMETRY_ENDPOINT_PATH;

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8081);

  @ClassRule
  public static ProcessEngineBootstrapRule secondEngineRule =
      new ProcessEngineBootstrapRule(configuration ->
          configuration.setTelemetryEndpoint(TELEMETRY_ENDPOINT)
            );

  protected ProcessEngineRule defaultEngineRule = new ProvidedProcessEngineRule();

  protected ProcessEngine defaultEngine;
  protected ProcessEngine secondEngine;

  @Before
  public void init() {
    defaultEngine = defaultEngineRule.getProcessEngine();
    secondEngine = secondEngineRule.getProcessEngine();
  }

  /**
   * When telemetry is toggled on one engine, then the other engine should
   * pick that up and also start sending telemetry.
   */
  @Test
  public void shouldPickUpTelemetryActivation() {
    // when
    defaultEngine.getManagementService().toggleTelemetry(true);

    ProcessEngineConfigurationImpl secondEngineConfiguration = (ProcessEngineConfigurationImpl)
        secondEngine.getProcessEngineConfiguration();
    TelemetryReporter telemetryReporter = secondEngineConfiguration.getTelemetryReporter();

    stubFor(post(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .willReturn(aResponse()
            .withStatus(HttpURLConnection.HTTP_ACCEPTED)));


    // when
    telemetryReporter.reportNow();

    // then
    // the second engine reports its metrics
    verify(postRequestedFor(urlEqualTo(TELEMETRY_ENDPOINT_PATH))
        .withHeader("Content-Type",  equalTo("application/json")));

  }

  @After
  public void tearDown() {
    WireMock.resetAllRequests();
  }
}
