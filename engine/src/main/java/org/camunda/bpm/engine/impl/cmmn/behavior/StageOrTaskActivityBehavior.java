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

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE;

import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;

/**
 * @author Roman Smirnov
 *
 */
public abstract class StageOrTaskActivityBehavior extends PlanItemDefinitionActivityBehavior {

  // creation /////////////////////////////////////////////////////////

  protected void creating(CmmnActivityExecution execution) {
    evaluateRequiredRule(execution);
    evaluateRepetitionRule(execution);
  }

  public void created(CmmnActivityExecution execution) {
    if (!execution.isCompleted() && !execution.isTerminated() && isAtLeastOneExitCriteriaSatisfied(execution)) {
      fireExitCriteria(execution);

    } else if (execution.isAvailable() && isAtLeastOneEntryCriteriaSatisfied(execution)) {
      fireEntryCriteria(execution);
    }
  }

  // enable ////////////////////////////////////////////////////////////

  public void onEnable(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "enable");
    ensureTransitionAllowed(execution, AVAILABLE, ENABLED, "enable");
  }

  // re-enable /////////////////////////////////////////////////////////

  public void onReenable(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "re-enable");
    ensureTransitionAllowed(execution, DISABLED, ENABLED, "re-enable");
  }

  // disable ///////////////////////////////////////////////////////////

  public void onDisable(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "disable");
    ensureTransitionAllowed(execution, ENABLED, DISABLED, "disable");
  }

  // start /////////////////////////////////////////////////////////////

  public void onStart(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "start");
    ensureTransitionAllowed(execution, AVAILABLE, ACTIVE, "start");
  }

  public void onManualStart(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "manualStart");
    ensureTransitionAllowed(execution, ENABLED, ACTIVE, "start");
  }

  public void started(CmmnActivityExecution execution) {
    // only perform start behavior, when this case execution is
    // still active.
    // it can happen that a exit sentry will be triggered, so that
    // the given case execution will be terminated, in that case we
    // do not need to perform the start behavior
    if (execution.isActive()) {
      performStart(execution);
    }
  }

  protected abstract void performStart(CmmnActivityExecution execution);

  // completion ////////////////////////////////////////////////////////

  public void onCompletion(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, COMPLETED, "complete");
    completing(execution);
  }

  public void onManualCompletion(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, COMPLETED, "complete");
    manualCompleting(execution);
  }

  // termination //////////////////////////////////////////////////////

  public void onTermination(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, TERMINATED, "terminate");
    performTerminate(execution);
  }

  public void onParentTermination(CmmnActivityExecution execution) {
    String id = execution.getId();
    String message = "It is not possible to parentTerminate case execution '"+id+"' which associated with a "+getTypeName()+".";
    throw createIllegalStateTransitionException("parentTerminate", message, execution);
  }

  public void onExit(CmmnActivityExecution execution) {
    String id = execution.getId();

    if (execution.isTerminated()) {
      String message = "Case execution '"+id+"' is already terminated.";
      throw createIllegalStateTransitionException("exit", message, execution);
    }

    if (execution.isCompleted()) {
      String message = "Case execution '"+id+"' must be {available|enabled|disabled|active|failed|suspended} to exit it, but was completed.";
      throw createIllegalStateTransitionException("exit", message, execution);
    }

    performExit(execution);
  }

  // suspension ///////////////////////////////////////////////////////////

  public void onSuspension(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, SUSPENDED, "suspend");
    performSuspension(execution);
  }

  public void onParentSuspension(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "parentSuspension");

    String id = execution.getId();

    if (execution.isSuspended()) {
      String message = "Case execution '"+id+"' is already suspended.";
      throw createIllegalStateTransitionException("parentSuspend", message, execution);
    }

    if (execution.isCompleted() || execution.isTerminated()) {
      String message = "Case execution '"+id+"' must be {available|enabled|disabled|active} to suspend it, but was "+execution.getCurrentState()+".";
      throw createIllegalStateTransitionException("parentSuspend", message, execution);
    }

    performParentSuspension(execution);
  }

  // resume /////////////////////////////////////////////////////////////////

  public void onResume(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "resume");
    ensureTransitionAllowed(execution, SUSPENDED, ACTIVE, "resume");

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        String id = execution.getId();
        String message = "It is not possible to resume case execution '"+id+"' which parent is not active.";
        throw createIllegalStateTransitionException("resume", message, execution);
      }
    }

    resuming(execution);

  }

  public void onParentResume(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "parentResume");
    String id = execution.getId();

    if (!execution.isSuspended()) {
      String message = "Case execution '"+id+"' must be suspended to resume it, but was "+execution.getCurrentState()+".";
      throw createIllegalStateTransitionException("parentResume", message, execution);
    }

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        String message = "It is not possible to resume case execution '"+id+"' which parent is not active.";
        throw createIllegalStateTransitionException("parentResume", message, execution);
      }
    }

    resuming(execution);

  }

  // occur ////////////////////////////////////////////////////////

  public void onOccur(CmmnActivityExecution execution) {
    String id = execution.getId();
    String message = "It is not possible to occur case execution '"+id+"' which associated with a "+getTypeName()+".";
    throw createIllegalStateTransitionException("occur", message, execution);
  }


  // sentry ///////////////////////////////////////////////////////////////

  public void fireEntryCriteria(CmmnActivityExecution execution) {
    boolean manualActivation = evaluateManualActivationRule(execution);

    if (manualActivation) {
      execution.enable();

    } else {
      execution.start();
    }
  }

  // manual activation rule //////////////////////////////////////////////

  protected boolean evaluateManualActivationRule(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    Object manualActivationRule = activity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    if (manualActivationRule != null) {
      CaseControlRule rule = (CaseControlRule) manualActivationRule;
      return rule.evaluate(execution);
    }

    return true;
  }

  // helper ///////////////////////////////////////////////////////////

  protected abstract String getTypeName();
}
