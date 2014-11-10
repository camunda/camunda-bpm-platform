/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.integrationtest.functional.spin;

import static org.camunda.spin.Spin.*;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.spin.plugin.SpinProcessEnginePlugin;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Smoketest Make sure camunda spin can be used in a process application </p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class PaSpinSupportTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static Archive<?> createDeployment() {
    return processArchiveDeployment(initWebArchiveDeployment());
  }

  @Test
  public void spinShouldBeAvailable() {
    Assert.assertEquals("someXml", XML("<someXml />").xPath("/someXml").element().name());
  }

  @Test
  public void spinPluginShouldBeRegistered() {
    List<ProcessEnginePlugin> processEnginePlugins = processEngineConfiguration.getProcessEnginePlugins();

    boolean spinPluginFound = false;

    for (ProcessEnginePlugin plugin : processEnginePlugins) {
      if (plugin instanceof SpinProcessEnginePlugin) {
        spinPluginFound = true;
        break;
      }
    }

    Assert.assertTrue(spinPluginFound);
  }

}

