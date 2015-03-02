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

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractPvmAtomicOperationTransitionNotifyListenerTake extends AbstractPvmEventAtomicOperation {

  protected PvmExecutionImpl eventNotificationsStarted(PvmExecutionImpl execution) {
    // while executing the transition, the activityInstance is 'null'
    // (we are not executing an activity)
    execution.setActivityInstanceId(null);
    return execution;
  }

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {
    TransitionImpl transition = execution.getTransition();
    ActivityImpl activity = execution.getActivity();
    ActivityImpl nextScope = findNextScope(activity.getFlowScope(), transition.getDestination());
    execution.setActivity(nextScope);

    if (nextScope.isCancelScope()) {
      execution.performOperation(TRANSITION_CANCEL_SCOPE);
    } else {
      execution.performOperation(TRANSITION_CREATE_SCOPE);
    }
  }

  protected CoreModelElement getScope(PvmExecutionImpl execution) {
    return execution.getTransition();
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_TAKE;
  }

  /** finds the next scope to enter.  the most outer scope is found first */
  public static ActivityImpl findNextScope(ScopeImpl outerScopeElement, ActivityImpl destination) {
    ActivityImpl nextScope = destination;
    while( (nextScope.getParent() instanceof ActivityImpl)
           && (nextScope.getParent() != outerScopeElement)
         ) {
      nextScope = (ActivityImpl) nextScope.getParent();
    }
    return nextScope;
  }
}
