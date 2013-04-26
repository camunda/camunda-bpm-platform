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
package org.camunda.bpm.cockpit.plugin.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.camunda.bpm.cockpit.plugin.core.spi.CockpitPlugin;

/**
 *
 * @author nico.rehwaldt
 */
public class Registry {

  protected static Map<String, CockpitPlugin> PLUGINS;

  public static List<CockpitPlugin> getCockpitPlugins() {

    if (PLUGINS == null) {
      loadPlugins();
    }

    return new ArrayList<CockpitPlugin>(PLUGINS.values());
  }

  public static CockpitPlugin getCockpitPlugin(String id) {
    if (PLUGINS == null) {
      loadPlugins();
    }

    return PLUGINS.get(id);
  }

  static void loadPlugins() {

    ServiceLoader<CockpitPlugin> loader = ServiceLoader.load(CockpitPlugin.class);

    Iterator<CockpitPlugin> iterator = loader.iterator();

    Map<String, CockpitPlugin> plugins = new HashMap<String, CockpitPlugin>();

    while (iterator.hasNext()) {
      CockpitPlugin plugin = iterator.next();
      plugins.put(plugin.getId(), plugin);
    }

    PLUGINS = plugins;
  }
}
