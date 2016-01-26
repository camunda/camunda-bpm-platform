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
package org.camunda.bpm.webapp.plugin.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.camunda.bpm.webapp.plugin.AppPluginRegistry;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * Default implementation of {@link AppPluginRegistry} that loads Plugins
 * via the {@link ServiceLoader} facilities provided by the Java SE platform.
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
public class DefaultAppPluginRegistry<T extends AppPlugin> implements AppPluginRegistry<T> {

  /** the interface type of plugins managed by this registry */
  protected final Class<T> pluginType;

  protected Map<String, T> pluginsMap;

  public DefaultAppPluginRegistry(Class<T> pluginType) {
    this.pluginType = pluginType;
  }

  protected void loadPlugins() {

    ServiceLoader<T> loader = ServiceLoader.load(pluginType);

    Iterator<T> iterator = loader.iterator();

    Map<String, T> map = new HashMap<String, T>();

    while (iterator.hasNext()) {
      T plugin = iterator.next();
      map.put(plugin.getId(), plugin);
    }

    this.pluginsMap = map;
  }

  @Override
  public List<T> getPlugins() {
    if (pluginsMap == null) {
      loadPlugins();
    }

    return new ArrayList<T>(pluginsMap.values());
  }

  @Override
  public T getPlugin(String id) {

    if (pluginsMap == null) {
      loadPlugins();
    }

    return pluginsMap.get(id);
  }
}
