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
import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.pvm.PvmException;

/**
 * @author Stefan Hentschel.
 */
public class CmmnBehaviorLogger extends ProcessEngineLogger {

  protected final String caseStateTransitionMessage = "Could not perform transition '{} on case execution with id '{}'.";

  public ProcessEngineException ruleExpressionNotBooleanException(Object result) {
    return new ProcessEngineException(exceptionMessage(
      "001",
      "Rule expression returns a non-boolean value. Value: '{}', Class: '{}'",
      result,
      result.getClass().getName()
    ));
  }

  public CaseIllegalStateTransitionException forbiddenManualCompletitionException(String transition, String id,
      String type) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "002",
      caseStateTransitionMessage +
      "Reason: It is not possible to manually complete the case execution which is associated with an element of type {}.",
      transition,
      id,
      type
    ));
  }

  public CaseIllegalStateTransitionException criteriaNotAllowedException(String criteria, String id,
      String additionalMessage) {

    return new CaseIllegalStateTransitionException(exceptionMessage(
      "003",
      "Cannot trigger case execution with id '{}' because {} criteria is not allowed for {}.",
      id,
      criteria,
      additionalMessage
    ));
  }

  public CaseIllegalStateTransitionException criteriaNotAllowedForEventListenerOrMilestonesException(String criteria, String id) {
    return criteriaNotAllowedException(criteria, id, "event listener or milestones");
  }

  public CaseIllegalStateTransitionException criteriaNotAllowedForEventListenerException(String criteria, String id) {
    return criteriaNotAllowedException(criteria, id, "event listener");
  }

  public CaseIllegalStateTransitionException criteriaNotAllowedForCaseInstanceException(String criteria, String id) {
    return criteriaNotAllowedException(criteria, id, "case instances");
  }

  CaseIllegalStateTransitionException executionAlreadyCompletedException(String transition, String id) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "004",
      caseStateTransitionMessage +
      "Reason: Case execution must be available or suspended, but was completed.",
      transition,
      id
    ));

  }

  public CaseIllegalStateTransitionException resumeInactiveCaseException(String transition, String id) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "005",
      caseStateTransitionMessage +
      "Reason: It is not possible to resume the case execution which parent is not active.",
      transition,
      id
    ));
  }

  public CaseIllegalStateTransitionException illegalStateTransitionException(String transition, String id, String typeName) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "006",
      caseStateTransitionMessage +
      "Reason: It is not possible to {} the case execution which is associated with a {}",
      transition,
      id,
      transition,
      typeName
    ));
  }

  public CaseIllegalStateTransitionException alreadyStateCaseException(String transition, String id, String state) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "007",
      caseStateTransitionMessage +
        "Reason: The case instance is already {}.",
      transition,
      id,
      state
    ));
  }

  public CaseIllegalStateTransitionException alreadyClosedCaseException(String transition, String id) {
    return alreadyStateCaseException(transition, id, "closed");
  }

  public CaseIllegalStateTransitionException alreadyActiveException(String transition, String id) {
    return alreadyStateCaseException(transition, id, "active");
  }

  public CaseIllegalStateTransitionException alreadyTerminatedException(String transition, String id) {
    return alreadyStateCaseException(transition, id, "terminated");
  }

  public CaseIllegalStateTransitionException alreadySuspendedException(String transition, String id) {
    return alreadyStateCaseException(transition, id, "suspended");
  }

  public CaseIllegalStateTransitionException wrongCaseStateException(String transition, String id,
      String acceptedState, String currentState) {
    return wrongCaseStateException(transition, id, transition, acceptedState, currentState);
  }

  public CaseIllegalStateTransitionException wrongCaseStateException(String transition, String id, String altTransition,
      String acceptedState, String currentState) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "008",
      caseStateTransitionMessage +
        "Reason: The case instance must be in state '{}' to {} it, but the state is '{}'.",
      transition,
      id,
      acceptedState,
      transition,
      currentState
    ));
  }

  public CaseIllegalStateTransitionException notACaseInstanceException(String transition, String id) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "009",
      caseStateTransitionMessage +
      "Reason: It is not possible to close a case execution which is not a case instance.",
      transition,
      id
    ));
  }

  public CaseIllegalStateTransitionException isAlreadyInStateException(String transition, String id, CaseExecutionState state) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "010",
      caseStateTransitionMessage +
      "Reason: The case execution is already in state '{}'.",
      transition,
      id,
      state
    ));
  }

  public CaseIllegalStateTransitionException unexpectedStateException(String transition, String id,
      CaseExecutionState expectedState, CaseExecutionState currentState) {

    return new CaseIllegalStateTransitionException(exceptionMessage(
      "011",
      caseStateTransitionMessage +
      "Reason: The case execution must be in state '{}' to {}, but it was in state '{}'",
      transition,
      id,
      expectedState,
      transition,
      currentState
    ));
  }

  public CaseIllegalStateTransitionException impossibleTransitionException(String transition, String id) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "012",
      caseStateTransitionMessage +
      "Reason: The transition is not possible for this case instance.",
      transition,
      id
    ));
  }



  public CaseIllegalStateTransitionException remainingChildException(String transition, String id,
      String childId, CaseExecutionState childState) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "013",
      caseStateTransitionMessage +
      "Reason: There is a child case execution with id '{}' in state '{}'",
      transition,
      id,
      childId,
      childState
    ));
  }

  public CaseIllegalStateTransitionException wrongChildStateException(String transition, String id, String childId, String stateList) {
    return new CaseIllegalStateTransitionException(exceptionMessage(
      "014",
      caseStateTransitionMessage +
      "Reason: There is a child case execution with id '{}' which is in one of the following states: {}",
      transition,
      id,
      childId,
      stateList
    ));
  }

  public PvmException transitCaseException(String transition, String id, CaseExecutionState currentState) {
    return new PvmException(exceptionMessage(
      "015",
      caseStateTransitionMessage +
      "Reason: Expected case execution state to be {terminatingOnTermination|terminatingOnExit} but it was '{}'.",
      transition,
      id,
      currentState
    ));
  }

  public PvmException suspendCaseException(String id, CaseExecutionState currentState) {
    return transitCaseException("suspend", id, currentState);
  }

  public PvmException terminateCaseException(String id, CaseExecutionState currentState) {
    return transitCaseException("terminate", id, currentState);
  }

  public ProcessEngineException missingDelegateParentClassException(String className, String parentClass) {
    return new ProcessEngineException(
      exceptionMessage("016", "Class '{}' doesn't implement '{}'.", className, parentClass));
  }

  public UnsupportedOperationException unsupportedTransientOperationException(String className) {
    return new UnsupportedOperationException(
      exceptionMessage("017", "Class '{}' is not supported in transient CaseExecutionImpl", className));
  }

  public ProcessEngineException invokeVariableListenerException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "018",
      "Variable listener invocation failed. Reason: {}",
      cause.getMessage()),
      cause
    );
  }

  public ProcessEngineException decisionDefinitionEvaluationFailed(CmmnActivityExecution execution, Exception cause) {
    return new ProcessEngineException(exceptionMessage(
      "019",
      "Could not evaluate decision in case execution '"+execution.getId()+"'. Reason: {}",
      cause.getMessage()),
      cause
    );
  }

}
