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
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;

/**
 *
 * @author nico.rehwaldt
 */
public interface CockpitPlugin {

  /**
   * Returns a set of resource classes
   *
   * @return
   */
  public Set<Class<?>> getResourceClasses();

  /**
   * Returns a list of mapping files for custom queries
   * provided by this plugin
   *
   * @return
   */
  public List<String> getMappingFiles();

  /**
   * Returns a uri to a plugins asset directory.
   * The directory must be unique across all plugins.
   *
   * <p>
   *
   * Typically <code>org.camunda.bpm.cockpit.plugin.{name}.assets</code>.
   *
   * <p>
   *
   * In the default implementation (provided by {@link AbstractCockpitPlugin} the
   * <code>assets</code> directory is relative the the {@link CockpitPlugin}
   * implementation class).
   *
   * @return the directory to search assets under
   */
  public String getAssetDirectory();

  /**
   * Returns the id of this plugin
   *
   * @return
   */
  public String getId();
}
