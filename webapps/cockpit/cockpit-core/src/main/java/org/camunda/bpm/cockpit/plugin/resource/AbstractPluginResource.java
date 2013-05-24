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
package org.camunda.bpm.cockpit.plugin.resource;

import org.camunda.bpm.cockpit.service.AbstractEngineAware;

/**
 * A abstract plugin resource class that may be used to implement
 * resources in cockpit.
 * 
 * @author nico.rehwaldt
 */
public class AbstractPluginResource extends AbstractEngineAware {

  public AbstractPluginResource(String engineName) {
    super(engineName);
  }
}
