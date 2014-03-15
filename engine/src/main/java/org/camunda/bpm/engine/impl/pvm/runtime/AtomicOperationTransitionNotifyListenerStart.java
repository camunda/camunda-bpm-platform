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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionStartContext;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationTransitionNotifyListenerStart extends AtomicOperationActivityInstanceStart {

  @Override
  protected ScopeImpl getScope(InterpretableExecution execution) {
    return (ScopeImpl) execution.getActivity();
  }

  @Override
  protected String getEventName() {
    return org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START;
  }

  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {

    super.eventNotificationsCompleted(execution);

    TransitionImpl transition = execution.getTransition();
    ActivityImpl destination = null;
    if(transition == null) { // this is null after async cont. -> transition is not stored in execution
      destination = (ActivityImpl) execution.getActivity();
    } else {
      destination = transition.getDestination();
    }
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    if (activity!=destination) {
      ActivityImpl nextScope = AtomicOperationTransitionNotifyListenerTake.findNextScope(activity, destination);
      execution.setActivity(nextScope);
      execution.performOperation(TRANSITION_CREATE_SCOPE);
    } else {
      execution.setTransition(null);
      execution.setActivity(destination);

      ExecutionStartContext executionStartContext = execution.getExecutionStartContext();
      if (executionStartContext != null) {
        executionStartContext.initialStarted(execution);
        execution.disposeExecutionStartContext();
      }

      execution.performOperation(ACTIVITY_EXECUTE);
    }
  }

  @Override
  public String getCanonicalName() {
    return "transition-notifiy-listener-start";
  }
}
