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
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.CompensationBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class PvmAtomicOperationDeleteCascadeFireActivityEnd extends PvmAtomicOperationActivityInstanceEnd {

  @Override
  protected PvmExecutionImpl eventNotificationsStarted(PvmExecutionImpl execution) {
    execution.setCanceled(true);
    return super.eventNotificationsStarted(execution);
  }

  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();

    if (activity!=null) {
      return activity;
    } else {
      // TODO: when can this happen?
      PvmExecutionImpl parent = execution.getParent();
      if (parent != null) {
        return getScope(execution.getParent());
      }
      return execution.getProcessDefinition();
    }
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_END;
  }

  @Override
  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {

    PvmActivity activity = execution.getActivity();

    if (execution.isScope()
        && (executesNonScopeActivity(execution) || isAsyncBeforeActivity(execution))
        && !CompensationBehavior.executesNonScopeCompensationHandler(execution))  {
      execution.removeAllTasks();
      // case this is a scope execution and the activity is not a scope
      execution.leaveActivityInstance();
      execution.setActivity(getFlowScopeActivity(activity));
      execution.performOperation(DELETE_CASCADE_FIRE_ACTIVITY_END);

    } else {
      if (execution.isScope()) {
        execution.destroy();
      }

      // remove this execution and its concurrent parent (if exists)
      execution.remove();

      boolean continueRemoval = !execution.isDeleteRoot();

      if (continueRemoval) {
        PvmExecutionImpl propagatingExecution = execution.getParent();
        if (propagatingExecution != null && !propagatingExecution.isScope() && !propagatingExecution.hasChildren()) {
          propagatingExecution.remove();
          continueRemoval = !propagatingExecution.isDeleteRoot();
          propagatingExecution = propagatingExecution.getParent();
        }

        if (continueRemoval) {
          if (propagatingExecution != null) {
            // continue deletion with the next scope execution
            // set activity on parent in case the parent is an inactive scope execution and activity has been set to 'null'.
            if(propagatingExecution.getActivity() == null && activity != null && activity.getFlowScope() != null) {
              propagatingExecution.setActivity(getFlowScopeActivity(activity));
            }
          }
        }
      }
    }
  }

  protected boolean executesNonScopeActivity(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    return activity!=null && !activity.isScope();
  }

  protected boolean isAsyncBeforeActivity(PvmExecutionImpl execution) {
    return execution.getActivityId() != null && execution.getActivityInstanceId() == null;
  }

  protected ActivityImpl getFlowScopeActivity(PvmActivity activity) {
    ScopeImpl flowScope = activity.getFlowScope();
    ActivityImpl flowScopeActivity = null;
    if(flowScope.getProcessDefinition() != flowScope) {
      flowScopeActivity = (ActivityImpl) flowScope;
    }
    return flowScopeActivity;
  }

  public String getCanonicalName() {
    return "delete-cascade-fire-activity-end";
  }
}
