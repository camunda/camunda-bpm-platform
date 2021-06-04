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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.dmn.engine.impl.spi.el.DmnScriptEngineResolver;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

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
public class ScriptingEngines implements DmnScriptEngineResolver {

  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";
  public static final String GROOVY_SCRIPTING_LANGUAGE = "groovy";
  public static final String JAVASCRIPT_SCRIPTING_LANGUAGE = "javascript";
  public static final String ECMASCRIPT_SCRIPTING_LANGUAGE = "ecmascript";

  public static final String GRAAL_JS_SCRIPT_ENGINE_NAME = "Graal.js";
  public static final String DEFAULT_JS_SCRIPTING_LANGUAGE = GRAAL_JS_SCRIPT_ENGINE_NAME;

  protected ScriptEngineResolver scriptEngineResolver;
  protected ScriptBindingsFactory scriptBindingsFactory;

  protected boolean enableScriptEngineCaching = true;

  public ScriptingEngines(ScriptBindingsFactory scriptBindingsFactory, ScriptEngineResolver scriptEngineResolver) {
    this(scriptEngineResolver);
    this.scriptBindingsFactory = scriptBindingsFactory;
  }

  public ScriptingEngines(ScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }

  public boolean isEnableScriptEngineCaching() {
    return enableScriptEngineCaching;
  }

  public void setEnableScriptEngineCaching(boolean enableScriptEngineCaching) {
    this.enableScriptEngineCaching = enableScriptEngineCaching;
  }

  public ScriptEngineManager getScriptEngineManager() {
    return scriptEngineResolver.getScriptEngineManager();
  }

  public ScriptingEngines addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
    scriptEngineResolver.addScriptEngineFactory(scriptEngineFactory);
    return this;
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

    ProcessApplicationReference pa = Context.getCurrentProcessApplication();
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();

    ScriptEngine engine = null;
    if (config.isEnableFetchScriptEngineFromProcessApplication()) {
      if(pa != null) {
        engine = getPaScriptEngine(language, pa);
      }
    }

    if(engine == null) {
      engine = getGlobalScriptEngine(language);
    }

    return engine;
  }

  protected ScriptEngine getPaScriptEngine(String language, ProcessApplicationReference pa) {
    try {
      ProcessApplicationInterface processApplication = pa.getProcessApplication();
      ProcessApplicationInterface rawObject = processApplication.getRawObject();

      if (rawObject instanceof AbstractProcessApplication) {
        AbstractProcessApplication abstractProcessApplication = (AbstractProcessApplication) rawObject;
        return abstractProcessApplication.getScriptEngineForName(language, enableScriptEngineCaching);
      }
      return null;
    }
    catch (ProcessApplicationUnavailableException e) {
      throw new ProcessEngineException("Process Application is unavailable.", e);
    }
  }

  protected ScriptEngine getGlobalScriptEngine(String language) {

    ScriptEngine scriptEngine = scriptEngineResolver.getScriptEngine(language, enableScriptEngineCaching);

    ensureNotNull("Can't find scripting engine for '" + language + "'", "scriptEngine", scriptEngine);

    return scriptEngine;
  }

  /** override to build a spring aware ScriptingEngines
   * @param engineBindin
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

  public void setScriptEngineResolver(ScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }
}
