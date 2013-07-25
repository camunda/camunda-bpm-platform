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
package org.camunda.bpm.cockpit;

import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>Provides access to the camunda cockpit core services.</p>
 *
 * @author roman.smirnov
 * @author nico.rehwaldt
 */
public class Cockpit {

  /**
   * The {@link CockpitRuntimeDelegate} is an delegate that will be
   * initialized by bootstrapping camunda cockpit with an specific
   * instance
   */
  protected static CockpitRuntimeDelegate COCKPIT_RUNTIME_DELEGATE;

  /**
   * Returns a configured {@link QueryService} to execute custom
   * statements to the corresponding process engine
   *
   * @param processEngineName
   *
   * @return a {@link QueryService}
   */
  public static QueryService getQueryService(String processEngineName) {
    return getRuntimeDelegate().getQueryService(processEngineName);
  }

  /**
   * Returns a configured {@link CommandExecutor} to execute
   * commands to the corresponding process engine
   *
   * @param processEngineName
   *
   * @return a {@link CommandExecutor}
   */
  public static CommandExecutor getCommandExecutor(String processEngineName) {
    return getRuntimeDelegate().getCommandExecutor(processEngineName);
  }

  public static ProcessEngine getProcessEngine(String processEngineName) {
    return getRuntimeDelegate().getProcessEngine(processEngineName);
  }

  /**
   * Returns an instance of {@link CockpitRuntimeDelegate}
   *
   * @return
   */
  public static CockpitRuntimeDelegate getRuntimeDelegate() {
    return COCKPIT_RUNTIME_DELEGATE;
  }

  /**
   * A setter to set the {@link CockpitRuntimeDelegate}.
   * @param cockpitRuntimeDelegate
   */
  public static void setCockpitRuntimeDelegate(CockpitRuntimeDelegate cockpitRuntimeDelegate) {
    COCKPIT_RUNTIME_DELEGATE = cockpitRuntimeDelegate;
  }

}
