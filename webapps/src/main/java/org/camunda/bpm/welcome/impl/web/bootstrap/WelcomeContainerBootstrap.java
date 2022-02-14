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
package org.camunda.bpm.welcome.impl.web.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.welcome.Welcome;
import org.camunda.bpm.welcome.impl.DefaultWelcomeRuntimeDelegate;

/**
 * @author Daniel Meyer
 *
 */
public class WelcomeContainerBootstrap implements ServletContextListener {

  private WelcomeEnvironment environment;

  @Override
  public void contextInitialized(ServletContextEvent sce) {

    environment = createWelcomeEnvironment();
    environment.setup();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

    environment.tearDown();
  }

  protected WelcomeEnvironment createWelcomeEnvironment() {
    return new WelcomeEnvironment();
  }

  protected static class WelcomeEnvironment {

    public void tearDown() {
      Welcome.setRuntimeDelegate(null);
    }

    public void setup() {
      Welcome.setRuntimeDelegate(new DefaultWelcomeRuntimeDelegate());
    }

    protected RuntimeContainerDelegate getContainerRuntimeDelegate() {
      return RuntimeContainerDelegate.INSTANCE.get();
    }
  }
}
