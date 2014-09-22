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
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REPETITION_RULE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REQUIRED_RULE;

import java.util.List;

import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.pvm.PvmException;


/**
 * @author Roman Smirnov
 *
 */
public abstract class PlanItemDefinitionActivityBehavior implements CmmnActivityBehavior {

  public void execute(CmmnActivityExecution execution) throws Exception {
    // nothing to do!
  }

  protected boolean isEntryCriteriaSatisfied(CmmnActivityExecution execution) {
    String id = execution.getId();
    CmmnActivity activity = execution.getActivity();

    if (activity != null) {

      List<CmmnSentryDeclaration> entryCriteria = activity.getEntryCriteria();

      if (entryCriteria != null && !entryCriteria.isEmpty()) {
        CmmnActivityExecution parent = execution.getParent();

        if (parent != null) {
          for (CmmnSentryDeclaration sentryDeclaration : entryCriteria) {

            String sentryId = sentryDeclaration.getId();
            if (parent.isSentrySatisfied(sentryId)) {
              return true;
            }

          }

          return false;

        } else {
          throw new PvmException("Case execution '"+id+"': has no parent.");
        }
      } else {
        // missing entry criteria (Sentry) is considered true.
        return true;
      }

    } else {
      throw new PvmException("Case execution '"+id+"': has no current activity.");

    }

  }

  protected boolean isExitCriteriaSatisfied(CmmnActivityExecution execution) {
    String id = execution.getId();
    CmmnActivity activity = execution.getActivity();

    if (activity != null) {

      List<CmmnSentryDeclaration> exitCriteria = activity.getExitCriteria();

      if (exitCriteria != null && !exitCriteria.isEmpty()) {
        CmmnActivityExecution parent = execution.getParent();

        if (parent != null) {
          for (CmmnSentryDeclaration sentryDeclaration : exitCriteria) {

            String sentryId = sentryDeclaration.getId();
            if (parent.isSentrySatisfied(sentryId)) {
              return true;
            }

          }

          return false;

        } else {
          throw new PvmException("Case execution '"+id+"': has no parent.");
        }
      } else {
        // missing entry criteria (Sentry) is considered true.
        return false;
      }

    } else {
      throw new PvmException("Case execution '"+id+"': has no current activity.");

    }

  }

  public void created(CmmnActivityExecution execution) {
    if (isExitCriteriaSatisfied(execution)) {
      triggerExitCriteria(execution);
      return;
    }

    if (isEntryCriteriaSatisfied(execution)) {
      triggerEntryCriteria(execution);
    }
  }

  public void triggerExitCriteria(CmmnActivityExecution execution) {
    execution.exit();
  }

  public void onCreate(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, NEW, AVAILABLE, "create");
    creating(execution);
  }

  public void onClose(CmmnActivityExecution execution) {
    String id = execution.getId();
    if (execution.isCaseInstanceExecution()) {

      if (execution.isClosed()) {
        String message = "Case instance'"+id+"' is already closed.";
        throw createIllegalStateTransitionException("close", message, execution);
      }

      if (execution.isActive()) {
        String message = "Case instance '"+id+"' must be {completed|terminated|suspended} to close it, but was 'active'.";
        throw createIllegalStateTransitionException("close", message, execution);
      }

    } else {
      String message = "It is not possible to close case execution '"+id+"' which is not a case instance.";
      throw createIllegalStateTransitionException("close", message, execution);
    }
  }

  protected void creating(CmmnActivityExecution execution) {
    // noop
  }

  protected void terminating(CmmnActivityExecution execution) {
    // noop
  }

  protected void completing(CmmnActivityExecution execution) {
    // noop
  }

  protected void manualCompleting(CmmnActivityExecution execution) {
    // noop
  }

  protected void suspending(CmmnActivityExecution execution) {
    // noop
  }

  protected void resuming(CmmnActivityExecution execution) {
    // noop
  }

  public void resumed(CmmnActivityExecution execution) {
    // noop
  }

  public void reactivated(CmmnActivityExecution execution) {
    // noop
  }

  public void started(CmmnActivityExecution execution) {
    // noop
  }

  protected void evaluateRequiredRule(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    Object requiredRule = activity.getProperty(PROPERTY_REQUIRED_RULE);
    if (requiredRule != null) {
      CaseControlRule rule = (CaseControlRule) requiredRule;
      boolean required = rule.evaluate(execution);
      execution.setRequired(required);
    }
  }

  protected void evaluateRepetitionRule(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    Object repetitionRule = activity.getProperty(PROPERTY_REPETITION_RULE);
    if (repetitionRule != null) {
      CaseControlRule rule = (CaseControlRule) repetitionRule;
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
      throw createIllegalStateTransitionException(transition, message, execution);
    } else
    // is the case execution in the expected state
    if (!expected.equals(currentState)) {
      String message = "Case execution '"+id+"' must be "+expected+" to "+transition+" it, but was "+currentState+".";
      throw createIllegalStateTransitionException(transition, message, execution);

    }
  }

  protected void ensureNotCaseInstance(CmmnActivityExecution execution, String transition) {
    if (execution.isCaseInstanceExecution()) {
      String id = execution.getId();
      String message = "It is not possible to "+transition+" case instance '"+id+"'.";
      throw createIllegalStateTransitionException(transition, message, execution);
    }
  }

  protected CaseIllegalStateTransitionException createIllegalStateTransitionException(String transition, String message, CmmnActivityExecution execution) {
    String id = execution.getId();
    String errorMessage = String.format("Could not perform transition '%s' on case execution '%s': %s", transition, id, message);
    return new CaseIllegalStateTransitionException(errorMessage);
  }

}
