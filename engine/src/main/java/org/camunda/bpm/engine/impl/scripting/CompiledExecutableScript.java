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

import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.VariableScope;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.logging.Logger;

public class CompiledExecutableScript extends ExecutableScript {

  private static final Logger LOG = Logger.getLogger(CompiledExecutableScript.class.getName());

  protected CompiledScript compiledScript;

  protected CompiledExecutableScript(String language) {
    this(language, null);
  }

  protected CompiledExecutableScript(String language, CompiledScript compiledScript) {
    super(language);
    this.compiledScript = compiledScript;
  }

  public CompiledScript getCompiledScript() {
    return compiledScript;
  }

  public void setCompiledScript(CompiledScript compiledScript) {
    this.compiledScript = compiledScript;
  }

  public Object evaluate(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    try {
      LOG.fine("Evaluating compiled script using " + language + " script engine ");
      return getCompiledScript().eval(bindings);
    } catch (ScriptException e) {
      if (e.getCause() instanceof BpmnError) {
        throw (BpmnError) e.getCause();
      }
      throw new ScriptEvaluationException("Unable to evaluate script: " + e.getMessage(), e);
    }
  }

}
