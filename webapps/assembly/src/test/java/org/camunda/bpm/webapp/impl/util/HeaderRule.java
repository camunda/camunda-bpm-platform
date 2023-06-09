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
package org.camunda.bpm.webapp.impl.util;

import java.io.IOException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.rules.ExternalResource;

/**
 * @author Tassilo Weidner
 */
public class HeaderRule extends ExternalResource {

  protected static final int SERVER_PORT = 8085;
  protected static final int RETRIES = 3;

  protected Server server = new Server(SERVER_PORT);
  protected WebAppContext webAppContext = new WebAppContext();
  protected HttpURLConnection connection = null;

  @Override
  protected void before() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void after() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void startServer(String webDescriptor, String scope) {
    startServer(webDescriptor, scope, "/camunda");
  }

  public void startServer(String webDescriptor, String scope, String contextPath) {
    startServer(webDescriptor, scope, contextPath, RETRIES);
  }

  protected void startServer(String webDescriptor, String scope, String contextPath, int startUpRetries) {
    webAppContext.setContextPath(contextPath);
    webAppContext.setResourceBase("/");
    webAppContext.setDescriptor("src/test/resources/WEB-INF/" + scope + "/" + webDescriptor);

    server.setHandler(webAppContext);

    try {
      server.start();
    } catch (Exception e) {
      if (e.getCause() instanceof BindException && startUpRetries > 0) {
        try {
          Thread.sleep(500L);
        } catch (Exception ex) {
        }
        startServer(webDescriptor, scope, contextPath, --startUpRetries);
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  public void performRequest() {
    performRequestWithHeader(null, null, "", null);
  }

  public void performPostRequest(String path) {
    performRequestWithHeader(null, null, path, "POST");
  }

  public void performRequestWithHeader(String name, String value) {
    performRequestWithHeader(name, value, "", null);
  }

  public void performRequestWithHeader(String name, String value, String path, String method) {
    try {
      connection =
        (HttpURLConnection) new URL("http://localhost:" + SERVER_PORT + "/camunda" + path)
          .openConnection();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if ("POST".equals(method)) {
      try {
        connection.setRequestMethod("POST");
      } catch (ProtocolException e) {
        throw new RuntimeException(e);
      }
    }

    if (name != null && value != null) {
      connection.setRequestProperty(name, value);
    }

    try {
      connection.connect();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getHeader(String headerName) {
    return connection.getHeaderField(headerName);
  }

  public String getCookieHeader() {
    return connection.getHeaderField("Set-Cookie");
  }

  public Throwable getException() {
    return webAppContext.getUnavailableException();
  }

  public boolean headerExists(String name) {
    for (String key : connection.getHeaderFields().keySet()) {
      if (name.equals(key)) {
        return true;
      }
    }

    return false;
  }

  public String getResponseBody() {
    try {
      return connection.getResponseMessage();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int getResponseCode() {
    try {
      return connection.getResponseCode();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getSessionCookieRegex(String path, String sameSite, boolean secure) {
    return getSessionCookieRegex(path, "JSESSIONID", sameSite, secure);
  }

  public String getSessionCookieRegex(String path, String cookieName, String sameSite, boolean secure) {
    StringBuilder regex = new StringBuilder(cookieName + "=.*;\\W*Path=/");
    if (path != null) {
      regex.append(path);
    }
    if (sameSite != null) {
      regex.append(";\\W*SameSite=").append(sameSite);
    }
    if (secure) {
      regex.append(";\\W*Secure");
    }
    return regex.toString();
  }

}
