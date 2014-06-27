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
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.NEW;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.CaseRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;


/**
 * @author Roman Smirnov
 *
 */
public abstract class PlanItemDefinitionActivityBehavior implements CmmnActivityBehavior {

  public void execute(CmmnActivityExecution execution) throws Exception {
    throw new UnsupportedOperationException("execute() is unsupported in CmmnActivityBehavior");
  }

  public void onCreate(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, NEW, AVAILABLE, "create");
    creating(execution);
  }

  public void onClose(CmmnActivityExecution execution) {
    if (execution.isCaseInstanceExecution()) {

      String id = execution.getId();

      if (execution.isClosed()) {
        throw new ProcessEngineException("Case instance'"+id+"' is already closed.");
      }

      if (execution.isActive()) {
        throw new ProcessEngineException("Case instance '"+id+"' must be completed|terminated|failed|suspended to close it, but was active.");
      }

    } else {
      throw new UnsupportedOperationException("It is not possible to close a case execution which is not a case instance.");
    }
  }

  protected void creating(CmmnActivityExecution execution) {
    // noop
  }

  protected void terminating(CmmnActivityExecution execution) {
    // noop
  }

  public void completing(CmmnActivityExecution execution) {
    // noop
  }

  public void suspending(CmmnActivityExecution execution) {
    // noop
  }

  public void resuming(CmmnActivityExecution execution) {
    // noop
  }

  public void resumed(CmmnActivityExecution execution) {
    // noop
  }

  public void reactivated(CmmnActivityExecution execution) {
    // noop
  }

  protected void evaluateRequiredRule(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    Object requiredRule = activity.getProperty("requiredRule");
    if (requiredRule != null) {
      CaseRule rule = (CaseRule) requiredRule;
      boolean required = rule.evaluate(execution);
      execution.setRequired(required);
    }
  }

  protected void evaluateRepetitionRule(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    Object repetitionRule = activity.getProperty("repetitionRule");
    if (repetitionRule != null) {
      CaseRule rule = (CaseRule) repetitionRule;
      rule.evaluate(execution);
      // TODO: set the value on execution?
    }
  }

  protected void ensureTransitionAllowed(CmmnActivityExecution execution, CaseExecutionState expected, CaseExecutionState target, String transition) {
    String id = execution.getId();

    CaseExecutionState currentState = execution.getCurrentState();

    // is the case execution already in the target state
    if (target.equals(currentState)) {
      String message = "Case execution '"+id+"' is already "+target+".";
      throw new ProcessEngineException(message);
    } else
    // is the case execution in the expected state
    if (!expected.equals(currentState)) {
      String message = "Case execution '"+id+"' must be "+expected+" to "+transition+" it, but was "+currentState+".";
      throw new ProcessEngineException(message);
    }
  }

  protected void ensureNotCaseInstance(CmmnActivityExecution execution, String transition) {
    if (execution.isCaseInstanceExecution()) {
      String id = execution.getId();
      String message = "It is not possible to "+transition+" the case instance '"+id+"'.";
      throw new ProcessEngineException(message);
    }
  }

}
