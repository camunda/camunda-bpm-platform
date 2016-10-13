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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationTransitionNotifyListenerStart extends PvmAtomicOperationActivityInstanceStart {

  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    return execution.getActivity();
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_START;
  }

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {

    super.eventNotificationsCompleted(execution);

    TransitionImpl transition = execution.getTransition();
    PvmActivity destination;
    if (transition == null) { // this is null after async cont. -> transition is not stored in execution
      destination = execution.getActivity();
    } else {
      destination = transition.getDestination();
    }
    execution.setTransition(null);
    execution.setActivity(destination);

    ExecutionStartContext executionStartContext = execution.getExecutionStartContext();
    if (executionStartContext != null) {
      executionStartContext.executionStarted(execution);
      execution.disposeExecutionStartContext();
    }

    execution.dispatchDelayedEventsAndPerformOperation(ACTIVITY_EXECUTE);
  }

  public String getCanonicalName() {
    return "transition-notifiy-listener-start";
  }
}
