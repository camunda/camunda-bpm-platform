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

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * <p>Represents an executable script.</p>
 *
 *
 * @author Daniel Meyer
 *
 */
public abstract class ExecutableScript {

  /** The language of the script. Used to resolve the
   * {@link ScriptEngine}. */
  protected final String language;

  protected ExecutableScript(String language) {
    this.language = language;
  }

  /**
   * The language in which the script is written.
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * <p>Evaluates the script using the provided engine and bindings</p>
   *
   * @param scriptEngine the script engine to use for evaluating the script.
   * @param variableScope the variable scope of the execution
   * @param bindings the bindings to use for evaluating the script.
   * @throws ProcessEngineException in case the script cannot be evaluated.
   * @return the result of the script evaluation
   */
  public Object execute(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    return evaluate(scriptEngine, variableScope, bindings);
  }

  protected abstract Object evaluate(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings);

}
