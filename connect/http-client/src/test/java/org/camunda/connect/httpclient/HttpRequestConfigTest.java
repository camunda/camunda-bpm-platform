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
package org.camunda.connect.httpclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.AUTHENTICATION_ENABLED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.CIRCULAR_REDIRECTS_ALLOWED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.CONNECTION_REQUEST_TIMEOUT;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.CONNECTION_TIMEOUT;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.CONTENT_COMPRESSION_ENABLED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.COOKIE_SPEC;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.DECOMPRESSION_ENABLED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.EXPECT_CONTINUE_ENABLED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.LOCAL_ADDRESS;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.MAX_REDIRECTS;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.NORMALIZE_URI;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.PROXY;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.PROXY_PREFERRED_AUTH_SCHEMES;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.REDIRECTS_ENABLED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.RELATIVE_REDIRECTS_ALLOWED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.SOCKET_TIMEOUT;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.STALE_CONNECTION_CHECK_ENABLED;
import static org.camunda.connect.httpclient.impl.RequestConfigOption.TARGET_PREFERRED_AUTH_SCHEMES;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.conn.ConnectTimeoutException;
import org.camunda.connect.ConnectorRequestException;
import org.camunda.connect.httpclient.impl.HttpConnectorImpl;
import org.camunda.connect.httpclient.impl.util.ParseUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public class HttpRequestConfigTest {

  public static final String EXAMPLE_URL = "http://camunda.org/example";
  public static final String EXAMPLE_CONTENT_TYPE = "application/json";
  public static final String EXAMPLE_PAYLOAD = "camunda";

  protected HttpConnector connector;

  @Before
  public void createConnector() {
    connector = new HttpConnectorImpl();
  }

  @Test
  public void shouldParseAuthenticationEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(AUTHENTICATION_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isAuthenticationEnabled()).isFalse();
  }

  @Test
  public void shouldParseCircularRedirectsAllowed() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(CIRCULAR_REDIRECTS_ALLOWED.getName(), true);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isCircularRedirectsAllowed()).isTrue();
  }

  @Test
  public void shouldParseConnectionTimeout() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(CONNECTION_TIMEOUT.getName(), -2);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getConnectTimeout()).isEqualTo(-2);
  }

  @Test
  public void shouldParseConnectionRequestTimeout() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(CONNECTION_REQUEST_TIMEOUT.getName(), -2);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getConnectionRequestTimeout()).isEqualTo(-2);
  }

  @Test
  public void shouldParseContentCompressionEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(CONTENT_COMPRESSION_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isContentCompressionEnabled()).isFalse();
  }

  @Test
  public void shouldParseCookieSpec() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(COOKIE_SPEC.getName(), "test");
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getCookieSpec()).isEqualTo("test");
  }

  @Test
  public void shouldParseDecompressionEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(DECOMPRESSION_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isDecompressionEnabled()).isFalse();
  }

  @Test
  public void shouldParseExpectContinueEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(EXPECT_CONTINUE_ENABLED.getName(), true);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isExpectContinueEnabled()).isTrue();
  }

  @Test
  public void shouldParseLocalAddress() throws UnknownHostException {
    // given
    InetAddress testAddress = InetAddress.getByName("127.0.0.1");
    HttpRequest request = connector.createRequest()
        .configOption(LOCAL_ADDRESS.getName(), testAddress);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getLocalAddress()).isEqualTo(testAddress);
  }

  @Test
  public void shouldParseMaxRedirects() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(MAX_REDIRECTS.getName(), -2);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getMaxRedirects()).isEqualTo(-2);
  }

  @Test
  public void shouldParseNormalizeUri() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(NORMALIZE_URI.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isNormalizeUri()).isFalse();
  }

  @Test
  public void shouldParseProxy() {
    // given
    HttpHost testHost = new HttpHost("test");
    HttpRequest request = connector.createRequest()
        .configOption(PROXY.getName(), testHost);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getProxy()).isEqualTo(testHost);
  }

  @Test
  public void shouldParseProxyPreferredAuthSchemes() {
    // given
    ArrayList<String> testArray = new ArrayList<String>();
    HttpRequest request = connector.createRequest()
        .configOption(PROXY_PREFERRED_AUTH_SCHEMES.getName(), testArray);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getProxyPreferredAuthSchemes()).isEqualTo(testArray);
  }

  @Test
  public void shouldParseRedirectsEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(REDIRECTS_ENABLED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isRedirectsEnabled()).isFalse();
  }

  @Test
  public void shouldParseRelativeRedirectsAllowed() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(RELATIVE_REDIRECTS_ALLOWED.getName(), false);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isRelativeRedirectsAllowed()).isFalse();
  }


  @Test
  public void shouldParseSocketTimeout() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(SOCKET_TIMEOUT.getName(), -2);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getSocketTimeout()).isEqualTo(-2);
  }


  @Test
  public void shouldParseStaleConnectionCheckEnabled() {
    // given
    HttpRequest request = connector.createRequest()
        .configOption(STALE_CONNECTION_CHECK_ENABLED.getName(), true);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.isStaleConnectionCheckEnabled()).isTrue();
  }


  @Test
  public void shouldParseTargetPreferredAuthSchemes() {
    // given
    ArrayList<String> testArray = new ArrayList<String>();
    HttpRequest request = connector.createRequest()
        .configOption(TARGET_PREFERRED_AUTH_SCHEMES.getName(), testArray);
    Map<String, Object> configOptions = request.getConfigOptions();

    Builder configBuilder = RequestConfig.custom();
    ParseUtil.parseConfigOptions(configOptions, configBuilder);

    // when
    RequestConfig config = configBuilder.build();

    // then
    assertThat(config.getTargetPreferredAuthSchemes()).isEqualTo(testArray);
  }

  @Test
  public void shouldNotChangeDefaultConfig() {
    // given
    HttpClient client = (HttpClient) Whitebox.getInternalState(connector, "httpClient");
    connector.createRequest().url(EXAMPLE_URL).get()
        .configOption(CONNECTION_TIMEOUT.getName(), -2)
        .configOption(SOCKET_TIMEOUT.getName(), -2)
        .configOption(CONNECTION_REQUEST_TIMEOUT.getName(), -2)
        .configOption(MAX_REDIRECTS.getName(), 0)
        .execute();

    // when
    RequestConfig config = (RequestConfig) Whitebox.getInternalState(client, "defaultConfig");

    // then
    assertThat(config.getMaxRedirects()).isEqualTo(50);
    assertThat(config.getConnectTimeout()).isEqualTo(-1);
    assertThat(config.getConnectionRequestTimeout()).isEqualTo(-1);
    assertThat(config.getSocketTimeout()).isEqualTo(-1);
  }

  @Test
  public void shouldThrowTimeoutException() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(CONNECTION_TIMEOUT.getName(), 1)
          .execute();
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Unable to execute HTTP request");
      assertThat(e).hasCauseExactlyInstanceOf(ConnectTimeoutException.class);
    }
  }

  @Test
  public void shouldThrowClassCastExceptionStringToInt() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(CONNECTION_TIMEOUT.getName(), "-1")
          .execute();
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + CONNECTION_TIMEOUT.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.String cannot be cast to class java.lang.Integer");
    }
  }

  @Test
  public void shouldThrowClassCastExceptionStringToBoolean() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
          .configOption(AUTHENTICATION_ENABLED.getName(), "true")
          .execute();
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + AUTHENTICATION_ENABLED.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.String cannot be cast to class java.lang.Boolean");
    }
  }

  @Test
  public void shouldThrowClassCastExceptionStringToHttpHost() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
      .configOption(PROXY.getName(), "proxy")
      .execute();
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + PROXY.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.String cannot be cast to class org.apache.http.HttpHost");
    }
  }

  @Test
  public void shouldThrowClassCastExceptionIntToHttpHost() {
    try {
      // when
      connector.createRequest().url(EXAMPLE_URL).get()
      .configOption(PROXY_PREFERRED_AUTH_SCHEMES.getName(), 0)
      .execute();
    } catch (ConnectorRequestException e) {
      // then
      assertThat(e).hasMessageContaining("Invalid value for request configuration option: " + PROXY_PREFERRED_AUTH_SCHEMES.getName());
      assertThat(e).hasCauseInstanceOf(ClassCastException.class);
      assertThat(e.getCause()).hasMessageContaining("java.lang.Integer cannot be cast to class java.util.Collection");
    }
  }

}
