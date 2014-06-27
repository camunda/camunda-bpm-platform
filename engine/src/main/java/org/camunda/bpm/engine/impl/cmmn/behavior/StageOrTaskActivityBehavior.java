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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.CaseRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;

/**
 * @author Roman Smirnov
 *
 */
public abstract class StageOrTaskActivityBehavior extends PlanItemDefinitionActivityBehavior {

  public void created(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    // Step 1: Check Entry Sentries
    // TODO: Check Entry Sentries, if the entryCriterias
    // are not fulfilled then stay in state AVAILABLE.

    // Step 2: Check ManualActiviation
    boolean manualActivation = true;
    Object requiredRule = activity.getProperty("manualActivationRule");
    if (requiredRule != null) {
      CaseRule rule = (CaseRule) requiredRule;
      manualActivation = rule.evaluate(execution);
    }

    if (manualActivation) {
      execution.enable();
    } else {
      execution.start();
    }

  }

  public void onEnable(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "enable");
    ensureTransitionAllowed(execution, AVAILABLE, ENABLED, "enable");
  }

  public void onReenable(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "re-enable");
    ensureTransitionAllowed(execution, DISABLED, ENABLED, "re-enable");
  }

  public void onDisable(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "re-enable");
    ensureTransitionAllowed(execution, ENABLED, DISABLED, "re-enable");
  }

  public void onStart(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "start");
    ensureTransitionAllowed(execution, AVAILABLE, ACTIVE, "start");
  }

  public void onManualStart(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "manualStart");
    ensureTransitionAllowed(execution, ENABLED, ACTIVE, "start");
  }

  public void onCompletion(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, COMPLETED, "complete");
    completing(execution);
  }

  public void onManualCompletion(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, COMPLETED, "complete");
    completing(execution);
  }

  public void onTermination(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, TERMINATED, "terminate");
    terminating(execution);
  }

  public void onParentTermination(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition parentTerminate on a task or a stage.");
  }

  public void onExit(CmmnActivityExecution execution) {
    String id = execution.getId();

    if (execution.isTerminated()) {
      throw new ProcessEngineException("Case execution '"+id+"' is already terminated.");
    }

    if (execution.isCompleted()) {
      String message = "Case execution '"+id+"' must be available|enabled|disabled|active|failed|suspended to exit it, but was completed.";
      throw new ProcessEngineException(message);
    }
    terminating(execution);
  }

  public void onOccur(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition occur on a task or a stage.");
  }

  public void onSuspension(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, ACTIVE, SUSPENDED, "suspend");
    suspending(execution);
  }

  public void onParentSuspension(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "parentSuspension");

    String id = execution.getId();

    if (execution.isSuspended()) {
      throw new ProcessEngineException("Case execution '"+id+"' is already suspended.");
    }

    if (execution.isCompleted() || execution.isTerminated()) {
      String message = "Case execution '"+id+"' must be available|enabled|disabled|active to suspend it, but was completed or terminated.";
      throw new ProcessEngineException(message);
    }
    suspending(execution);
  }

  public void onResume(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "resume");
    ensureTransitionAllowed(execution, SUSPENDED, ACTIVE, "resume");

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        throw new ProcessEngineException("It is not possible to resume a case execution which parent is not active.");
      }
    }

    resuming(execution);

  }

  public void onParentResume(CmmnActivityExecution execution) {
    ensureNotCaseInstance(execution, "parentResume");

    if (!execution.isSuspended()) {
      String id = execution.getId();
      String message = "Case execution '"+id+"' must be available|enabled|disabled|active to suspend it, but was completed or terminated.";
      throw new ProcessEngineException(message);
    }

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        throw new ProcessEngineException("It is not possible to resume a case execution which parent is not active.");
      }
    }

    resuming(execution);

  }

  protected void creating(CmmnActivityExecution execution) {
    evaluateRequiredRule(execution);
    evaluateRepetitionRule(execution);
  }

}
