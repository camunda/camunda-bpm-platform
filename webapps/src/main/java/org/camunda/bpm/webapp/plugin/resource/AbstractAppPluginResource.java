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
package org.camunda.bpm.webapp.plugin.resource;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.webapp.AppRuntimeDelegate;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * A abstract plugin resource class that may be used to implement
 * REST resources which are added to the REST application of the app.
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
public abstract class AbstractAppPluginResource<T extends AppPlugin> {

  protected AppRuntimeDelegate<T> runtimeDelegate;
  protected String engineName;

  public AbstractAppPluginResource(AppRuntimeDelegate<T> runtimeDelegate, String engineName) {
    this.runtimeDelegate = runtimeDelegate;
    this.engineName = engineName;
  }

  protected ProcessEngine getProcessEngine() {
    return runtimeDelegate.getProcessEngine(engineName);
  }

}
