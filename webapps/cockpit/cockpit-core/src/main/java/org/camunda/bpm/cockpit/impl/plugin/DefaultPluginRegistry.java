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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.camunda.bpm.cockpit.plugin.PluginRegistry;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;

/**
 * Default implementation of {@link PluginRegistry} that loads plugins
 * via the {@link ServiceLoader<CockpitPlugin>}.
 * 
 * @author nico.rehwaldt
 */
public class DefaultPluginRegistry implements PluginRegistry {

  private Map<String, CockpitPlugin> pluginsMap;

  protected void loadPlugins() {

    ServiceLoader<CockpitPlugin> loader = ServiceLoader.load(CockpitPlugin.class);

    Iterator<CockpitPlugin> iterator = loader.iterator();

    Map<String, CockpitPlugin> map = new HashMap<String, CockpitPlugin>();

    while (iterator.hasNext()) {
      CockpitPlugin plugin = iterator.next();
      map.put(plugin.getId(), plugin);
    }

    this.pluginsMap = map;
  }

  @Override
  public List<CockpitPlugin> getPlugins() {
    if (pluginsMap == null) {
      loadPlugins();
    }

    return new ArrayList<CockpitPlugin>(pluginsMap.values());
  }

  @Override
  public CockpitPlugin getPlugin(String id) {

    if (pluginsMap == null) {
      loadPlugins();
    }

    return pluginsMap.get(id);
  }
}
