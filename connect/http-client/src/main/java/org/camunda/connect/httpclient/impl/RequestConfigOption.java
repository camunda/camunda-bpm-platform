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
package org.camunda.connect.httpclient.impl;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.apache.hc.client5.http.config.RequestConfig.Builder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;

public enum RequestConfigOption {

  AUTHENTICATION_ENABLED("authentication-enabled",
      (builder, value) -> builder.setAuthenticationEnabled((boolean) value)),
  CIRCULAR_REDIRECTS_ALLOWED("circular-redirects-allowed",
      (builder, value) -> builder.setCircularRedirectsAllowed((boolean) value)),
  CONNECT_TIMEOUT("connect-timeout",
      (builder, value) -> builder.setConnectTimeout((Timeout) value)),
  CONNECTION_KEEP_ALIVE("connection-keep-alive",
      (builder, value) -> builder.setConnectionKeepAlive((Timeout) value)),
  CONNECTION_REQUEST_TIMEOUT("connection-request-timeout",
      (builder, value) -> builder.setConnectionRequestTimeout((Timeout) value)),
  CONTENT_COMPRESSION_ENABLED("content-compression-enabled",
      (builder, value) -> builder.setContentCompressionEnabled((boolean) value)),
  COOKIE_SPEC("cookie-spec",
      (builder, value) -> builder.setCookieSpec((String) value)),
  EXPECT_CONTINUE_ENABLED("expect-continue-enabled",
      (builder, value) -> builder.setExpectContinueEnabled((boolean) value)),
  HARD_CANCELLATION_ENABLED("hard-cancellation-enabled",
      (builder, value) -> builder.setHardCancellationEnabled((boolean) value)),
  MAX_REDIRECTS("max-redirects",
      (builder, value) -> builder.setMaxRedirects((int) value)),
  PROXY("proxy",
      (builder, value) -> builder.setProxy((HttpHost) value)),
  PROXY_PREFERRED_AUTH_SCHEMES("proxy-preferred-auth-scheme",
      (builder, value) -> builder.setProxyPreferredAuthSchemes((Collection<String>) value)),
  REDIRECTS_ENABLED("redirects-enabled",
      (builder, value) -> builder.setRedirectsEnabled((boolean) value)),
  RESPONSE_TIMEOUT("response-timeout",
      (builder, value) -> builder.setResponseTimeout((Timeout) value)),
  TARGET_PREFERRED_AUTH_SCHEMES("target-preferred-auth-schemes",
      (builder, value) -> builder.setTargetPreferredAuthSchemes((Collection<String>) value));

  private final String name;
  private final BiConsumer<Builder, Object> consumer;

  RequestConfigOption(String name, BiConsumer<Builder, Object> consumer) {
    this.name = name;
    this.consumer = consumer;
  }

  public String getName() {
    return name;
  }

  public void apply(Builder configBuilder, Object value) {
    consumer.accept(configBuilder, value);
  }

}
