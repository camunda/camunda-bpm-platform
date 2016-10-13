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

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.ScriptInvocation;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;

/**
 * <p>
 * {@link ActivityBehavior} implementation of the BPMN 2.0 script task.
 * </p>
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

  @Override
  public void performExecution(final ActivityExecution execution) throws Exception {
    executeWithErrorPropagation(execution, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        ScriptInvocation invocation = new ScriptInvocation(script, execution);
        Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(invocation);
        Object result = invocation.getInvocationResult();
        if (result != null && resultVariable != null) {
          execution.setVariable(resultVariable, result);
        }
        leave(execution);
        return null;
      }
    });
  }

  /**
   * Searches recursively through the exception to see if the exception itself
   * or one of its causes is a {@link BpmnError}.
   *
   * @param e
   *          the exception to check
   * @return the BpmnError that was the cause of this exception or null if no
   *         BpmnError was found
   */
  protected BpmnError checkIfCauseOfExceptionIsBpmnError(Throwable e) {
    if (e instanceof BpmnError) {
      return (BpmnError) e;
    } else if (e.getCause() == null) {
      return null;
    }
    return checkIfCauseOfExceptionIsBpmnError(e.getCause());
  }

  public ExecutableScript getScript() {
    return script;
  }

}
