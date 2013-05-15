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
public class CockpitEmbeddedBootstrap extends CockpitContainerBootstrap {

  @Override
  protected CockpitEnvironment createCockpitEnvironment() {
    return new CockpitEmbeddedEnvironment();
  }

  protected static class CockpitEmbeddedEnvironment extends CockpitEnvironment {

    private ProcessEngine processEngine;

    @Override
    public void tearDown() {
      System.out.println("Tearing down cockpit environment");

      super.tearDown();

      getContainerRuntimeDelegate().unregisterProcessEngine(processEngine);
      processEngine.close();
    }

    @Override
    public void setup() {
      System.out.println("Bootstrapping cockpit with " + CockpitEmbeddedEnvironment.class.getName());

      processEngine = createProcessEngine();
      getContainerRuntimeDelegate().registerProcessEngine(processEngine);

      super.setup();
    }

    protected ProcessEngine createProcessEngine() {
      ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
      processEngineConfiguration.setProcessEngineName("default");
      return processEngineConfiguration.buildProcessEngine();
    }
  }
}
