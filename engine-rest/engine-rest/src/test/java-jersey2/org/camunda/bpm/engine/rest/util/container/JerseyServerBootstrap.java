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
package org.camunda.bpm.engine.rest.util.container;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class JerseyServerBootstrap extends EmbeddedServerBootstrap {

  protected HttpServer server;

  public JerseyServerBootstrap() {
    super(new JaxrsApplication());
  }

  public JerseyServerBootstrap(Application application) {
    super(application);
  }

  @Override
  public void stop() {
    server.shutdownNow();
  }

  @Override
  protected void startServerInternal() throws Exception {
    server.start();
  }

  @Override
  protected void setupServer(Application application) {
    ResourceConfig rc = ResourceConfig.forApplication(application);

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ServerProperties.TRACING, "ALL");
    rc.addProperties(properties);

    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    URI serverUri = UriBuilder.fromPath(ROOT_RESOURCE_PATH).scheme("http").host("localhost").port(port).build();
    try {
      server = GrizzlyHttpServerFactory.createHttpServer(serverUri, rc, false);
    } catch (IllegalArgumentException e) {
      throw new ServerBootstrapException(e);
    } catch (NullPointerException e) {
      throw new ServerBootstrapException(e);
    }
  }
}
