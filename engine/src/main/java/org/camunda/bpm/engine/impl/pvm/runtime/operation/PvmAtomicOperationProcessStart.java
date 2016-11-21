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

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.Callback;
import org.camunda.bpm.engine.impl.pvm.runtime.InstantiationStack;
import org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class PvmAtomicOperationProcessStart extends AbstractPvmEventAtomicOperation {

  @Override
  public boolean isAsync(PvmExecutionImpl execution) {
    ProcessInstanceStartContext startContext = execution.getProcessInstanceStartContext();
    return startContext != null && startContext.isAsync();
  }

  public boolean isAsyncCapable() {
    return true;
  }

  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    return execution.getProcessDefinition();
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_START;
  }

  protected PvmExecutionImpl eventNotificationsStarted(PvmExecutionImpl execution) {
    // Note: the following method call initializes the property
    // "processInstanceStartContext" on the given execution.
    // Do not remove it!
    execution.getProcessInstanceStartContext();
    return execution;
  }

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {

    execution.continueIfExecutionDoesNotAffectNextOperation(new Callback<PvmExecutionImpl, Void>() {
      @Override
      public Void callback(PvmExecutionImpl execution) {
        execution.dispatchEvent(null);
        return null;
      }
    }, new Callback<PvmExecutionImpl, Void>() {
      @Override
      public Void callback(PvmExecutionImpl execution) {
        ProcessInstanceStartContext processInstanceStartContext = execution.getProcessInstanceStartContext();
        InstantiationStack instantiationStack = processInstanceStartContext.getInstantiationStack();

        if (instantiationStack.getActivities().isEmpty()) {
          execution.setActivity(instantiationStack.getTargetActivity());
          execution.performOperation(ACTIVITY_START_CREATE_SCOPE);
        } else {
          // initialize the activity instance id
          execution.setActivityInstanceId(execution.getId());
          execution.performOperation(ACTIVITY_INIT_STACK);

        }
        return null;
      }
    }, execution);

  }

  public String getCanonicalName() {
    return "process-start";
  }

}
