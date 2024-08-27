/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

public class DefaultScriptEngineResolver implements ScriptEngineResolver {

  protected final ScriptEngineManager scriptEngineManager;

  protected Map<String, ScriptEngine> cachedEngines = new HashMap<>();

  public DefaultScriptEngineResolver(ScriptEngineManager scriptEngineManager) {
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

      if (scriptEngine == null) {
        scriptEngine = getScriptEngine(language);

        if (scriptEngine != null && isCachable(scriptEngine)) {
          cachedEngines.put(language, scriptEngine);
        }
      }

    } else {
      scriptEngine = getScriptEngine(language);
    }

    return scriptEngine;
  }

  protected ScriptEngine getScriptEngine(String language) {
    ScriptEngine scriptEngine = null;
    if (ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE.equalsIgnoreCase(language) ||
        ScriptingEngines.ECMASCRIPT_SCRIPTING_LANGUAGE.equalsIgnoreCase(language)) {
      scriptEngine = getJavaScriptScriptEngine(language);
    } else {
      scriptEngine = scriptEngineManager.getEngineByName(language);
    }

    if (scriptEngine != null) {
      configureScriptEngines(language, scriptEngine);
    }
    return scriptEngine;
  }

  protected ScriptEngine getJavaScriptScriptEngine(String language) {
    ScriptEngine scriptEngine = null;
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    if (config != null && config.getScriptEngineNameJavaScript() != null) {
      scriptEngine = scriptEngineManager.getEngineByName(config.getScriptEngineNameJavaScript());
    } else {
      scriptEngine = scriptEngineManager.getEngineByName(ScriptingEngines.DEFAULT_JS_SCRIPTING_LANGUAGE);
      if (scriptEngine == null) {
        // default engine is not available, try to fetch any existing JS script engine
        scriptEngine = scriptEngineManager.getEngineByName(language);
      }
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

  protected void configureScriptEngines(String language, ScriptEngine scriptEngine) {
    if (ScriptingEngines.GROOVY_SCRIPTING_LANGUAGE.equals(language)) {
      configureGroovyScriptEngine(scriptEngine);
    }

    if (ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME.equals(scriptEngine.getFactory().getEngineName())) {
      configureGraalJsScriptEngine(scriptEngine);
    }
  }

  /**
   * Allows providing custom configuration for the groovy script engine.
   * @param scriptEngine the groovy script engine to configure.
   */
  protected void configureGroovyScriptEngine(ScriptEngine scriptEngine) {
    // make sure Groovy compiled scripts only hold weak references to java methods
    scriptEngine.getContext().setAttribute("#jsr223.groovy.engine.keep.globals", "weak", ScriptContext.ENGINE_SCOPE);
  }

  /**
   * Allows providing custom configuration for the Graal JS script engine.
   * @param scriptEngine the Graal JS script engine to configure.
   */
  protected void configureGraalJsScriptEngine(ScriptEngine scriptEngine) {
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    if (config != null) {
      if (config.isConfigureScriptEngineHostAccess()) {
        // make sure Graal JS can provide access to the host and can lookup classes
        scriptEngine.getContext().setAttribute("polyglot.js.allowHostAccess", true, ScriptContext.ENGINE_SCOPE);
        scriptEngine.getContext().setAttribute("polyglot.js.allowHostClassLookup", true, ScriptContext.ENGINE_SCOPE);
      }
      if (config.isEnableScriptEngineLoadExternalResources()) {
        // make sure Graal JS can load external scripts
        scriptEngine.getContext().setAttribute("polyglot.js.allowIO", true, ScriptContext.ENGINE_SCOPE);
      }
      if (config.isEnableScriptEngineNashornCompatibility()) {
        // enable Nashorn compatibility mode
        scriptEngine.getContext().setAttribute("polyglot.js.nashorn-compat", true, ScriptContext.ENGINE_SCOPE);
      }
    }
  }

}
