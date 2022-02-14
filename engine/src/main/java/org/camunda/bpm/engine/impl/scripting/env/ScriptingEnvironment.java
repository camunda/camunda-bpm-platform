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
package org.camunda.bpm.engine.impl.scripting.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;

/**
 * <p>The scripting environment contains scripts that provide an environment to
 * a user provided script. The environment may contain additional libraries
 * or imports.</p>
 *
 * <p>The environment performs lazy initialization of scripts: the first time a script of a given
 * script language is executed, the environment will use the {@link ScriptEnvResolver ScriptEnvResolvers}
 * for resolving the environment scripts for that language. The scripts (if any) are then pre-compiled
 * and cached for reuse.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ScriptingEnvironment {

  /** the cached environment scripts per script language */
  protected Map<String, List<ExecutableScript>> env = new HashMap<>();

  /** the resolvers */
  protected List<ScriptEnvResolver> envResolvers;

  /** the script factory used for compiling env scripts */
  protected ScriptFactory scriptFactory;

  /** the scripting engines */
  protected ScriptingEngines scriptingEngines;

  public ScriptingEnvironment(ScriptFactory scriptFactory, List<ScriptEnvResolver> scriptEnvResolvers, ScriptingEngines scriptingEngines) {
    this.scriptFactory = scriptFactory;
    this.envResolvers = scriptEnvResolvers;
    this.scriptingEngines = scriptingEngines;
  }

  /**
   * execute a given script in the environment
   *
   * @param script the {@link ExecutableScript} to execute
   * @param scope the scope in which to execute the script
   * @return the result of the script evaluation
   */
  public Object execute(ExecutableScript script, VariableScope scope) {

    // get script engine
    ScriptEngine scriptEngine = scriptingEngines.getScriptEngineForLanguage(script.getLanguage());

    // create bindings
    Bindings bindings = scriptingEngines.createBindings(scriptEngine, scope);

    return execute(script, scope, bindings, scriptEngine);
  }

  public Object execute(ExecutableScript script, VariableScope scope, Bindings bindings, ScriptEngine scriptEngine) {

    // first, evaluate the env scripts (if any)
    List<ExecutableScript> envScripts = getEnvScripts(script, scriptEngine);
    for (ExecutableScript envScript : envScripts) {
      envScript.execute(scriptEngine, scope, bindings);
    }

    // next evaluate the actual script
    return script.execute(scriptEngine, scope, bindings);
  }

  protected Map<String, List<ExecutableScript>> getEnv(String language) {
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    ProcessApplicationReference processApplication = Context.getCurrentProcessApplication();

    Map<String, List<ExecutableScript>> result = null;
    if (config.isEnableFetchScriptEngineFromProcessApplication()) {
      if(processApplication != null) {
        result = getPaEnvScripts(processApplication);
      }
    }

    return result != null ? result : env;
  }

  protected Map<String, List<ExecutableScript>> getPaEnvScripts(ProcessApplicationReference pa) {
    try {
      ProcessApplicationInterface processApplication = pa.getProcessApplication();
      ProcessApplicationInterface rawObject = processApplication.getRawObject();

      if (rawObject instanceof AbstractProcessApplication) {
        AbstractProcessApplication abstractProcessApplication = (AbstractProcessApplication) rawObject;
        return abstractProcessApplication.getEnvironmentScripts();
      }
      return null;
    }
    catch (ProcessApplicationUnavailableException e) {
      throw new ProcessEngineException("Process Application is unavailable.", e);
    }
  }

  protected List<ExecutableScript> getEnvScripts(ExecutableScript script, ScriptEngine scriptEngine) {
    List<ExecutableScript> envScripts = getEnvScripts(script.getLanguage().toLowerCase());
    if (envScripts.isEmpty()) {
      envScripts = getEnvScripts(scriptEngine.getFactory().getLanguageName().toLowerCase());
    }
    return envScripts;
  }

  /**
   * Returns the env scripts for the given language. Performs lazy initialization of the env scripts.
   *
   * @param scriptLanguage the language
   * @return a list of executable environment scripts. Never null.
   */
  protected List<ExecutableScript> getEnvScripts(String scriptLanguage) {
    Map<String, List<ExecutableScript>> environment = getEnv(scriptLanguage);
    List<ExecutableScript> envScripts = environment.get(scriptLanguage);
    if(envScripts == null) {
      synchronized (this) {
        envScripts = environment.get(scriptLanguage);
        if(envScripts == null) {
          envScripts = initEnvForLanguage(scriptLanguage);
          environment.put(scriptLanguage, envScripts);
        }
      }
    }
    return envScripts;
  }

  /**
   * Initializes the env scripts for a given language.
   *
   * @param language the language
   * @return the list of env scripts. Never null.
   */
  protected List<ExecutableScript> initEnvForLanguage(String language) {

    List<ExecutableScript> scripts = new ArrayList<>();
    for (ScriptEnvResolver resolver : envResolvers) {
      String[] resolvedScripts = resolver.resolve(language);
      if(resolvedScripts != null) {
        for (String resolvedScript : resolvedScripts) {
          scripts.add(scriptFactory.createScriptFromSource(language, resolvedScript));
        }
      }
    }

    return scripts;
  }

}
