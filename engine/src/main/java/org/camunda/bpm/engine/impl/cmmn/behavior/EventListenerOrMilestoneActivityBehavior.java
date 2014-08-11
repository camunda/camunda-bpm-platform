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

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;

/**
 * @author Roman Smirnov
 *
 */
public abstract class EventListenerOrMilestoneActivityBehavior extends PlanItemDefinitionActivityBehavior {

  public void onEnable(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("enable", execution);
  }

  public void onReenable(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("reenable", execution);
  }

  public void onDisable(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("disable", execution);
  }

  public void onStart(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("start", execution);
  }

  public void onManualStart(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("manualStart", execution);
  }

  public void onCompletion(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("complete", execution);
  }

  public void onManualCompletion(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("complete", execution);
  }

  public void onTermination(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, AVAILABLE, TERMINATED, "terminate");
    terminating(execution);
  }

  public void onParentTermination(CmmnActivityExecution execution) {
    String id = execution.getId();

    if (execution.isTerminated()) {
      String message = "Case execution '"+id+"' is already terminated.";
      throwIllegalStateTransitionException("parentTerminate", message, execution);
    }

    if (execution.isCompleted()) {
      String message = "Case execution '"+id+"' must be available or suspended, but was completed.";
      throwIllegalStateTransitionException("parentTerminate", message, execution);
    }
    terminating(execution);
  }

  public void onExit(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("exit", execution);
  }

  public void onOccur(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, AVAILABLE, COMPLETED, "occur");
  }

  public void onSuspension(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, AVAILABLE, SUSPENDED, "suspend");
    suspending(execution);
  }

  public void onParentSuspension(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("parentSuspend", execution);
  }

  public void onResume(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, SUSPENDED, AVAILABLE, "resume");

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        String id = execution.getId();
        String message = "It is not possible to resume case execution '"+id+"' which parent is not active.";
        throwIllegalStateTransitionException("resume", message, execution);
      }
    }

    resuming(execution);
  }

  public void onParentResume(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("parentResume", execution);
  }

  public void onReactivation(CmmnActivityExecution execution) {
    throwIllegalStateTransitionException("reactivate", execution);
  }

  protected void throwIllegalStateTransitionException(String transition, CmmnActivityExecution execution) {
    String id = execution.getId();
    String message = String.format("It is not possible to %s case execution '%s' which associated with a %s.", transition, id, getTypeName());
    throwIllegalStateTransitionException(transition, message, execution);
  }

  protected abstract String getTypeName();

}
