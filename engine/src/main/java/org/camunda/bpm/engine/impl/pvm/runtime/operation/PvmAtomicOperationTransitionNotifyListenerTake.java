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
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationTransitionNotifyListenerTake implements PvmAtomicOperation {

  private static Logger log = Logger.getLogger(PvmAtomicOperationTransitionNotifyListenerTake.class.getName());

  public boolean isAsync(PvmExecutionImpl execution) {
    return execution.getActivity().isAsyncAfter();
  }

  public void execute(PvmExecutionImpl execution) {
    TransitionImpl transition = execution.getTransition();

    // while executing the transition, the activityInstance is 'null'
    // (we are not executing an activity)
    execution.setActivityInstanceId(null);

    List<DelegateListener<? extends BaseDelegateExecution>> executionListeners = transition.getListeners(ExecutionListener.EVENTNAME_TAKE);
    int executionListenerIndex = execution.getListenerIndex();

    if (executionListeners.size()>executionListenerIndex) {
      execution.setEventName(ExecutionListener.EVENTNAME_TAKE);
      execution.setEventSource(transition);
      DelegateListener<? extends BaseDelegateExecution> listener = executionListeners.get(executionListenerIndex);
      try {
        execution.invokeListener(listener);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new PvmException("couldn't execute event listener : "+e.getMessage(), e);
      }
      execution.setListenerIndex(executionListenerIndex + 1);
      execution.performOperationSync(this);

    } else {
      log.fine(execution+" takes transition "+transition);
      execution.setListenerIndex(0);
      execution.setEventName(null);
      execution.setEventSource(null);

      ActivityImpl activity = execution.getActivity();
      ActivityImpl nextScope = findNextScope(activity.getFlowScope(), transition.getDestination());
      execution.setActivity(nextScope);

      if (nextScope.isCancelScope()) {
        execution.performOperation(TRANSITION_CANCEL_SCOPE);
      } else {
        execution.performOperation(TRANSITION_CREATE_SCOPE);
      }
    }
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

  public String getCanonicalName() {
    return "transition-notify-listener-take";
  }
}
