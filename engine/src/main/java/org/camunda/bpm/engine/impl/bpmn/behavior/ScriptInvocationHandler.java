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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import javax.script.CompiledScript;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.ScriptingEngines;

/**
 * <p>Handles the script invocation in a given {@link VariableScope}. The script invocation
 * context supports pre-compilation of the script sources into a {@link CompiledScript}.</p>
 *
 * <p>This class is thread-safe and instances of this class are meant to be used for evaluating
 * the same script multiple, times in different variable scope instances.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ScriptInvocationHandler {

  /** Source code of the script to execute. */
  protected String script;

  /** The language in which the scipt is written (JavaScript, Groovy ...). */
  protected String language;

  /** The name of the variable under which the result of the script
   * should be stored in the variable scope. */
  protected String resultVariable;

  /** Holds the compiled script */
  protected CompiledScript compiledScript;

  /** Flag indicating whether script compilation is supported. Keeps us from
   * attempting to pre-compile scripts multiple times in case the script engine
   * does not support compilation. Will be populated after first invocation.
   * See {@link #preCompileScript(ScriptingEngines)} */
  protected boolean isScriptCompilationSupported = true;


  /**
   * @param script source code of the script
   * @param language the language used
   * @param resultVariable the variable name under which the result of the script invocation (if any) should be stored
   */
  public ScriptInvocationHandler(String script, String language, String resultVariable) {
    this.script = script;
    this.language = language;
    this.resultVariable = resultVariable;
  }

  /**
   * <p>Evaluates the script in the given variable scope.</p>
   *
   * @param variableScope the variable scope in which the script should be evaluate
   * @throws ProcessEngineException in case the script cannot be evaluated.
   */
  public void evaluate(VariableScope variableScope) {

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final ScriptingEngines scriptingEngines = processEngineConfiguration.getScriptingEngines();

    if(processEngineConfiguration.isEnableScriptCompilation()) {
      preCompileScript(scriptingEngines);
    }

    Object result = null;

    if(compiledScript == null) {
      result = scriptingEngines.evaluate(script, language, variableScope);

    } else {
      result = scriptingEngines.evaluate(compiledScript, language, variableScope);
    }

    if (resultVariable != null) {
      variableScope.setVariable(resultVariable, result);

    }

  }

  /**
   * Will attempt to pre-compile a script and cache it in {@link #compiledScript}. If the
   * script compilation returns 'null', {@link #isScriptCompilationSupported} will be set to
   * false and re-compilation will not be attempted.
   *
   * @param scriptingEngines
   */
  protected void preCompileScript(ScriptingEngines scriptingEngines) {
    if(compiledScript == null && isScriptCompilationSupported) {
      synchronized (this) {
        if(compiledScript == null && isScriptCompilationSupported) {
          compiledScript = scriptingEngines.compile(script, language);
          isScriptCompilationSupported = compiledScript != null;
        }
      }
    }
  }

}
