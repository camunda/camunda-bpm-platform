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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Tassilo Weidner
 */
public class HeaderRule extends ExternalResource {

  protected static final int SERVER_PORT = 8085;

  protected Server server = new Server(SERVER_PORT);
  protected WebAppContext webAppContext = new WebAppContext();
  protected URLConnection connection = null;

  protected void before() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void after() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void startServer(String webDescriptor, String scope) {
    webAppContext.setResourceBase("/");
    webAppContext.setDescriptor("src/test/resources/WEB-INF/" + scope + "/" + webDescriptor);

    server.setHandler(webAppContext);

    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void performRequest() {
    performRequestWithHeader(null, null);
  }

  public void performRequestWithHeader(String name, String value) {
    try {
      connection = new URL("http://localhost:" + SERVER_PORT + "/").openConnection();
    } catch (IOException e) {
      throw new RuntimeException(e);
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

}
