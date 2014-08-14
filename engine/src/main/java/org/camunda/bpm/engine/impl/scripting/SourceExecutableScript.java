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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.logging.Logger;

/**
 * A script which is provided as source code. This is used if the corresponding
 * script engine does not support compilation.
 *
 * @author Daniel Meyer
 *
 */
public class SourceExecutableScript extends ExecutableScript {

  private static final Logger LOG = Logger.getLogger(SourceExecutableScript.class.getName());

  /** The source of the script. */
  protected String scriptSrc;

  public SourceExecutableScript(String language, String src) {
    super(language);
    scriptSrc = src;
  }

  public Object execute(ScriptEngine engine, VariableScope variableScope, Bindings bindings) {
    try {
      LOG.fine("Evaluating un-compiled script using " + language + " script engine ");
      return engine.eval(scriptSrc, bindings);

    } catch (ScriptException e) {
      throw new ProcessEngineException("problem evaluating script: " + e.getMessage(), e);
    }
  }

  public String getScriptSrc() {
    return scriptSrc;
  }

  public void setScriptSrc(String scriptSrc) {
    this.scriptSrc = scriptSrc;
  }
}
