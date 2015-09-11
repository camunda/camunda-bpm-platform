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
package org.camunda.bpm.application.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptEngineResolver;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessApplicationScriptEnvironment {

  protected ProcessApplicationInterface processApplication;

  protected ScriptEngineResolver processApplicationScriptEngineResolver;
  protected Map<String, List<ExecutableScript>> environmentScripts = new HashMap<String, List<ExecutableScript>>();

  public ProcessApplicationScriptEnvironment(ProcessApplicationInterface processApplication) {
    this.processApplication = processApplication;
  }

  /**
   * <p>Returns an instance of {@link ScriptEngine} for the given <code>scriptEngineName</code>.</p>
   *
   * <p>Iff the given parameter <code>cache</code> is set <code>true</code>,
   * then the instance {@link ScriptEngine} will be cached.</p>
   *
   * @param scriptEngineName the name of the {@link ScriptEngine} to return
   * @param cache a boolean value which indicates whether the {@link ScriptEngine} should
   *              be cached or not.
   *
   * @return a {@link ScriptEngine}
   */
  public ScriptEngine getScriptEngineForName(String scriptEngineName, boolean cache) {
    if(processApplicationScriptEngineResolver == null) {
      synchronized (this) {
        if(processApplicationScriptEngineResolver == null) {
          processApplicationScriptEngineResolver = new ScriptEngineResolver(new ScriptEngineManager(getProcessApplicationClassloader()));
        }
      }
    }
    return processApplicationScriptEngineResolver.getScriptEngine(scriptEngineName, cache);
  }

  /**
   * Returns a map of cached environment scripts per script language.
   */
  public Map<String, List<ExecutableScript>> getEnvironmentScripts() {
    return environmentScripts;
  }

  protected ClassLoader getProcessApplicationClassloader() {
    return processApplication.getProcessApplicationClassloader();
  }

}
