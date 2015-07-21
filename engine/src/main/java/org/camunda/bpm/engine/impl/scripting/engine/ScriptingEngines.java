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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ScriptCompilationException;
import org.camunda.bpm.engine.delegate.VariableScope;

import javax.script.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * <p>Manager for JSR-223 {@link ScriptEngine} handling.</p>
 *
 * <p><strong>Resolving a script engine:</strong>
 * This class supports resolving a script engine for a given 'language name' (eg. 'groovy').
 * If the configuration option {@link #enableScriptEngineCaching} is set to true,
 * the class will attempt to cache 'cachable' script engines. We assume a {@link ScriptEngine} is
 * 'cachable' if it declares to be threadsafe (see {@link #isCachable(ScriptEngine)})</p>
 *
 * <p><strong>Custom Bindings:</strong> this class supports custom {@link Bindings}
 * implementations through the {@link #scriptBindingsFactory}. See {@link ScriptBindingsFactory}.</p>
 * </p>
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ScriptingEngines {

  private static Logger LOG = Logger.getLogger(ScriptingEngines.class.getName());

  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";
  public static final String GROOVY_SCRIPTING_LANGUAGE = "groovy";

  private final ScriptEngineManager scriptEngineManager;
  protected ScriptBindingsFactory scriptBindingsFactory;

  protected Map<String, ScriptEngine> cachedEngines = new HashMap<String, ScriptEngine>();

  protected boolean enableScriptEngineCaching = true;

  public ScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
    this(new ScriptEngineManager());
    this.scriptBindingsFactory = scriptBindingsFactory;
  }

  public ScriptingEngines(ScriptEngineManager scriptEngineManager) {
    this.scriptEngineManager = scriptEngineManager;
  }

  public void setEnableScriptEngineCaching(boolean cacheScriptEngines) {
    this.enableScriptEngineCaching = cacheScriptEngines;
  }

  public boolean isEnableScriptEngineCaching() {
    return enableScriptEngineCaching;
  }

  public ScriptEngineManager getScriptEngineManager() {
    return scriptEngineManager;
  }

  public ScriptingEngines addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
    scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
    return this;
  }

  public void setScriptEngineFactories(List<ScriptEngineFactory> scriptEngineFactories) {
    if (scriptEngineFactories != null) {
      for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
        scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
      }
    }
  }

  /**
   * <p>Used to compile a script provided as String into an engine-specific {@link CompiledScript}.</p>
   *
   * <p><strong>Note on caching of compiled scripts:</strong> only cache the returned script if
   * {@link #enableScriptEngineCaching} is set to 'true'. Depending on the implementation, the compiled
   * script will keep references to the script engine which created it.</p>
   *
   * @param language the script language in which the script is written
   * @param src a string of the source of the script
   * @return a {@link CompiledScript} or null if script engine can be found but does not support compilation.
   * @throws ProcessEngineException if no {@link ScriptEngine} can be resolved for the provided language or
   *         if the script cannot be compiled (syntax error ...).
   */
  public CompiledScript compile(String language, String src) {
    ScriptEngine scriptEngine = getScriptEngineForLanguage(language);

    if(scriptEngine instanceof Compilable && !scriptEngine.getFactory().getLanguageName().equalsIgnoreCase("ecmascript")) {
      Compilable compilingEngine = (Compilable) scriptEngine;

      try {
        CompiledScript compiledScript = compilingEngine.compile(src);

        LOG.fine("Compiled script using " + language + " script engine");

        return compiledScript;

      } catch (ScriptException e) {
        throw new ScriptCompilationException("Unable to compile script: " + e.getMessage(), e);

      }

    } else {
      // engine does not support compilation
      return null;

    }

  }

  /**
   * Loads the given script engine by language name. Will throw an exception if no script engine can be loaded for the given language name.
   *
   * @param language the name of the script language to lookup an implementation for
   * @return the script engine
   * @throws ProcessEngineException if no such engine can be found.
   */
  public ScriptEngine getScriptEngineForLanguage(String language) {

    if (language != null) {
      language = language.toLowerCase();
    }

    ScriptEngine scriptEngine = null;

    if (enableScriptEngineCaching) {
      scriptEngine = getCachedScriptEngine(language);

    } else {
      scriptEngine = scriptEngineManager.getEngineByName(language);

    }

    ensureNotNull("Can't find scripting engine for '" + language + "'", "scriptEngine", scriptEngine);

    return scriptEngine;

  }

  public Set<String> getAllSupportedLanguages() {
    Set<String> languages = new HashSet<String>();
    List<ScriptEngineFactory> engineFactories = scriptEngineManager.getEngineFactories();
    for (ScriptEngineFactory scriptEngineFactory : engineFactories) {
      languages.add(scriptEngineFactory.getLanguageName());
    }
    return languages;
  }

  /**
   * Returns a cached script engine or creates a new script engine if no such engine is currently cached.
   *
   * @param language the language (such as 'groovy' for the script engine)
   * @return the cached engine or null if no script engine can be created for the given language
   */
  protected ScriptEngine getCachedScriptEngine(String language) {

    ScriptEngine scriptEngine = cachedEngines.get(language);

    if(scriptEngine == null) {
      scriptEngine = scriptEngineManager.getEngineByName(language);

      if(scriptEngine != null) {

        if(GROOVY_SCRIPTING_LANGUAGE.equals(language)) {
          configureGroovyScriptEngine(scriptEngine);
        }

        if(isCachable(scriptEngine)) {
          cachedEngines.put(language, scriptEngine);
        }

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

  /**
   * Allows providing custom configuration for the groovy script engine.
   * @param scriptEngine the groovy script engine to configure.
   */
  protected void configureGroovyScriptEngine(ScriptEngine scriptEngine) {

    // make sure Groovy compiled scripts only hold weak references to java methods
    scriptEngine.getContext().setAttribute("#jsr223.groovy.engine.keep.globals", "weak", ScriptContext.ENGINE_SCOPE);

  }

  /** override to build a spring aware ScriptingEngines
   * @param engineBindings
   * @param scriptEngine */
  public Bindings createBindings(ScriptEngine scriptEngine, VariableScope variableScope) {
    return scriptBindingsFactory.createBindings(variableScope, scriptEngine.createBindings());
  }

  public ScriptBindingsFactory getScriptBindingsFactory() {
    return scriptBindingsFactory;
  }

  public void setScriptBindingsFactory(ScriptBindingsFactory scriptBindingsFactory) {
    this.scriptBindingsFactory = scriptBindingsFactory;
  }
}
