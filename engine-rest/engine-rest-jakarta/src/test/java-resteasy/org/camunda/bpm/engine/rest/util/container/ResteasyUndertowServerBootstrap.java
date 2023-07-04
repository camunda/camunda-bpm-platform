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

import io.undertow.servlet.api.DeploymentInfo;
import java.net.BindException;
import java.util.Properties;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

public class ResteasyUndertowServerBootstrap extends AbstractServerBootstrap {

  protected UndertowJaxrsServer server;
  protected DeploymentInfo deploymentInfo;

  public ResteasyUndertowServerBootstrap(DeploymentInfo deploymentInfo) {
    this.deploymentInfo = deploymentInfo;
    setupServer();
  }

  @Override
  public void stop() {
    this.server.stop();
  }

  @Override
  protected void startServer(int startUpRetries) {
    try {
      this.server.start();
    } catch (Exception e) {
      if ((e instanceof BindException || e.getCause() instanceof BindException) && startUpRetries > 0) {
        stop();
        try {
          Thread.sleep(1500L);
        } catch (Exception ex) {
        }
        setupServer();
        startServer(--startUpRetries);
      } else {
        throw new ServerBootstrapException(e);
      }
    }
  }

  protected void setupServer() {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));

    this.server = new UndertowJaxrsServer();
    this.server.setPort(port);
    this.server.deploy(this.deploymentInfo);
  }
}
