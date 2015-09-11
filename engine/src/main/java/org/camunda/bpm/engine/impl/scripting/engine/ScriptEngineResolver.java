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
package org.camunda.bpm.engine.impl.scripting.engine;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

/**
 * @author Thorben Lindhauer
 *
 */
public class ScriptEngineResolver {

  protected final ScriptEngineManager scriptEngineManager;

  protected Map<String, ScriptEngine> cachedEngines = new HashMap<String, ScriptEngine>();

  public ScriptEngineResolver(ScriptEngineManager scriptEngineManager) {
    this.scriptEngineManager = scriptEngineManager;
  }

  public void addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
    scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
  }

  public ScriptEngineManager getScriptEngineManager() {
    return scriptEngineManager;
  }


  /**
   * Returns a cached script engine or creates a new script engine if no such engine is currently cached.
   *
   * @param language the language (such as 'groovy' for the script engine)
   * @return the cached engine or null if no script engine can be created for the given language
   */
  public ScriptEngine getScriptEngine(String language, boolean resolveFromCache) {

    ScriptEngine scriptEngine = null;

    if (resolveFromCache) {
      scriptEngine = cachedEngines.get(language);

      if(scriptEngine == null) {
        scriptEngine = scriptEngineManager.getEngineByName(language);

        if(scriptEngine != null) {

          if(ScriptingEngines.GROOVY_SCRIPTING_LANGUAGE.equals(language)) {
            configureGroovyScriptEngine(scriptEngine);
          }

          if(isCachable(scriptEngine)) {
            cachedEngines.put(language, scriptEngine);
          }

        }

      }

    } else {
      scriptEngine = scriptEngineManager.getEngineByName(language);
    }

    return scriptEngine;
  }

  /**
   * Allows checking whether the script engine can be cached.
   *
   * @param scriptEngine the script engine to check.
   * @return true if the script engine may be cached.
   */
  protected boolean isCachable(ScriptEngine scriptEngine) {
    // Check if script-engine supports multithreading. If true it can be cached.
    Object threadingParameter = scriptEngine.getFactory().getParameter("THREADING");
    return threadingParameter != null;
  }

  /**
   * Allows providing custom configuration for the groovy script engine.
   * @param scriptEngine the groovy script engine to configure.
   */
  protected void configureGroovyScriptEngine(ScriptEngine scriptEngine) {

    // make sure Groovy compiled scripts only hold weak references to java methods
    scriptEngine.getContext().setAttribute("#jsr223.groovy.engine.keep.globals", "weak", ScriptContext.ENGINE_SCOPE);
  }


}
