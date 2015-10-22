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
 * <p>Base atomic operation used for implementing atomic operations which
 * create a new concurrent execution for executing an activity. This atomic
 * operation makes sure the execution is created under the correct parent.</p>
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class PvmAtomicOperationCreateConcurrentExecution implements PvmAtomicOperation {

  public void execute(PvmExecutionImpl execution) {

    // Invariant: execution is the Scope Execution for the activity's flow scope.

    PvmActivity activityToStart = execution.getNextActivity();
    execution.setNextActivity(null);

    PvmExecutionImpl propagatingExecution = execution.createConcurrentExecution();

    // set next activity on propagating execution
    propagatingExecution.setActivity(activityToStart);
    concurrentExecutionCreated(propagatingExecution);
  }

  protected abstract void concurrentExecutionCreated(PvmExecutionImpl propagatingExecution);

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

}
