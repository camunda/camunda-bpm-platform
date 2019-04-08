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
package org.camunda.connect.soap.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.protocol.HTTP;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.impl.HttpConnectorImpl;
import org.camunda.connect.httpclient.soap.SoapHttpConnector;
import org.camunda.connect.httpclient.soap.impl.SoapHttpConnectorImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Since Apache HTTP client makes it extremely hard to test the proper configuration
 * of a http client, this is more of an integration test that checks that a
 * system property is respected
 *
 * @author Thorben Lindhauer
 */
public class SoapHttpConnectorSystemPropertiesTest {

  public static final int PORT = 51234;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
      WireMockConfiguration.wireMockConfig().port(PORT));

  protected Set<String> updatedSystemProperties;

  @Before
  public void setUp() {
    updatedSystemProperties = new HashSet<String>();
    wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));
  }

  @After
  public void clearCustomSystemProperties() {
    for (String property : updatedSystemProperties) {
      System.getProperties().remove(property);
    }
  }

  public void setSystemProperty(String property, String value) {
    if (!System.getProperties().containsKey(property)) {
      updatedSystemProperties.add(property);
      System.setProperty(property, value);
    }
    else {
      throw new RuntimeException("Cannot perform test: System property "
          + property + " is already set. Will not attempt to overwrite this property.");
    }
  }

  @Test
  public void shouldSetUserAgentFromSystemProperty() {
    // given
    setSystemProperty("http.agent", "foo");

    SoapHttpConnector customConnector = new SoapHttpConnectorImpl();

    // when
    customConnector.createRequest().url("http://localhost:" + PORT).payload("test").execute();

    // then
    verify(postRequestedFor(urlEqualTo("/")).withHeader(HTTP.USER_AGENT, equalTo("foo")));

  }
}
