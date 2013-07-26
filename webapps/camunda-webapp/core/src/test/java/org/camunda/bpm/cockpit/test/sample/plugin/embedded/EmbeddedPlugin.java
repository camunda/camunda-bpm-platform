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
package org.camunda.bpm.cockpit.test.sample.plugin.embedded;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;
import org.camunda.bpm.cockpit.test.sample.plugin.embedded.resources.EmbeddedPluginRootResource;

/**
 * A plugin serving webapp embedded resources
 *
 * @author nico.rehwaldt
 */
public class EmbeddedPlugin extends AbstractCockpitPlugin {

  public static final String ID = "embedded";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getAssetDirectory() {
    return "webapp://plugin/embedded";
  }

  @Override
  public Set<Class<?>> getResourceClasses() {
    final HashSet<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(EmbeddedPluginRootResource.class);

    return classes;
  }
}
