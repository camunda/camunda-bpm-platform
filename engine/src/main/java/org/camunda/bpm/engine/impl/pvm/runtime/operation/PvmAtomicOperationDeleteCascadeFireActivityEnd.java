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
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class PvmAtomicOperationDeleteCascadeFireActivityEnd extends PvmAtomicOperationActivityInstanceEnd {

  protected boolean isSkipNotifyListeners(PvmExecutionImpl execution) {
    return false;
  }

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

    super.eventNotificationsCompleted(execution);

    PvmActivity activity = execution.getActivity();
    if ( (execution.isScope())
            && (activity!=null)
            && (!activity.isScope())
          )  {
      // case this is a scope execution and the activity is not a scope
      execution.setActivity(getFlowScopeActivity(activity));
      execution.performOperation(DELETE_CASCADE_FIRE_ACTIVITY_END);

    } else {
      if (execution.isScope()) {
        execution.destroy();
      }

      execution.remove();

      if (!execution.isDeleteRoot()) {
        PvmExecutionImpl parent = execution.getParent();
        if (parent!=null) {
          // set activity on parent in case the parent is an inactive scope execution and activity has been set to 'null'.
          if(parent.getActivity() == null && activity != null && activity.getFlowScope() != null) {
            parent.setActivity(getFlowScopeActivity(activity));
          }
          parent.performOperation(DELETE_CASCADE);
        }
      }
    }
  }

  private ActivityImpl getFlowScopeActivity(PvmActivity activity) {
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
