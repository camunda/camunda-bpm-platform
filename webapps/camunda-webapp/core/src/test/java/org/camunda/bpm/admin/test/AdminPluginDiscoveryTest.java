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
package org.camunda.bpm.admin.test;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.impl.DefaultAdminRuntimeDelegate;
import org.camunda.bpm.admin.plugin.spi.AdminPlugin;
import org.camunda.bpm.admin.test.sample.simple.SimpleAdminPlugin;
import org.camunda.bpm.admin.test.util.AbstractAdminCoreTest;
import org.camunda.bpm.cockpit.test.util.DeploymentHelper;
import org.fest.assertions.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class AdminPluginDiscoveryTest extends AbstractAdminCoreTest {

  @Deployment
  public static Archive<?> createDeployment() {
    WebArchive archive = createBaseDeployment()
          .addAsLibraries(DeploymentHelper.getAdminTestPluginJar());

    return archive;
  }

  protected static Condition<List<?>> CONTAINS_PLUGIN = new Condition<List<?>>() {

    @Override
    public boolean matches(List<?> value) {
      for (Object o: value) {
        if (o instanceof SimpleAdminPlugin) {
          return true;
        }
      }

      return false;
    }
  };

  @Before
  public void setup() {
    Admin.setAdminRuntimeDelegate(new DefaultAdminRuntimeDelegate());
  }

  @After
  public void teardown() {
    Admin.setAdminRuntimeDelegate(null);
  }

  @Test
  public void shouldDiscoverCockpitPlugin() {


    // given
    // plugin on class path

    // when
    List<AdminPlugin> plugins = Admin.getRuntimeDelegate().getAppPluginRegistry().getPlugins();

    // then
    assertThat(plugins).satisfies(CONTAINS_PLUGIN);
  }

}
