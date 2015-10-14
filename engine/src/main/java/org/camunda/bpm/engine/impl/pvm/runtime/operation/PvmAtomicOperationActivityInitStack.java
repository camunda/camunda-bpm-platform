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

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.InstantiationStack;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Instantiates the next activity on the stack of the current execution's start context.
 *
 * @author Thorben Lindhauer
 */
public class PvmAtomicOperationActivityInitStack implements PvmAtomicOperation {

  public String getCanonicalName() {
    return "activity-stack-init";
  }

  public void execute(PvmExecutionImpl execution) {
    ExecutionStartContext executionStartContext = execution.getExecutionStartContext();

    InstantiationStack instantiationStack = executionStartContext.getInstantiationStack();
    List<PvmActivity> activityStack = instantiationStack.getActivities();
    PvmActivity currentActivity = activityStack.remove(0);

    PvmExecutionImpl propagatingExecution = execution;
    if (currentActivity.isScope()) {
      propagatingExecution = execution.createExecution();
      execution.setActive(false);
      propagatingExecution.setActivity(currentActivity);
      propagatingExecution.initialize();
    }
    else {
      propagatingExecution.setActivity(currentActivity);
    }


    // notify listeners for the instantiated activity
    propagatingExecution.performOperation(ACTIVITY_INIT_STACK_NOTIFY_LISTENER_START);
  }

  public boolean isAsync(PvmExecutionImpl instance) {
    return false;
  }

  public PvmExecutionImpl getStartContextExecution(PvmExecutionImpl execution) {
    return execution;
  }

  public boolean isAsyncCapable() {
    return false;
  }
}
