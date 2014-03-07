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
package org.camunda.bpm.webapp.plugin;

import java.util.List;

import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * The holder of registered {@link AppPlugin AppPlugins}.
 *
 * @author nico.rehwaldt
 */
public interface AppPluginRegistry<T extends AppPlugin> {

  /**
   * Returns all registered plugins
   *
   * @return
   */
  public List<T> getPlugins();

  /**
   * Returns the registered plugin with the given name or
   * <code>null</code> if the plugin does not exist.
   *
   * @param id
   * @return
   */
  public T getPlugin(String id);
}
