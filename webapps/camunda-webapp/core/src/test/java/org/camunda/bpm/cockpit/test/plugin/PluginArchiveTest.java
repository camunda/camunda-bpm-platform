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

import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.test.sample.plugin.simple.SimpleCockpitPlugin;
import org.camunda.bpm.cockpit.test.util.DeploymentHelper;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class PluginArchiveTest {

  @Test
  public void shouldContainCorrectResources() {

    JavaArchive pluginArchive = DeploymentHelper.getCockpitTestPluginAsFiles();

    String pluginPkg = SimpleCockpitPlugin.class.getPackage().getName().replaceAll("\\.", "/");

    String contents = pluginArchive.toString(true);

    assertThat(contents)
        .contains("/META-INF/services/" + CockpitPlugin.class.getName())
        .contains("/" + pluginPkg + "/" + SimpleCockpitPlugin.class.getSimpleName() + ".class")
        .contains("/plugin-webapp/simple/test.txt")
        .contains("/" + pluginPkg + "/queries/simple.xml");
  }
}
