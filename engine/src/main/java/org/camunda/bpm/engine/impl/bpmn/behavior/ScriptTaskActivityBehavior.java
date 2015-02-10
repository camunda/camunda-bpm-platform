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

import javax.script.ScriptException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;


/**
 * <p>{@link ActivityBehavior} implementation of the BPMN 2.0 script task.</p>
 *
 * @author Joram Barrez
 * @author Christian Stettler
 * @author Falko Menge
 * @author Daniel Meyer
 *
 */
public class ScriptTaskActivityBehavior extends TaskActivityBehavior {

  protected ExecutableScript script;
  protected String resultVariable;

  public ScriptTaskActivityBehavior(ExecutableScript script, String resultVariable) {
    this.script = script;
    this.resultVariable = resultVariable;
  }

  public void execute(ActivityExecution execution) throws Exception {

    boolean noErrors = true;

    try {
      Object result = Context.getProcessEngineConfiguration()
        .getScriptingEnvironment()
        .execute(script, execution);
      if(result != null && resultVariable != null) {
        execution.setVariable(resultVariable, result);
      }

    } catch (ProcessEngineException e) {
      noErrors = false;
      if (e.getCause() instanceof ScriptException
          && e.getCause().getCause() instanceof BpmnError) {
        propagateBpmnError((BpmnError) e.getCause().getCause(), execution);

      } else if (e.getCause() instanceof ScriptException
          && e.getCause().getCause() instanceof ScriptException
          && e.getCause().getCause().getCause() instanceof BpmnError) {
        propagateBpmnError((BpmnError) e.getCause().getCause().getCause(), execution);


      } else {
        propagateExceptionAsError(e, execution);
      }
    }
    if (noErrors) {
      leave(execution);
    }
  }

  public ExecutableScript getScript() {
    return script;
  }

}
