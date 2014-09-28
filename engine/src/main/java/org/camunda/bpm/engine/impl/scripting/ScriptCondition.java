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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureInstanceOf;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * A {@link Condition} which invokes a {@link ExecutableScript} when evaluated.
 *
 * @author Sebastian Menski
 */
public class ScriptCondition implements Condition {

  protected final ExecutableScript script;

  public ScriptCondition(ExecutableScript script) {
    this.script = script;
  }

  public boolean evaluate(DelegateExecution execution) {
    Object result = Context.getProcessEngineConfiguration()
      .getScriptingEnvironment()
      .execute(script, execution);

    ensureNotNull("condition script returns null", "result", result);
    ensureInstanceOf("condition script returns non-Boolean", "result", result, Boolean.class);

    return (Boolean) result;
  }

  public ExecutableScript getScript() {
    return script;
  }

}
