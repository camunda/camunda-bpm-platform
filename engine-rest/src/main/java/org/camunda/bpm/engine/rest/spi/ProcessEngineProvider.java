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
package org.camunda.bpm.engine.rest.spi;

import org.camunda.bpm.engine.ProcessEngine;

import java.util.Set;

/**
 * A simple provider SPI used to locate a process engine object.
 *
 * @author Daniel Meyer
 *
 */
public interface ProcessEngineProvider {

  /**
   * Provides the default engine. Has to return null if no default engine exists.
   */
  ProcessEngine getDefaultProcessEngine();

  /**
   * Provides the engine with the given name. Has to return null if no such engine exists.
   */
  ProcessEngine getProcessEngine(String name);

  /**
   * Returns the name of all known process engines. Returns an empty set if no engines are accessible.
   */
  Set<String> getProcessEngineNames();

}
