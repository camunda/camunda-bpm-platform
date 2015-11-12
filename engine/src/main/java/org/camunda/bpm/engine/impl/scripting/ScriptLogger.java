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
package org.camunda.bpm.engine.impl.scripting;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Daniel Meyer
 *
 */
public class ScriptLogger extends ProcessEngineLogger {

  public void debugEvaluatingCompiledScript(String language) {
    logDebug(
        "001", "Evaluating compiled script {} in language", language);
  }

  public void debugCompiledScriptUsing(String language) {
    logDebug(
        "002", "Compiled script using {} script language", language);
  }

  public void debugEvaluatingNonCompiledScript(String scriptSource) {
    logDebug(
        "001", "Evaluating non-compiled script {}", scriptSource);
  }

}
