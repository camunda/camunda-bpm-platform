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

import static org.camunda.bpm.engine.delegate.CaseExecutionListener.DISABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.pvm.PvmException;

/**
 * @author Roman Smirnov
 *
 */
public class AtomicOperationCaseExecutionDisable extends AbstractCmmnEventAtomicOperation {

  public String getCanonicalName() {
    return "case-execution-disable";
  }

  protected String getEventName() {
    return DISABLE;
  }

  protected CmmnExecution eventNotificationsStarted(CmmnExecution execution) {
    try {

      CmmnActivityBehavior behavior = getActivityBehavior(execution);
      behavior.onDisable(execution);

      execution.setCurrentState(DISABLED);

    } catch (RuntimeException e) {
      String id = execution.getId();
      throw new PvmException("Cannot disable case execution '"+id+"'.", e);
    }

    return execution;
  }

  protected void eventNotificationsCompleted(CmmnExecution execution) {
    CmmnExecution parent = execution.getParent();
    if (parent != null) {
      CmmnActivityBehavior behavior = getActivityBehavior(parent);
      if (behavior instanceof CompositeActivityBehavior) {
        CompositeActivityBehavior compositeBehavior = (CompositeActivityBehavior) behavior;
        compositeBehavior.childStateChanged(parent, execution);
      }
    }
  }

}
