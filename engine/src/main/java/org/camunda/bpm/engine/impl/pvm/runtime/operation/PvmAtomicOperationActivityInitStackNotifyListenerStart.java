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

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.InstantiationStack;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class PvmAtomicOperationActivityInitStackNotifyListenerStart extends PvmAtomicOperationActivityInstanceStart {

  public String getCanonicalName() {
    return "activity-init-stack-notify-listener-start";
  }

  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();

    if (activity!=null) {
      return activity;
    } else {
      PvmExecutionImpl parent = execution.getParent();
      if (parent != null) {
        return getScope(execution.getParent());
      }
      return execution.getProcessDefinition();
    }
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_START;
  }

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {
    super.eventNotificationsCompleted(execution);

    execution.activityInstanceStarted();

    ExecutionStartContext startContext = execution.getExecutionStartContext();
    InstantiationStack instantiationStack = startContext.getInstantiationStack();

    PvmExecutionImpl propagatingExecution = execution;
    ActivityImpl activity = execution.getActivity();
    if (activity.getActivityBehavior() instanceof ModificationObserverBehavior) {
      ModificationObserverBehavior behavior = (ModificationObserverBehavior) activity.getActivityBehavior();
      List<ActivityExecution> concurrentExecutions = behavior.initializeScope(propagatingExecution, 1);
      propagatingExecution = (PvmExecutionImpl) concurrentExecutions.get(0);
    }

    // if the stack has been instantiated
    if (instantiationStack.getActivities().isEmpty() && instantiationStack.getTargetActivity() != null) {
      // as if we are entering the target activity instance id via a transition
      propagatingExecution.setActivityInstanceId(null);

      // execute the target activity with this execution
      startContext.applyVariables(propagatingExecution);
      propagatingExecution.setActivity(instantiationStack.getTargetActivity());
      propagatingExecution.performOperation(ACTIVITY_START_CREATE_SCOPE);

    }
    else if (instantiationStack.getActivities().isEmpty() && instantiationStack.getTargetTransition() != null) {
      // as if we are entering the target activity instance id via a transition
      propagatingExecution.setActivityInstanceId(null);

      // execute the target transition with this execution
      PvmTransition transition = instantiationStack.getTargetTransition();
      startContext.applyVariables(propagatingExecution);
      propagatingExecution.setActivity(transition.getSource());
      propagatingExecution.setTransition((TransitionImpl) transition);
      propagatingExecution.performOperation(TRANSITION_START_NOTIFY_LISTENER_TAKE);
    }
    else {
      // else instantiate the activity stack further
      propagatingExecution.setActivity(null);
      propagatingExecution.performOperation(ACTIVITY_INIT_STACK);

    }

  }

}
