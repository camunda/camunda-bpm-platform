/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.webapp.impl.security.filter.headersec.util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;

/**
 * @author Tassilo Weidner
 */
public class HeaderSecRule extends ExternalResource {

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

  public void startServer(String webDescriptor) {
    webAppContext.setResourceBase("/");
    webAppContext.setDescriptor("src/test/resources/WEB-INF/headersec/" + webDescriptor);

    server.setHandler(webAppContext);

    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void performRequest() {
    try {
      connection = new URL("http://localhost:" + SERVER_PORT + "/").openConnection();
    } catch (IOException e) {
      throw new RuntimeException(e);
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
