/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.impl.web.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.impl.DefaultRuntimeDelegate;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;

/**
 * A servlet context listener that bootstraps cockpit with an embedded process engine.
 *
 * <p>
 *
 * Use for testing and development purposes only.
 *
 * @author nico.rehwaldt
 */
public class CockpitEmbeddedBootstrap implements ServletContextListener {

  private CockpitEnvironment environment;

  @Override
  public void contextInitialized(ServletContextEvent sce) {

    environment = createCockpitEnvironment();
    environment.setup();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

    environment.tearDown();
  }

  protected CockpitEnvironment createCockpitEnvironment() {
    return new CockpitEnvironment();
  }

  protected static class CockpitEnvironment {

    private ProcessEngine processEngine;

    private void tearDown() {

      System.out.println("Tearing down cockpit environment");

      Cockpit.setCockpitRuntimeDelegate(null);

      getContainerRuntimeDelegate().unregisterProcessEngine(processEngine);
      processEngine.close();
    }

    private void setup() {

      System.out.println("Bootstrapping cockpit with " + CockpitEmbeddedBootstrap.class.getName());

      processEngine = createProcessEngine();
      getContainerRuntimeDelegate().registerProcessEngine(processEngine);

      Cockpit.setCockpitRuntimeDelegate(new DefaultRuntimeDelegate());
    }

    protected ProcessEngine createProcessEngine() {
      ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
      processEngineConfiguration.setProcessEngineName("default");
      return processEngineConfiguration.buildProcessEngine();
    }

    protected RuntimeContainerDelegate getContainerRuntimeDelegate() {
      return RuntimeContainerDelegate.INSTANCE.get();
    }
  }
}
