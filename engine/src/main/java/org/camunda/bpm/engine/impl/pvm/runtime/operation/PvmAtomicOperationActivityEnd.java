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

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class PvmAtomicOperationActivityEnd implements PvmAtomicOperation {

  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    return execution.getActivity();
  }

  public boolean isAsync(PvmExecutionImpl execution) {
    return execution.getActivity().isAsyncAfter();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void execute(PvmExecutionImpl execution) {

    ActivityImpl activity = execution.getActivity();
    ActivityImpl parentActivity = activity.getParentActivity();

    // if the execution is a single path of execution inside the process definition scope
    if ( (parentActivity!=null)
         &&(!parentActivity.isScope())
          ) {
      execution.setActivity(parentActivity);
      execution.performOperation(ACTIVITY_NOTIFY_LISTENER_END);

    } else if (execution.isProcessInstanceExecution()) {
      execution.performOperation(PROCESS_END);

    } else if (execution.isScope()) {

      ActivityBehavior parentActivityBehavior = (parentActivity!=null ? parentActivity.getActivityBehavior() : null);
      if (parentActivityBehavior instanceof CompositeActivityBehavior) {
        CompositeActivityBehavior compositeActivityBehavior = (CompositeActivityBehavior) parentActivity.getActivityBehavior();

        if(activity.isScope() && activity.getOutgoingTransitions().isEmpty()) {
          // there is no transition destroying the scope
          PvmExecutionImpl parentScopeExecution = execution.getParent();
          execution.destroy();
          execution.remove();
          parentScopeExecution.setActivity(parentActivity);
          compositeActivityBehavior.lastExecutionEnded(parentScopeExecution);
        } else {
          execution.setActivity(parentActivity);
          compositeActivityBehavior.lastExecutionEnded(execution);
        }
      } else {
        // default destroy scope behavior
        PvmExecutionImpl parentScopeExecution = execution.getParent();
        execution.destroy();
        execution.remove();
        // if we are a scope under the process instance
        // and have no outgoing transitions: end the process instance here
        if(activity.getParent() == activity.getProcessDefinition()
                && activity.getOutgoingTransitions().isEmpty()) {
          parentScopeExecution.setActivity(activity);
          // we call end() because it sets isEnded on the execution
          parentScopeExecution.performOperation(PROCESS_END);
        } else {
          parentScopeExecution.setActivity(parentActivity);
          parentScopeExecution.performOperation(ACTIVITY_NOTIFY_LISTENER_END);
        }
      }

    } else { // execution.isConcurrent() && !execution.isScope()

      execution.remove();

      // prune if necessary
      PvmExecutionImpl concurrentRoot = execution.getParent();
      if (concurrentRoot.getExecutions().size()==1) {
        PvmExecutionImpl lastConcurrent = concurrentRoot.getExecutions().get(0);
        if (!lastConcurrent.isScope()) {
          concurrentRoot.setActivity(lastConcurrent.getActivity());
          lastConcurrent.setReplacedBy(concurrentRoot);

          // Move children of lastConcurrent one level up
          if (lastConcurrent.getExecutions().size() > 0) {
            concurrentRoot.getExecutions().clear();
            for (PvmExecutionImpl childExecution : lastConcurrent.getExecutions()) {
              ((List)concurrentRoot.getExecutions()).add(childExecution); // casting ... damn generics
              childExecution.setParent(concurrentRoot);
            }
            lastConcurrent.getExecutions().clear();
          }

          // Copy execution-local variables of lastConcurrent
          concurrentRoot.setVariablesLocal(lastConcurrent.getVariablesLocal());

          // Make sure parent execution is re-activated when the last concurrent child execution is active
          if (!concurrentRoot.isActive() && lastConcurrent.isActive()) {
            concurrentRoot.setActive(true);
          }

          lastConcurrent.remove();
        } else {
          lastConcurrent.setConcurrent(false);
        }
      }
    }
  }

  protected boolean isExecutionAloneInParent(PvmExecutionImpl execution) {
    ScopeImpl parentScope = execution.getActivity().getParent();
    for (PvmExecutionImpl other: execution.getParent().getExecutions()) {
      if (other!=execution && parentScope.contains(other.getActivity())) {
        return false;
      }
    }
    return true;
  }

  public String getCanonicalName() {
    return "activity-end";
  }

}
