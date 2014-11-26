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

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import java.util.logging.Logger;


/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationTransitionCreateScope implements PvmAtomicOperation {

  private static Logger log = Logger.getLogger(PvmAtomicOperationTransitionCreateScope.class.getName());

  public boolean isAsync(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    return activity.isAsyncBefore();
  }

  public void execute(PvmExecutionImpl execution) {

    // we are continuing execution along this sequence flow:
    // reset activity instance id before creating the scope
    execution.setActivityInstanceId(execution.getParentActivityInstanceId());

    PvmExecutionImpl propagatingExecution = null;
    ActivityImpl activity = execution.getActivity();
    if (activity.isScope()) {
      propagatingExecution = execution.createExecution();
      propagatingExecution.setActivity(activity);
      propagatingExecution.setTransition(execution.getTransition());
      execution.setTransition(null);
      execution.setActive(false);
      execution.setActivity(null);
      log.fine("create scope: parent "+execution+" continues as execution "+propagatingExecution);
      propagatingExecution.initialize();

    } else {
      propagatingExecution = execution;
    }

    propagatingExecution.performOperation(TRANSITION_NOTIFY_LISTENER_START);
  }

  public String getCanonicalName() {
    return "transition-create-scope";
  }
}
