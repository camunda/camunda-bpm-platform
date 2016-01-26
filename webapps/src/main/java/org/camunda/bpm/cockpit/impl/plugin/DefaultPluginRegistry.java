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
package org.camunda.bpm.cockpit.impl.plugin;

import java.util.List;

import org.camunda.bpm.cockpit.plugin.PluginRegistry;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.webapp.plugin.AppPluginRegistry;

/**
 * Wrapper for backwards-compatibility to {@link PluginRegistry}.
 *
 * @author Daniel Meyer
 */
public class DefaultPluginRegistry implements PluginRegistry {

  private AppPluginRegistry<CockpitPlugin> wrappedRegistry;

  public DefaultPluginRegistry(AppPluginRegistry<CockpitPlugin> wrappedRegistry) {
    this.wrappedRegistry = wrappedRegistry;
  }

  public List<CockpitPlugin> getPlugins() {
    return wrappedRegistry.getPlugins();
  }

  public CockpitPlugin getPlugin(String id) {
    return wrappedRegistry.getPlugin(id);
  }

}
