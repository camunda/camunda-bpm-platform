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
package org.camunda.bpm.webapp;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.webapp.plugin.AppPluginRegistry;
import org.camunda.bpm.webapp.plugin.resource.PluginResourceOverride;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * The list of services provided by a camunda webapplication, providing
 * a plugin registry and access to the process engine.
 *
 * @author Daniel Meyer
 *
 */
public interface AppRuntimeDelegate<T extends AppPlugin> {

  /**
   * Returns a {@link ProcessEngine} to the assigned
   * <code>processEngineName</code>
   *
   * @param processEngineName
   * @return a {@link ProcessEngine}
   */
  public ProcessEngine getProcessEngine(String processEngineName);

  /**
   * Returns the list of {@link ProcessEngine} names available to the runtime
   * @return
   */
  public Set<String> getProcessEngineNames();

  /**
   * Returns the default {@link ProcessEngine} provided by the
   * @return
   */
  public ProcessEngine getDefaultProcessEngine();

  /**
   * A registry that provides access to the plugins registered
   * in the application.
   *
   * @return
   */
  public AppPluginRegistry<T> getAppPluginRegistry();

  /**
   * A list of resource overrides.
   * @return the list of registered resource overrides
   */
  public List<PluginResourceOverride> getResourceOverrides();

}
