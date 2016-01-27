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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 *
 */
public abstract class PvmAtomicOperationInterruptScope implements PvmAtomicOperation {

  public void execute(PvmExecutionImpl execution) {
    PvmActivity interruptingActivity = getInterruptingActivity(execution);

    PvmExecutionImpl scopeExecution = !execution.isScope() ? execution.getParent() : execution;

    if (scopeExecution != execution) {
      // remove the current execution before interrupting and continuing executing the interrupted activity
      // reason:
      //   * interrupting should not attempt to fire end events for this execution
      //   * the interruptingActivity is executed with the scope execution
      execution.remove();
    }

    scopeExecution.interrupt("Interrupting activity "+interruptingActivity+" executed.");

    scopeExecution.setActivity(interruptingActivity);
    scopeExecution.setActive(true);
    scopeExecution.setTransition(execution.getTransition());
    scopeInterrupted(scopeExecution);
  }

  protected abstract void scopeInterrupted(PvmExecutionImpl execution);

  protected abstract PvmActivity getInterruptingActivity(PvmExecutionImpl execution);

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

  public boolean isAsyncCapable() {
    return false;
  }

}
