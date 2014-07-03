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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.*;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Roman Smirnov
 *
 */
public class StageActivityBehavior extends StageOrTaskActivityBehavior implements CompositeActivityBehavior {

  public void started(CmmnActivityExecution execution) throws Exception {
    CmmnActivity activity = execution.getActivity();
    List<CmmnActivity> childActivities = activity.getActivities();

    if (childActivities != null && !childActivities.isEmpty()) {
      execution.createChildExecutions(childActivities);
    } else {
      execution.complete();
    }
  }

  public void onReactivation(CmmnActivityExecution execution) {
    if (execution.isActive()) {
      throw new ProcessEngineException("Case execution is already active.");
    }

    if (execution.isCaseInstanceExecution()) {
      if (execution.isClosed()) {
        throw new ProcessEngineException("It is not possible to re-activate a closed case instance.");
      }
    } else {
      ensureTransitionAllowed(execution, FAILED, ACTIVE, "re-activate");
    }

  }

  public void onCompletion(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, COMPLETED, "complete");
    canComplete(execution, false, true);
    completing(execution);
  }

  public void onManualCompletion(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, COMPLETED, "complete");
    canComplete(execution, true, true);
    completing(execution);
  }

  protected boolean canComplete(CmmnActivityExecution execution, boolean manualCompletion, boolean throwException) {
    CmmnActivity activity = execution.getActivity();

    List<? extends CmmnExecution> children = execution.getCaseExecutions();

    if (children == null || children.isEmpty()) {
      // if the stage does not contain any child
      // then the stage can complete.
      return true;
    }

    // verify there are no ACTIVE children
    for (CmmnExecution child : children) {
      // TODO: child is ACTIVE or NEW
      if (child.isActive()) {

        if (throwException) {
          throw new ProcessEngineException("At least one child case execution is active.");
        }

        return false;
      }
    }

    // get autoComplete property
    Object autoCompleteProperty = activity.getProperty("autoComplete");
    boolean autoComplete = false;
    if (autoCompleteProperty != null) {
      autoComplete = (Boolean) autoCompleteProperty;
    }

    if (autoComplete || manualCompletion) {
      // ensure that all required children are DISABLED
      // NOTE: All COMPLETED and TERMINATED children are not
      // available in the case execution tree.

      for (CmmnExecution child : children) {
        if (child.isRequired() && !child.isDisabled()) {

          if (throwException) {
            throw new ProcessEngineException("At least one required child case execution is active.");
          }

          return false;
        }
      }

    } else { /* autoComplete == false && manualCompletion == true */
      // ensure that ALL children are DISABLED
      // NOTE: All COMPLETED and TERMINATED children are not
      // available in the case execution tree.

      for (CmmnExecution child : children) {
        if (!child.isDisabled()) {

          if (throwException) {
            throw new ProcessEngineException("At least one required child case execution is availabe|enabled|suspended.");
          }

          return false;
        }
      }

      // TODO: are there any DiscretionaryItems?
      // if yes, then it is not possible to complete
      // this stage (NOTE: manualCompletion == false)!

    }

    return true;
  }

  public void childStateChanged(CmmnExecution execution, CmmnExecution child) {
    if (child.isDisabled() || child.isCompleted() || child.isTerminated()) {
      if (canComplete(execution, false, false)) {
        execution.complete();
      }
    }
  }

  protected CmmnActivityBehavior getActivityBehavior(CmmnActivityExecution execution) {
    String id = execution.getId();

    CmmnActivity activity = execution.getActivity();

    ensureNotNull("Case execution '" + id + "': has no current activity", "activity", activity);

    CmmnActivityBehavior behavior = activity.getActivityBehavior();

    ensureNotNull("There is no behavior specified in " + activity + " for case execution '" + id + "'", "behavior", behavior);

    return behavior;
  }

  protected void terminating(CmmnActivityExecution execution) {
    List<? extends CmmnExecution> children = execution.getCaseExecutions();
    if (children != null && !children.isEmpty()) {

      for (CmmnExecution child : children) {

        CmmnActivityBehavior behavior = getActivityBehavior(child);

        if (behavior instanceof StageOrTaskActivityBehavior) {
          child.exit();
        } else { /* behavior instanceof EventListenerOrMilestoneActivityBehavior */
          child.parentTerminate();
        }
      }
    }
  }

  public void suspending(CmmnActivityExecution execution) {
    List<? extends CmmnExecution> children = execution.getCaseExecutions();
    if (children != null && !children.isEmpty()) {

      for (CmmnExecution child : children) {
        CmmnActivityBehavior behavior = getActivityBehavior(child);

        if (behavior instanceof StageOrTaskActivityBehavior) {
          child.parentSuspend();
        } else { /* behavior instanceof EventListenerOrMilestoneActivityBehavior */
          child.suspend();
        }
      }
    }
  }

  public void resumed(CmmnActivityExecution execution) {
    List<? extends CmmnExecution> children = execution.getCaseExecutions();
    if (children != null && !children.isEmpty()) {

      for (CmmnExecution child : children) {
        CmmnActivityBehavior behavior = getActivityBehavior(child);

        if (behavior instanceof StageOrTaskActivityBehavior) {
          child.parentResume();
        } else { /* behavior instanceof EventListenerOrMilestoneActivityBehavior */
          child.resume();
        }
      }
    }
  }

  public void reactivated(CmmnActivityExecution execution) {
    if (execution.isCaseInstanceExecution()) {
      CaseExecutionState previousState = execution.getPreviousState();

      if (SUSPENDED.equals(previousState)) {
        resumed(execution);
      }
    }
  }

}
