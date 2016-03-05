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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.application.impl.ProcessApplicationIdentifier;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;

/**
 * @author Daniel Meyer
 *
 */
public class CommandLogger extends ProcessEngineLogger {


  public void debugCreatingNewDeployment() {
    logDebug(
        "001", "Creating new deployment");
  }

  public void usingExistingDeployment() {
    logDebug(
        "002", "Using existing deployment");
  }

  public void debugModificationInstruction(String processInstanceId, int i, String describe) {
    logDebug(
        "003", "Modifying process instance '{}': Instruction {}: {}",
        processInstanceId, i, describe);
  }

  public void debugStartingInstruction(String processInstanceId, int i, String describe) {
    logDebug(
        "004", "Starting process instance '{}': Instruction {}: {}",
        processInstanceId, i, describe);
  }

  public void debugStartingCommand(Command<?> cmd) {
    logDebug(
        "005",
        "Starting command -------------------- {} ----------------------", ClassNameUtil.getClassNameWithoutPackage(cmd));
  }

  public void debugFinishingCommand(Command<?> cmd) {
    logDebug(
        "006",
        "Finishing command -------------------- {} ----------------------", ClassNameUtil.getClassNameWithoutPackage(cmd));
  }

  public void debugWaitingFor(long waitTime) {
    logDebug(
        "007", "Waiting for {} before retrying command", waitTime);
  }

  public void debugCaughtOptimisticLockingException(OptimisticLockingException e) {
    logDebug(
        "008", "caught optimistic locking excpetion", e);
  }

  public void debugOpeningNewCommandContext() {
    logDebug(
        "009", "opening new command context");
  }

  public void debugReusingExistingCommandContext() {
    logDebug(
        "010", "reusing existing command context");
  }

  public void closingCommandContext() {
    logDebug(
         "011", "closing existing command context");
  }

  public void calledInsideTransaction() {
    logDebug(
        "012", "called inside transaction skipping");
  }

  public void maskedExceptionInCommandContext(Throwable throwable) {
    logDebug(
        "013", "masked exception in command context. for root cause, see below as it will be rethrown later.", throwable);
  }

  public void exceptionWhileRollingBackTransaction(Exception e) {
    logError(
        "014", "exception while rolling back transaction", e);
  }

  public void exceptionWhileGettingValueForVariable(Exception t) {
    logDebug(
        "015", "exception while getting value for variable {}", t.getMessage(), t);
  }

  public void couldNotFindProcessDefinitionForEventSubscription(MessageEventSubscriptionEntity messageEventSubscription, String processDefinitionId) {
    logDebug(
        "016",
        "Found event subscription with {} but process definition {} could not be found.", messageEventSubscription, processDefinitionId);
  }

  public void debugIgnoringEventSubscription(EventSubscriptionEntity eventSubscription, String processDefinitionId) {
    logDebug(
        "017", "Found event subscription with {} but process definition {} could not be found.", eventSubscription, processDefinitionId);
  }

  public void debugProcessingDeployment(String name) {
    logDebug(
        "018", "Processing deployment {}", name);
  }

  public void debugProcessingResource(String name) {
    logDebug(
        "019", "Processing resource {}", name);
  }

  public ProcessEngineException paWithNameNotRegistered(String name) {
    return new ProcessEngineException(exceptionMessage(
        "020", "A process application with name '{}' is not registered", name));
  }

  public ProcessEngineException cannotReolvePa(ProcessApplicationIdentifier processApplicationIdentifier) {
    return new ProcessEngineException(exceptionMessage(
        "021", "Cannot resolve process application based on {}", processApplicationIdentifier));
  }

  public void warnDisabledDeploymentLock() {
    logWarn(
        "022", "No exclusive lock is aquired while deploying because it is disabled. "
        + "This can lead to problems when multiple process engines use the same data source (i.e. in cluster mode).");
  }

  public BadUserRequestException exceptionStartProcessInstanceByIdAndTenantId() {
    return new BadUserRequestException(exceptionMessage(
        "023", "Cannot specify a tenant-id when start a process instance by process definition id."));
  }

  public BadUserRequestException exceptionStartProcessInstanceAtStartActivityAndSkipListenersOrMapping() {
    return new BadUserRequestException(exceptionMessage(
        "024", "Cannot skip custom listeners or input/output mappings when start a process instance at default start activity."));
  }

  public BadUserRequestException exceptionCorrelateMessageWithProcessDefinitionId() {
    return new BadUserRequestException(exceptionMessage(
        "025", "Cannot specify a process definition id when correlate a message, except for explicit correlation of a start message."));
  }

  public BadUserRequestException exceptionCorrelateStartMessageWithCorrelationVariables() {
    return new BadUserRequestException(exceptionMessage(
        "026", "Cannot specify correlation variables of a process instance when correlate a start message."));
  }

  public BadUserRequestException exceptionDeliverSignalToSingleExecutionWithTenantId() {
    return new BadUserRequestException(exceptionMessage(
        "027", "Cannot specify a tenant-id when deliver a signal to a single execution."));
  }

  public BadUserRequestException exceptionCorrelateMessageWithProcessInstanceAndTenantId() {
    return new BadUserRequestException(exceptionMessage(
        "028", "Cannot specify a tenant-id when correlate a message to a single process instance."));
  }

  public BadUserRequestException exceptionCorrelateMessageWithProcessDefinitionAndTenantId() {
    return new BadUserRequestException(exceptionMessage(
        "029", "Cannot specify a tenant-id when correlate a start message to a specific version of a process definition."));
  }

  public MismatchingMessageCorrelationException exceptionCorrelateMessageToSingleProcessDefinition(String messageName, long processDefinitionCound, CorrelationSet correlationSet) {
    return new MismatchingMessageCorrelationException(exceptionMessage(
        "030",
        "Cannot correlate a message with name '{}' to a single process definition. {} process definitions match the correlations keys: {}",
        messageName, processDefinitionCound, correlationSet
        ));
  }

  public MismatchingMessageCorrelationException exceptionCorrelateMessageToSingleExecution(String messageName, long executionCound, CorrelationSet correlationSet) {
    return new MismatchingMessageCorrelationException(exceptionMessage(
        "031",
        "Cannot correlate a message with name '{}' to a single execution. {} executions match the correlation keys: {}",
        messageName, executionCound, correlationSet
        ));
  }

}
