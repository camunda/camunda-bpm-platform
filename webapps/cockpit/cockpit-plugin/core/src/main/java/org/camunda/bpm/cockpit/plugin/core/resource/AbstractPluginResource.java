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
package org.camunda.bpm.cockpit.plugin.core.resource;


import org.camunda.bpm.cockpit.plugin.core.Cockpit;
import org.camunda.bpm.cockpit.plugin.core.db.CommandExecutor;
import org.camunda.bpm.cockpit.plugin.core.db.QueryService;

/**
 *
 * @author nico.rehwaldt
 */
public class AbstractPluginResource {

  private final String engineName;

  public AbstractPluginResource(String engineName) {
    this.engineName = engineName;
  }

  /**
   * Return a {@link CommandExecutor} for the current
   * engine to execute plugin commands.
   *
   * @return
   */
  public CommandExecutor getCommandExecutor() {
    return Cockpit.getCommandExecutor(engineName);
  }

  /**
   * Return a {@link QueryService} for the current
   * engine to execute queries against the engine datbase.
   *
   * @return
   */
  public QueryService getQueryService() {
    return Cockpit.getQueryService(engineName);
  }
}
