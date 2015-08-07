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

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * A script which is provided as source code.
 *
 * @author Daniel Meyer
 *
 */
public class SourceExecutableScript extends CompiledExecutableScript {

  private static final Logger LOG = Logger.getLogger(SourceExecutableScript.class.getName());

  /** The source of the script. */
  protected String scriptSource;

  /** Flag to signal if the script should be compiled */
  protected boolean shouldBeCompiled = true;

  public SourceExecutableScript(String language, String source) {
    super(language);
    scriptSource = source;
  }

  @Override
  public Object evaluate(ScriptEngine engine, VariableScope variableScope, Bindings bindings) {
    if (shouldBeCompiled) {
      compileScript();
    }

    if (getCompiledScript() != null) {
      return super.evaluate(engine, variableScope, bindings);
    }
    else {
      return evaluateScript(engine, bindings);
    }
  }

  protected void compileScript() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (!processEngineConfiguration.isEnableScriptCompilation()) {
      // if script compilation is disabled abort
      shouldBeCompiled = false;
    } else {
      if (getCompiledScript() == null && shouldBeCompiled) {
        synchronized (this) {
          if (getCompiledScript() == null && shouldBeCompiled) {
            // try to compile script
            compiledScript = processEngineConfiguration.getScriptingEngines().compile(language, scriptSource);
            // either the script was successfully compiled or it can't be
            // compiled but we won't try it again
            shouldBeCompiled = false;
          }
        }
      }
    }

  }

  protected Object evaluateScript(ScriptEngine engine, Bindings bindings) {
    try {
      LOG.fine("Evaluating un-compiled script using " + language + " script engine ");
      return engine.eval(scriptSource, bindings);
    } catch (ScriptException e) {
      if (e.getCause() instanceof BpmnError) {
        throw (BpmnError) e.getCause();
      }
      throw new ScriptEvaluationException("Unable to evaluate script: " + e.getMessage(), e);
    }
  }

  public String getScriptSource() {
    return scriptSource;
  }

  /**
   * Sets the script source code. And invalidates any cached compilation result.
   *
   * @param scriptSource
   *          the new script source code
   */
  public void setScriptSource(String scriptSource) {
    this.compiledScript = null;
    shouldBeCompiled = true;
    this.scriptSource = scriptSource;
  }

  public boolean isShouldBeCompiled() {
    return shouldBeCompiled;
  }

}
