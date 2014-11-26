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

package org.camunda.bpm.engine.impl.task.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;

/**
 * A {@link TaskListener} which invokes a {@link ExecutableScript} when notified.
 *
 * @author Sebastian Menski
 */
public class ScriptTaskListener implements TaskListener {

  protected final ExecutableScript script;

  public ScriptTaskListener(ExecutableScript script) {
    this.script = script;
  }

	public void notify(DelegateTask delegateTask) {
    Context.getProcessEngineConfiguration()
      .getScriptingEnvironment()
      .execute(script, delegateTask);
	}

  public ExecutableScript getScript() {
    return script;
  }

}
