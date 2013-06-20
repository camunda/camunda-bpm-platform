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
package org.camunda.bpm.cockpit.test.plugin;

import static org.fest.assertions.Assertions.assertThat;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.test.sample.plugin.simple.SimplePlugin;
import org.camunda.bpm.cockpit.test.util.AbstractCockpitCoreTest;
import org.camunda.bpm.cockpit.test.util.DeploymentHelper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Execution;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(Arquillian.class)
public class CockpitDefaultRuntimeDelegateTest extends AbstractCockpitCoreTest {

  @Deployment
  public static Archive<?> createDeployment() {

    WebArchive archive = createBaseDeployment();

    return archive;
  }

  @Test
  public void shouldProvideDefaultProcessEngine () {
    // given
    ProcessEngine platformDefaultProcessEngine = BpmPlatform.getDefaultProcessEngine();

    // when
    ProcessEngine engine = Cockpit.getRuntimeDelegate().getDefaultProcessEngine();

    // then
    assertThat(engine).isEqualTo(platformDefaultProcessEngine);
  }

  @Test
  public void shouldProvideProcessEngineByName() {
    // given
    ProcessEngine platformNamedProcessEngine = BpmPlatform.getProcessEngineService().getProcessEngine("default");

    // when
    ProcessEngine engine = Cockpit.getRuntimeDelegate().getProcessEngine("default");

    // then
    assertThat(engine).isEqualTo(platformNamedProcessEngine);
  }

  @Test
  public void shouldProvideProcessEngineNames() {
    // given
    Set<String> platformEngineNames = BpmPlatform.getProcessEngineService().getProcessEngineNames();

    // when
    Set<String> engineNames = Cockpit.getRuntimeDelegate().getProcessEngineNames();

    // then
    assertThat(engineNames).isEqualTo(platformEngineNames);
  }
}
