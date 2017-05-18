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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Stefan Hentschel.
 */
public class BpmnBehaviorLogger extends ProcessEngineLogger {


  public void missingBoundaryCatchEvent(String executionId, String errorCode) {
    logInfo(
      "001",
      "Execution with id '{}' throws an error event with errorCode '{}', but no catching boundary event was defined. " +
        "Execution is ended (none end event semantics).",
      executionId,
      errorCode
    );
  }

  public void leavingActivity(String activityId) {
    logDebug("002", "Leaving activity '{}'.", activityId);
  }

  public void missingOutgoingSequenceFlow(String activityId) {
    logDebug("003", "No outgoing sequence flow found for activity '{}'. Ending execution.", activityId);
  }

  public ProcessEngineException stuckExecutionException(String activityId) {
    return new ProcessEngineException(exceptionMessage(
      "004",
      "No outgoing sequence flow for the element with id '{}' could be selected for continuing the process.",
      activityId
    ));
  }

  public ProcessEngineException missingDefaultFlowException(String activityId, String defaultSequenceFlow) {
    return new ProcessEngineException(
      exceptionMessage("005", "Default sequence flow '{}' for element with id '{}' could not be not found.",
        defaultSequenceFlow, activityId)
    );
  }

  public ProcessEngineException missingConditionalFlowException(String activityId) {
    return new ProcessEngineException(exceptionMessage(
      "006",
      "No conditional sequence flow leaving the Flow Node '{}' could be selected for continuing the process.",
      activityId
    ));
  }

  public ProcessEngineException incorrectlyUsedSignalException(String className) {
    return new ProcessEngineException(exceptionMessage("007", "signal() can only be called on a '{}' instance.", className));
  }

  public ProcessEngineException missingDelegateParentClassException(String className,
      String javaDelegate, String activityBehavior) {

    return new ProcessEngineException(
      exceptionMessage("008", "Class '{}' doesn't implement '{}' nor '{}'.", className, javaDelegate, activityBehavior));
  }

  public void outgoingSequenceFlowSelected(String sequenceFlowId) {
    logDebug("009", "Sequence flow with id '{}' was selected as outgoing sequence flow.", sequenceFlowId);
  }

  public ProcessEngineException unsupportedSignalException(String activityId) {
    return new ProcessEngineException(exceptionMessage("010", "The activity with id '{}' doesn't accept signals.", activityId));
  }

  public void activityActivation(String activityId) {
    logDebug("011", "Element with id '{}' activates.", activityId);
  }

  public void noActivityActivation(String activityId) {
    logDebug("012", "Element with id '{}' does not activate.", activityId);
  }

  public void ignoringEventSubscription(EventSubscriptionEntity eventSubscription, String processDefinitionId) {
    logDebug(
      "014",
      "Found event subscription '{}' but process definition with id '{}' could not be found.",
      eventSubscription.toString(),
      processDefinitionId
    );
  }

