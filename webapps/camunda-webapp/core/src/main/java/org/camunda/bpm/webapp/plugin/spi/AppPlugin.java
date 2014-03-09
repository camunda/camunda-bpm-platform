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
package org.camunda.bpm.webapp.plugin.spi;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.webapp.plugin.resource.PluginResourceOverride;
import org.camunda.bpm.webapp.plugin.spi.impl.AbstractAppPlugin;

/**
 * The service provider interface (SPI) that must be provided by a webapplication plugin.
 *
 * <p>
 *
 * A implementation of this SPI publishes
 *
 * <ul>
 *   <li>a unique ID</li>
 *   <li>a directory that contains the plugins client-side assets (HTML + JavaScript files)</li>
 *   <li>a number of resource classes that extend the restful API</li>
 * </ul>
 *
 * <p>
 *
 * Plugin developers should not use this interface directly but use {@link AbstractAppPlugin} as a base class.
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 *
 */
public interface AppPlugin {

  /**
   * Returns the unique id of this plugin.
   *
   * @return
   */
  public String getId();

  /**
   * Returns a set of JAX-RS resource classes that extend the rest API.
   *
   * <p>
   *
   * Typically, a plugin publishes its API via a subclass of {@link org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource}.
   *
   * @return the set of resource classes provided by this plugin
   */
  public Set<Class<?>> getResourceClasses();

  /**
   * Returns a uri to a plugin resources directory.
   * The directory must be unique across all plugins.
   *
   * @return the directory providing the plugins client side resources
   */
  public String getAssetDirectory();

  /**
   * Allows providing a list of {@link PluginResourceOverride resource overrides}. Resource overrides allow
   * to conditionally override the static resources provided by other plugins.
   *
   * @return a list of {@link PluginResourceOverride} implementations.
   */
  public List<PluginResourceOverride> getResourceOverrides();


}
