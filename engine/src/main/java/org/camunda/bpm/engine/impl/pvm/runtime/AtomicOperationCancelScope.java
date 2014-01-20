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
package org.camunda.bpm.engine.impl.pvm.runtime;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;

/**
 * <p>Base class for implementing an atomic operation which performs <em>cancel scope</em> behavior.</p>
 *
 * <p>Cancel scope behavior is different from "destroy scope" behavior. Destroy scope will delete the
 * scope execution for the current scope. <em>cancel scope</em> will not delete the scope execution itself
 * but will rather</p>
 * <ul>
 *   <li>find the scope execution for the current scope,</li>
 *   <li>perform an interrupt: this will delete any executions / subprocess instances etc
 *     which are child executions of the scope</li>
 *   <li>set the scope execution to the cancelling activity and move forward.</li>
 * </ul>
 * <p>So as opposed to <em>destroy scope</em> we will not delete the current scope execution but cancel
 * anything that is happening in the current scope and then, still in the current scope, execute the
 * canceling activity.</p>
 *
 * <h2>Usage in BPMN:</h2>
 * <p>In the context of BPMN this behavior is required for interrupting constructs like</p>
 * <ul>
 *   <li>boundary events with cancelActivity="true"</li>
 *   <li>interrupting event subprocesses,</li>
 *   <li>terminate end events etc..</li>
 * </ul>
 *
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class AtomicOperationCancelScope implements AtomicOperation {

  public void execute(InterpretableExecution execution) {
    PvmActivity activity = getCancellingActivity(execution);

    // find scope execution:
    InterpretableExecution scopeExecution = execution.isScope() ? execution : execution.getParent();

    PvmScope scope = activity.getScope();
    if (scope != activity.getParent()) {

      if (activity.getParent() instanceof PvmActivity) {
        PvmActivity parent = (PvmActivity) activity.getParent();

        if (parent.isScope()) {
          scopeExecution = (InterpretableExecution) scopeExecution.getParent();
        }
      }
    }

    // cancel the current scope (removes all child executions)
    scopeExecution.cancelScope("Cancel scope activity " + activity + " executed.");

    // set new activity
    scopeExecution.setActivity(activity);
    scopeExecution.setActive(true);

    scopeCancelled(scopeExecution);
  }

  protected abstract void scopeCancelled(InterpretableExecution execution);

  protected abstract PvmActivity getCancellingActivity(InterpretableExecution execution);

  public boolean isAsync(InterpretableExecution execution) {
    return false;
  }

}
