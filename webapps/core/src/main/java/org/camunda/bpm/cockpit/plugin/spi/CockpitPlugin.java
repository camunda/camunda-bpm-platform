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
package org.camunda.bpm.cockpit.plugin.spi;

import java.util.List;

import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * The service provider interface (SPI) that must be provided by
 * a cockpit plugin.
 *
 * A cockpit plugin may provide additional MyBatis mapping files, see {@link #getMappingFiles()}.
 *
 * Plugin developers should not use this interface directly but use
 * {@link org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin} as a base class.
 *
 * @author nico.rehwaldt
 *
 * @see org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin
 */
public interface CockpitPlugin extends AppPlugin {

  /**
   * Returns a list of mapping files that define the custom queries
   * provided by this plugin.
   *
   * <p>
   *
   * The mapping files define additional MyBatis queries that can be executed by the plugin.
   *
   * <p>
   *
   * Inside the plugin the queries may be executed via the {@link org.camunda.bpm.cockpit.db.QueryService} that may be obtained through
   * {@link org.camunda.bpm.cockpit.Cockpit#getQueryService(java.lang.String) }.
   *
   * @return the list of additional mapping files
   */
  public List<String> getMappingFiles();

}
