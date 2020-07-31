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
package org.camunda.bpm.spring.boot.starter.webapp.filter.util;

import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HeaderRule extends ExternalResource {

  public static final String PORT_PLACEHOLDER_WEBAPP_URL = "{PORT}";
  public static final String WEBAPP_URL = "http://localhost:" + PORT_PLACEHOLDER_WEBAPP_URL +
      "/camunda/app/tasklist/default";

  protected Integer port = null;
  protected HttpURLConnection connection = null;

  public HeaderRule() {
  }

  public HeaderRule(int port) {
    this.port = port;
  }

  @Override
  protected void after() {
    port = null;
    connection = null;
  }

  public URLConnection performRequest() {
    return performRequest(WEBAPP_URL.replace(PORT_PLACEHOLDER_WEBAPP_URL, String.valueOf(port)), null, null, null);
  }

  public URLConnection performRequest(String url) {
    return performRequest(url, null, null, null);
  }

  public URLConnection performPostRequest(String url, String headerName, String headerValue) {
    return performRequest(url, "POST", headerName, headerValue);
  }

  public URLConnection performRequest(String url, String method, String headerName, String headerValue) {
    try {
      connection =
        (HttpURLConnection) new URL(url)
          .openConnection();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    connection.setInstanceFollowRedirects(false);

    if ("POST".equals(method)) {
      try {
        connection.setRequestMethod("POST");
      } catch (ProtocolException e) {
        throw new RuntimeException(e);
      }
    }

    if (headerName != null && headerValue != null) {
      connection.setRequestProperty(headerName, headerValue);
    }

    try {
      connection.connect();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return connection;
  }

  public List<String> getCookieHeaders() {
    return getHeaders("Set-Cookie");
  }

  public String getHeaderXsrfToken() {
    return connection.getHeaderField("X-XSRF-TOKEN");
  }

  public String getXsrfTokenHeader() {
    return getHeaderXsrfToken();
  }

  public String getCookieValue(String cookieName) {
    List<String> cookies = getCookieHeaders();

    for (String cookie : cookies) {
      if (cookie.startsWith(cookieName + "=")) {
        return cookie;
      }
    }

    return "";
  }

  public String getXsrfCookieValue() {
    return getCookieValue("XSRF-TOKEN");
  }

  public String getErrorResponseContent() {
    try {
      StringWriter writer = new StringWriter();
      IOUtils.copy(connection.getErrorStream(), writer, "UTF-8");
      return writer.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<String> getHeaders(String name) {
    Map<String, List<String>> headerFields = connection.getHeaderFields();
    return headerFields.get(name);
  }

  public String getHeader(String name) {
    return getHeaders(name).get(0);
  }

  public boolean headerExists(String name) {
    return getHeaders(name) != null;
  }

}
