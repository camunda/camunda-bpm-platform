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

import io.undertow.servlet.Servlets;
import jakarta.servlet.DispatcherType;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

import java.util.Properties;

public class ResteasyUndertowServerBootstrap extends EmbeddedServerBootstrap {

  private UndertowJaxrsServer server;

  public ResteasyUndertowServerBootstrap() {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));

    this.server = new UndertowJaxrsServer();
    this.server.setPort(port);

    this.server.deploy(Servlets.deployment()
        .setDeploymentName("rest-test.war")
        .setContextPath("/rest-test")
        .setClassLoader(ResteasyUndertowServerBootstrap.class.getClassLoader())
        //.addListener(Servlets.listener(ResteasyBootstrap.class))
        .addFilter(Servlets.filter("camunda-auth", ProcessEngineAuthenticationFilter.class)
            .addInitParam("authentication-provider", "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider")
        )
        .addFilterUrlMapping("camunda-auth", "/rest/*", DispatcherType.REQUEST)
        .addServlet(Servlets.servlet("camunda-app", HttpServletDispatcher.class)
            .addMapping("/rest/*")
            .addInitParam("jakarta.ws.rs.Application", "org.camunda.bpm.engine.rest.util.container.JaxrsApplication")
        )
    );
  }

  @Override
  public void start() {
    this.server.start();
  }

  @Override
  public void stop() {
    this.server.stop();
  }
}
