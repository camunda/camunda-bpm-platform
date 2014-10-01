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
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.util.EnsureUtil;


/**
 * @author Roman Smirnov
 *
 */
public abstract class PlanItemDefinitionActivityBehavior implements CmmnActivityBehavior {

  public void execute(CmmnActivityExecution execution) throws Exception {
    // nothing to do!
  }

  // sentries //////////////////////////////////////////////////////////////////////////////

  protected boolean isAtLeastOneEntryCriteriaSatisfied(CmmnActivityExecution execution) {
    CmmnActivity activity = getActivity(execution);

    List<CmmnSentryDeclaration> entryCriteria = activity.getEntryCriteria();

    if (entryCriteria != null && !entryCriteria.isEmpty()) {
      return isSentrySatisified(entryCriteria, execution);

    } else {
      // missing entry criteria (Sentry) is considered true.
      return true;
    }
  }

  protected boolean isAtLeastOneExitCriteriaSatisfied(CmmnActivityExecution execution) {
    CmmnActivity activity = getActivity(execution);

    List<CmmnSentryDeclaration> exitCriteria = activity.getExitCriteria();

    if (exitCriteria != null && !exitCriteria.isEmpty()) {
      return isSentrySatisified(exitCriteria, execution);

    } else {
      return false;
    }
  }

  protected boolean isSentrySatisified(List<CmmnSentryDeclaration> sentryDeclarations, CmmnActivityExecution execution) {
    String id = execution.getId();
    CmmnActivityExecution parent = execution.getParent();
    EnsureUtil.ensureNotNull(PvmException.class, "Case execution '"+id+"': has no parent.", "parent", parent);

    for (CmmnSentryDeclaration sentryDeclaration : sentryDeclarations) {

      String sentryId = sentryDeclaration.getId();
      if (parent.isSentrySatisfied(sentryId)) {
        return true;
      }

    }

    return false;
  }

  // rules (required and repetition rule) /////////////////////////////////////////

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

  // creation ///////////////////////////////////////////////////////////////

  public void onCreate(CmmnActivityExecution execution) {
    ensureTransitionAllowed(execution, NEW, AVAILABLE, "create");
    creating(execution);
  }


  protected void creating(CmmnActivityExecution execution) {
    // noop
  }

  // start /////////////////////////////////////////////////////////////////

  public void started(CmmnActivityExecution execution) {
    // noop
  }

  // completion //////////////////////////////////////////////////////////////

  protected void completing(CmmnActivityExecution execution) {
    // noop
  }

  protected void manualCompleting(CmmnActivityExecution execution) {
    // noop
  }

  // close ///////////////////////////////////////////////////////////////////

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

  // termination ////////////////////////////////////////////////////////////

  protected void performTerminate(CmmnActivityExecution execution) {
    execution.performTerminate();
  }

  protected void performParentTerminate(CmmnActivityExecution execution) {
    execution.performParentTerminate();
  }

  protected void performExit(CmmnActivityExecution execution) {
    execution.performExit();
  }

  // suspension ///////////////////////////////////////////////////////////////

  protected void performSuspension(CmmnActivityExecution execution) {
    execution.performSuspension();
  }

  protected void performParentSuspension(CmmnActivityExecution execution) {
    execution.performParentSuspension();
  }

  // resume /////////////////////////////////////////////////////////////////

  protected void resuming(CmmnActivityExecution execution) {
    // noop
  }

  public void resumed(CmmnActivityExecution execution) {
    if (execution.isAvailable()) {
      // trigger created() to check whether an exit- or
      // entryCriteria has been satisfied in the meantime.
      created(execution);
    }
  }

  // re-activation ///////////////////////////////////////////////////////////

  public void reactivated(CmmnActivityExecution execution) {
    // noop
  }

  // helper //////////////////////////////////////////////////////////////////////

  protected void ensureTransitionAllowed(CmmnActivityExecution execution, CaseExecutionState expected, CaseExecutionState target, String transition) {
    String id = execution.getId();

    CaseExecutionState currentState = execution.getCurrentState();

    // the state "suspending" or "terminating" will set immediately
    // inside the corresponding AtomicOperation, that's why the
    // previous state will be used to ensure that the transition
    // is allowed.
    if (execution.isTerminating() || execution.isSuspending()) {
      currentState = execution.getPreviousState();
    }

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

  protected CmmnActivity getActivity(CmmnActivityExecution execution) {
    String id = execution.getId();
    CmmnActivity activity = execution.getActivity();
    ensureNotNull(PvmException.class, "Case execution '"+id+"': has no current activity.", "activity", activity);

    return activity;
  }

}
