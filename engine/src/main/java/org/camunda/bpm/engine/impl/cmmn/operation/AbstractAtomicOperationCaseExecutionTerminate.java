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
package org.camunda.bpm.engine.impl.cmmn.operation;

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.pvm.PvmException;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractAtomicOperationCaseExecutionTerminate extends AbstractCmmnEventAtomicOperation {

  protected CmmnExecution eventNotificationsStarted(CmmnExecution execution) {
    try {
      CmmnActivityBehavior behavior = getActivityBehavior(execution);
      triggerBehavior(behavior, execution);

      execution.setCurrentState(TERMINATED);

    } catch (RuntimeException e) {
      String id = execution.getId();
      throw new PvmException("Cannot "+getEventName()+" case execution '"+id+"'.", e);
    }


    return execution;
  }

  protected void eventNotificationsCompleted(CmmnExecution execution) {
    if (!execution.isCaseInstanceExecution()) {
      execution.remove();
    }

    // TODO: We need to know what kind of termination happens!
    // if a case execution will be terminated because the exitCriterias
    // are fulfilled, then it will be "exit" executed on the case execution.
    // in that case the case execution have to notify the parent too.
    // but if the transition "exit" will be executed, because the
    // parent has been terminated, we do not care about the notification
    // of the parent.

    CmmnExecution parent = execution.getParent();
    if (parent != null) {
      notifyParent(parent, execution);
    }
  }

  protected abstract void triggerBehavior(CmmnActivityBehavior behavior, CmmnExecution execution);

  protected void notifyParent(CmmnExecution parent, CmmnExecution execution) {
    // noop
  }

}
