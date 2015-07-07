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
package org.camunda.bpm.engine.impl.scripting.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.dmn.scriptengine.DmnScriptEngine;

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
  protected Map<String, List<ExecutableScript>> env = new HashMap<String, List<ExecutableScript>>();

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

    final String scriptLanguage = script.getLanguage();

    // first, evaluate the env scripts (if any)
    List<ExecutableScript> envScripts = getEnvScripts(scriptLanguage);
    for (ExecutableScript envScript : envScripts) {
      envScript.execute(scriptEngine, scope, bindings);
    }

    // set the script engine manager for the dmn script engine
    if (scriptEngine instanceof DmnScriptEngine) {
      // TODO: If we also want to set the decision id in the script context we should probably use an own script context per invocation
      scriptEngine.getContext().setAttribute(DmnScriptEngine.SCRIPT_ENGINE_MANAGER_ATTRIBUTE, scriptingEngines.getScriptEngineManager(), ScriptContext.ENGINE_SCOPE);
    }

    // next evaluate the actual script
    return script.execute(scriptEngine, scope, bindings);
  }

  /**
   * Returns the env scripts for the given language. Performs lazy initialization of the env scripts.
   *
   * @param scriptLanguage the language
   * @return a list of executable environment scripts. Never null.
   */
  protected List<ExecutableScript> getEnvScripts(String scriptLanguage) {
    List<ExecutableScript> envScripts = env.get(scriptLanguage);
    if(envScripts == null) {
      synchronized (this) {
        envScripts = env.get(scriptLanguage);
        if(envScripts == null) {
          envScripts = initEnvForLanguage(scriptLanguage);
          env.put(scriptLanguage, envScripts);
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

    List<ExecutableScript> scripts = new ArrayList<ExecutableScript>();
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
