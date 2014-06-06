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
package org.camunda.bpm.cockpit.service;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * Base class for engine aware service and resource
 * implementations.
 *
 * <p>
 *
 * Subclasses must implement a constructor that accepts the name of
 * the engine for which cockpit services should be provided.
 *
 * @author nico.rehwaldt
 */
@Deprecated
public class AbstractEngineAware {

  private final String engineName;

  /**
   * Creates a engine aware instance for the given engine
   *
   * @param engineName
   */
  public AbstractEngineAware(String engineName) {
    this.engineName = engineName;
  }

  /**
   * Return a {@link CommandExecutor} for the current
   * engine to execute plugin commands.
   *
   * @return
   */
  protected CommandExecutor getCommandExecutor() {
    return Cockpit.getCommandExecutor(engineName);
  }

  /**
   * Return a {@link QueryService} for the current
   * engine to execute queries against the engine datbase.
   *
   * @return
   */
  protected QueryService getQueryService() {
    return Cockpit.getQueryService(engineName);
  }

  /**
   * Return a {@link ProcessEngine} for the current
   * engine name to execute queries against the engine.
   *
   * @return the process engine
   */
  protected ProcessEngine getProcessEngine() {
    return Cockpit.getProcessEngine(engineName);
  }
}
