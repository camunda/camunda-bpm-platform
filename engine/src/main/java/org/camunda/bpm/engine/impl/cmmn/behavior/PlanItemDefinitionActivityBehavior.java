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

import static org.camunda.bpm.engine.delegate.CaseExecutionListener.ENABLE;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.OCCUR;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.START;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.NEW;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REPETITION_RULE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REQUIRED_RULE;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.helper.CmmnProperties;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.pvm.PvmException;


/**
 * @author Roman Smirnov
 *
 */
public abstract class PlanItemDefinitionActivityBehavior implements CmmnActivityBehavior {

  protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;

  public void execute(CmmnActivityExecution execution) throws Exception {
    // nothing to do!
  }

  // sentries //////////////////////////////////////////////////////////////////////////////

  protected boolean isAtLeastOneEntryCriterionSatisfied(CmmnActivityExecution execution) {
    if (execution.isEntryCriterionSatisfied()) {
      return true;
    }

    CmmnActivity activity = getActivity(execution);
    List<CmmnSentryDeclaration> criteria = activity.getEntryCriteria();
    return !(criteria != null && !criteria.isEmpty());
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

  protected boolean evaluateRepetitionRule(CmmnActivityExecution execution) {
    CmmnActivity activity = execution.getActivity();

    Object repetitionRule = activity.getProperty(PROPERTY_REPETITION_RULE);
    if (repetitionRule != null) {
      CaseControlRule rule = (CaseControlRule) repetitionRule;
      return rule.evaluate(execution);
    }

    return false;
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
        throw LOG.alreadyClosedCaseException("close", id);
      }

      if (execution.isActive()) {
        throw LOG.wrongCaseStateException("close", id, "[completed|terminated|suspended]", "active");
      }

    } else {
      throw LOG.notACaseInstanceException("close", id);
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

  // repetition ///////////////////////////////////////////////////////////////

  public void repeat(CmmnActivityExecution execution, String standardEvent) {
    CmmnActivity activity = execution.getActivity();
    boolean repeat = false;

    if (activity.getEntryCriteria().isEmpty()) {
      List<String> events = activity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
      if (events != null && events.contains(standardEvent)) {
        repeat = evaluateRepetitionRule(execution);
      }
    }
    else {

      if (ENABLE.equals(standardEvent) || START.equals(standardEvent) || OCCUR.equals(standardEvent)) {
        repeat = evaluateRepetitionRule(execution);
      }
    }

    if (repeat) {

      CmmnActivityExecution parent = execution.getParent();

      // instantiate a new instance of given activity
      List<CmmnExecution> children = parent.createChildExecutions(Arrays.asList(activity));
      // start the lifecycle of the new instance
      parent.triggerChildExecutionsLifecycle(children);
    }

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
      throw LOG.isAlreadyInStateException(transition, id, target);

    } else
    // is the case execution in the expected state
    if (!expected.equals(currentState)) {
      throw LOG.unexpectedStateException(transition, id, expected, currentState);
    }
  }

  protected void ensureNotCaseInstance(CmmnActivityExecution execution, String transition) {
    if (execution.isCaseInstanceExecution()) {
      String id = execution.getId();
      throw LOG.impossibleTransitionException(transition, id);
    }
  }

  protected CmmnActivity getActivity(CmmnActivityExecution execution) {
    String id = execution.getId();
    CmmnActivity activity = execution.getActivity();
    ensureNotNull(PvmException.class, "Case execution '"+id+"': has no current activity.", "activity", activity);

    return activity;
  }

}
