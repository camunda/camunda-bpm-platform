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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;

/**
 * @author Roman Smirnov
 *
 */
public abstract class StageOrTaskActivityBehavior extends PlanItemDefinitionActivityBehavior {

  protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;

  // creation /////////////////////////////////////////////////////////

  protected void creating(CmmnActivityExecution execution) {
    evaluateRequiredRule(execution);
  }

  public void created(CmmnActivityExecution execution) {
    if (execution.isAvailable() && isAtLeastOneEntryCriterionSatisfied(execution)) {
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
    throw LOG.illegalStateTransitionException("parentTerminate", id, getTypeName());
  }

  public void onExit(CmmnActivityExecution execution) {
    String id = execution.getId();

    if (execution.isTerminated()) {
      throw LOG.alreadyTerminatedException("exit", id);
    }

    if (execution.isCompleted()) {
      throw LOG.wrongCaseStateException("exit", id, "[available|enabled|disabled|active|failed|suspended]", "completed");
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
      throw LOG.alreadySuspendedException("parentSuspend", id);
    }

    if (execution.isCompleted() || execution.isTerminated()) {
      throw LOG.wrongCaseStateException("parentSuspend", id, "suspend", "[available|enabled|disabled|active]",
        execution.getCurrentState().toString());
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
        throw LOG.resumeInactiveCaseException("resume", id);
      }
    }

    resuming(execution);

  }

  public void onParentResume(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "parentResume");
    String id = execution.getId();

    if (!execution.isSuspended()) {
      throw LOG.wrongCaseStateException("parentResume", id, "resume", "suspended", execution.getCurrentState().toString());
    }

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        throw LOG.resumeInactiveCaseException("parentResume", id);
      }
    }

    resuming(execution);

  }

  // occur ////////////////////////////////////////////////////////

  public void onOccur(CmmnActivityExecution execution) {
    String id = execution.getId();
    throw LOG.illegalStateTransitionException("occur", id, getTypeName());
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
    boolean manualActivation = false;
    CmmnActivity activity = execution.getActivity();
    Object manualActivationRule = activity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    if (manualActivationRule != null) {
      CaseControlRule rule = (CaseControlRule) manualActivationRule;
      manualActivation = rule.evaluate(execution);
    }
    return manualActivation;
  }

  // helper ///////////////////////////////////////////////////////////

  protected abstract String getTypeName();
}
