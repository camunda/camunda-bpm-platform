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
package org.camunda.spin.plugin.impl;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.commons.logging.BaseLogger;

/**
 * @author Thorben Lindhauer
 *
 */
public class SpinPluginLogger extends BaseLogger {

  public static final String PROJECT_CODE = "SPIN-PLUGIN";

  public static final SpinPluginLogger LOGGER = BaseLogger.createLogger(SpinPluginLogger.class, PROJECT_CODE, "org.camunda.spin.plugin", "01");

  public void logNoDataFormatsInitiailized(String dataFormatDescription, String reason) {
    logInfo(
        "001", "Cannot initialize %s: %s", dataFormatDescription, reason);
  }

  public ProcessEngineException fallbackSerializerCannotDeserializeObjects() {
    return new ProcessEngineException(exceptionMessage(
        "002", "Fallback serializer cannot handle deserialized objects"));
  }
}
