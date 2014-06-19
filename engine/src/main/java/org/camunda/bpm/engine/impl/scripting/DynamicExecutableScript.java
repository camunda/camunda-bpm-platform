/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.scripting;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * A script which is dynamically determined during the execution.
 * Therefore it has to be executed in the context of an atomic operation.
 *
 * @author Sebastian Menski
 */
public abstract class DynamicExecutableScript extends ExecutableScript {

  protected final Expression scriptExpression;

  protected DynamicExecutableScript(Expression scriptExpression, String language) {
    super(language);
    this.scriptExpression = scriptExpression;
  }

  public Object execute(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    ExecutableScript script = getScript(variableScope);
    return script.execute(scriptEngine, variableScope, bindings);
  }

  public abstract ExecutableScript getScript(VariableScope variableScope);

  protected ExecutableScript compileScript(String scriptSource) {
    return Context.getProcessEngineConfiguration()
      .getScriptFactory()
      .createScript(scriptSource, language);
  }

}