  public ProcessEngineException sendingEmailException(String recipient, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("015", "Unable to send email to recipient '{}'.", recipient), cause);
  }

  public ProcessEngineException emailFormatException() {
    return new ProcessEngineException(
      exceptionMessage("016", "'html' or 'text' is required to be defined as mail format when using the mail activity.")
    );
  }

  public ProcessEngineException emailCreationException(String format, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("017", "Unable to create a mail with format '{}'.", format), cause);
  }

  public ProcessEngineException addRecipientException(String recipient, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("018", "Unable to add '{}' as recipient.", recipient), cause);
  }

  public ProcessEngineException missingRecipientsException() {
    return new ProcessEngineException(exceptionMessage("019", "No recipient could be found for sending email."));
  }

  public ProcessEngineException addSenderException(String sender, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("020", "Could not set '{}' as from address in email.", sender), cause);
  }

  public ProcessEngineException addCcException(String cc, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("021", "Could not add '{}' as cc recipient.", cc), cause);
  }

  public ProcessEngineException addBccException(String bcc, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("022", "Could not add '{}' as bcc recipient.", bcc), cause);
  }

  public ProcessEngineException invalidAmountException(String type, int amount) {
    return new ProcessEngineException(exceptionMessage(
      "023",
      "Invalid number of '{}': must be positive integer value or zero, but was '{}'.",
      type,
      amount
    ));
  }

  public ProcessEngineException unresolvableExpressionException(String expression, String type) {
    return new ProcessEngineException(
      exceptionMessage("024", "Expression '{}' didn't resolve to type '{}'.", expression, type)
    );
  }

  public ProcessEngineException invalidVariableTypeException(String variable, String type) {
    return new ProcessEngineException(exceptionMessage("025", "Variable '{}' is not from type '{}'.", variable, type));
  }

  public ProcessEngineException resolveCollectionExpressionOrVariableReferenceException() {
    return new ProcessEngineException(exceptionMessage("026", "Couldn't resolve collection expression nor variable reference"));
  }

  public ProcessEngineException expressionNotANumberException(String type, String expression) {
    return new ProcessEngineException(exceptionMessage(
      "027",
      "Could not resolve expression from type '{}'. Expression '{}' needs to be a number or number String.",
      type,
      expression
    ));
  }

  public ProcessEngineException expressionNotBooleanException(String type, String expression) {
    return new ProcessEngineException(exceptionMessage(
      "028",
      "Could not resolve expression from type '{}'. Expression '{}' needs to evaluate to a boolean value.",
      type,
      expression
    ));
  }

  public void multiInstanceCompletionConditionState(Boolean state) {
    logDebug("029", "Completion condition of multi-instance satisfied: '{}'", state);
  }

  public void activityActivation(String activityId, int joinedExecutions, int availableExecution) {
    logDebug(
      "030",
      "Element with id '{}' activates. Joined '{}' of '{}' available executions.",
      activityId,
      joinedExecutions,
      availableExecution
    );
  }

  public void noActivityActivation(String activityId, int joinedExecutions, int availableExecution) {
    logDebug(
      "031",
      "Element with id '{}' does not activate. Joined '{}' of '{}' available executions.",
      activityId,
      joinedExecutions,
      availableExecution
      );
  }

  public ProcessEngineException unsupportedConcurrencyException(String scopeExecutionId, String className) {
    return new ProcessEngineException(exceptionMessage(
      "032",
      "Execution '{}' with execution behavior of class '{}' cannot have concurrency.",
      scopeExecutionId,
      className
    ));
  }

  public ProcessEngineException resolveDelegateExpressionException(Expression expression, Class<?> parentClass, Class<JavaDelegate> javaDelegateClass) {
    return new ProcessEngineException(exceptionMessage(
      "033",
      "Delegate Expression '{}' did neither resolve to an implementation of '{}' nor '{}'.",
      expression,
      parentClass,
      javaDelegateClass
    ));
  }

  public ProcessEngineException shellExecutionException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("034", "Could not execute shell command."), cause);
  }

  public void errorPropagationException(String activityId, Throwable cause) {
    logError("035", "caught an exception while propagate error in activity with id '{}'", activityId, cause);
  }

  public void debugConcurrentScopeIsPruned(PvmExecutionImpl execution) {
    logDebug(
        "036", "Concurrent scope is pruned {}", execution);
  }

  public void debugCancelConcurrentScopeExecution(PvmExecutionImpl execution) {
    logDebug(
        "037", "Cancel concurrent scope execution {}", execution);
  }

  public void destroyConcurrentScopeExecution(PvmExecutionImpl execution) {
    logDebug(
        "038", "Destroy concurrent scope execution", execution);
  }

  public void completeNonScopeEventSubprocess() {
    logDebug(
        "039", "Destroy non-socpe event subprocess");
  }

  public void endConcurrentExecutionInEventSubprocess() {
    logDebug(
        "040", "End concurrent execution in event subprocess");
  }

  public ProcessEngineException missingDelegateVariableMappingParentClassException(String className, String delegateVarMapping) {
    return new ProcessEngineException(
      exceptionMessage("041", "Class '{}' doesn't implement '{}'.", className, delegateVarMapping));
  }

  public ProcessEngineException missingBoundaryCatchEventError(String executionId, String errorCode) {
    return new ProcessEngineException(
      exceptionMessage(
        "042",
        "Execution with id '{}' throws an error event with errorCode '{}', but no error handler was defined. ",
        executionId,
        errorCode));
  }

}
