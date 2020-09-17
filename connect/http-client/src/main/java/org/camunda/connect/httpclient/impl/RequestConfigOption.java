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

import java.net.InetAddress;
import java.util.Collection;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;

public enum RequestConfigOption {

  AUTHENTICATION_ENABLED("authentication-enabled") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setAuthenticationEnabled((boolean) value);
    }
  },
  CIRCULAR_REDIRECTS_ALLOWED("circular-redirects-allowed") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setCircularRedirectsAllowed((boolean) value);
    }
  },
  CONNECTION_TIMEOUT("connection-timeout") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setConnectTimeout((int) value);
    }
  },
  CONNECTION_REQUEST_TIMEOUT("connection-request-timeout") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setConnectionRequestTimeout((int) value);
    }
  },
  CONTENT_COMPRESSION_ENABLED("content-compression-enabled") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setContentCompressionEnabled((boolean) value);
    }
  },
  COOKIE_SPEC("cookie-spec") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setCookieSpec((String) value);
    }
  },
  DECOMPRESSION_ENABLED("decompression-enabled") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setDecompressionEnabled((boolean) value);
    }
  },
  EXPECT_CONTINUE_ENABLED("expect-continue-enabled") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setExpectContinueEnabled((boolean) value);
    }
  },
  LOCAL_ADDRESS("local-address") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setLocalAddress((InetAddress) value);
    }
  },
  MAX_REDIRECTS("max-redirects") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setMaxRedirects((int) value);
    }
  },
  NORMALIZE_URI("normalize-uri") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setNormalizeUri((boolean) value);
    }
  },
  PROXY("proxy") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setProxy((HttpHost) value);
    }
  },
  PROXY_PREFERRED_AUTH_SCHEMES("proxy-preferred-auth-scheme") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setProxyPreferredAuthSchemes((Collection<String>) value);
    }
  },
  REDIRECTS_ENABLED("relative-redirects-allowed") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setRedirectsEnabled((boolean) value);
    }
  },
  RELATIVE_REDIRECTS_ALLOWED("relative-redirects-allowed") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setRelativeRedirectsAllowed((boolean) value);
    }
  },
  SOCKET_TIMEOUT("socket-timeout") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setSocketTimeout((int) value);
    }
  },
  STALE_CONNECTION_CHECK_ENABLED("stale-connection-check-enabled") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setStaleConnectionCheckEnabled((boolean) value);
    }
  },
  TARGET_PREFERRED_AUTH_SCHEMES("target-preferred-auth-schemes") {
    @Override
    public void apply(Builder configBuilder, Object value) {
      configBuilder.setTargetPreferredAuthSchemes((Collection<String>) value);
    }
  };

  private String name;

  private RequestConfigOption(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public abstract void apply(Builder configBuilder, Object value);
}
