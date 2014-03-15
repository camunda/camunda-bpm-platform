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
package org.camunda.bpm.cockpit.test.sample.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.cockpit.impl.DefaultCockpitRuntimeDelegate;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;

/**
 *
 * @author nico.rehwaldt
 */
public class CockpitTestBootstrap implements ServletContextListener {

  private ProcessEngine processEngine;

  @Override
  public void contextInitialized(ServletContextEvent sce) {

    processEngine = createTestProcessEngine();

    RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine);

    Cockpit.setCockpitRuntimeDelegate(new DefaultCockpitRuntimeDelegate());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

    RuntimeContainerDelegate.INSTANCE.get().unregisterProcessEngine(processEngine);

    processEngine.close();
  }

  private ProcessEngine createTestProcessEngine() {
    ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

    processEngineConfiguration.setProcessEngineName("default");

    return processEngineConfiguration.buildProcessEngine();
  }
}
