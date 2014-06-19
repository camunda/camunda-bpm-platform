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
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * A pre-compiled script.
 *
 * @author Daniel Meyer
 *
 */
public class CompiledExecutableScript extends ExecutableScript {

  /**
   * the pre-compiled script.
   */
  protected CompiledScript compiledScript;

  /**
   * @param language the script language
   * @param compiledScript the compiled script
   */
  protected CompiledExecutableScript(String language, CompiledScript compiledScript) {
    super(language);
    this.compiledScript = compiledScript;
  }

  public Object execute(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    try {
      return compiledScript.eval(bindings);
    } catch (ScriptException e) {
      throw new ProcessEngineException("Could not evaluate script: "+e.getMessage(), e);
    }
  }

}
