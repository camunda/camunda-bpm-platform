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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.*;

/**
 * @author Roman Smirnov
 *
 */
public abstract class EventListenerOrMilestoneActivityBehavior extends PlanItemDefinitionActivityBehavior {

  public void onEnable(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition enable on a event listener or a milestone.");
  }

  public void onReenable(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition re-enable on a event listener or a milestone.");
  }

  public void onDisable(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition disable on a event listener or a milestone.");
  }

  public void onStart(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition start on a event listener or a milestone.");
  }

  public void onManualStart(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition manualStart on a event listener or a milestone.");
  }

  public void started(CmmnActivityExecution execution) throws Exception {
    throw new UnsupportedOperationException("It is not possible to execute the started behavior of a event listener or a milestone.");
  }

  public void onCompletion(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition complete on a event listener or a milestone.");
  }

  public void onManualCompletion(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition complete on a event listener or a milestone.");
  }

  public void onTermination(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, AVAILABLE, TERMINATED, "terminate");
    terminating(execution);
  }

  public void onParentTermination(CmmnActivityExecution execution) {
    String id = execution.getId();

    if (execution.isTerminated()) {
      throw new ProcessEngineException("Case execution '"+id+"' is already terminated.");
    }

    if (execution.isCompleted()) {
      String message = "Case execution '"+id+"' must be available or suspended to parentTerminate it, but was completed.";
      throw new ProcessEngineException(message);
    }
    terminating(execution);
  }

  public void onExit(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition exit on a event listener or a milestone.");
  }

  public void onOccur(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, AVAILABLE, COMPLETED, "occur");
  }

  public void onSuspension(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, AVAILABLE, SUSPENDED, "suspend");
    suspending(execution);
  }

  public void onParentSuspension(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition parentSuspend on a event listener or a milestone.");
  }

  public void onResume(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, SUSPENDED, AVAILABLE, "resume");

    CmmnActivityExecution parent = execution.getParent();
    if (parent != null) {
      if (!parent.isActive()) {
        throw new ProcessEngineException("It is not possible to resume a case execution which parent is not active.");
      }
    }

    resuming(execution);
  }

  public void onParentResume(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition parentResume on a event listener or a milestone.");
  }

  public void onReactivation(CmmnActivityExecution execution) {
    throw new UnsupportedOperationException("It is not possible to execute the transition re-activate on a event listener or a milestone.");
  }

}
