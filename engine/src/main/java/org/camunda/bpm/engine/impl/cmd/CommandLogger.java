/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import java.util.Arrays;

import org.camunda.bpm.application.impl.ProcessApplicationIdentifier;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
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

  public void couldNotFindProcessDefinitionForEventSubscription(EventSubscriptionEntity messageEventSubscription, String processDefinitionId) {
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

  public BadUserRequestException exceptionUpdateSuspensionStateForTenantOnlyByProcessDefinitionKey() {
    return new BadUserRequestException(exceptionMessage(
        "032", "Can only specify a tenant-id when update the suspension state which is referenced by process definition key."));
  }

  public ProcessEngineException exceptionBpmnErrorPropagationFailed(String errorCode, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
        "033",
        "Propagation of bpmn error {} failed. ",
        errorCode), cause);
  }

  public ProcessEngineException exceptionCommandWithUnauthorizedTenant(String command) {
    return new ProcessEngineException(exceptionMessage(
        "034",
        "Cannot {} because it belongs to no authenticated tenant.",
        command
        ));
  }

  public void warnDeploymentResourceHasWrongName(String resourceName, String[] suffixes) {
    logWarn(
        "035",
        String.format("Deployment resource '%s' will be ignored as its name must have one of suffixes %s.",
            resourceName,
            Arrays.toString(suffixes)
            ));

  }

  public ProcessEngineException processInstanceDoesNotExist(String processInstanceId) {
    return new ProcessEngineException(exceptionMessage(
        "036",
        "Process instance '{}' cannot be modified. The process instance does not exist",
        processInstanceId));
  }

  public ProcessEngineException processDefinitionOfInstanceDoesNotMatchModification(ExecutionEntity processInstance, String processDefinitionId) {
    return new ProcessEngineException(exceptionMessage(
      "037",
      "Process instance '{}' cannot be modified. Its process definition '{}' does not match given process definition '{}'",
      processInstance.getId(),
      processInstance.getProcessDefinitionId(),
      processDefinitionId
    ));
  }

  public void debugHistoryCleanupWrongConfiguration() {
    logDebug("038", "History cleanup won't be scheduled. Either configure batch window or call it with immediatelyDue = true.");
  }

  public ProcessEngineException processDefinitionOfHistoricInstanceDoesNotMatchTheGivenOne(HistoricProcessInstance historicProcessInstance, String processDefinitionId) {
    return new ProcessEngineException(exceptionMessage(
      "039",
      "Historic process instance '{}' cannot be restarted. Its process definition '{}' does not match given process definition '{}'",
      historicProcessInstance.getId(),
      historicProcessInstance.getProcessDefinitionId(),
      processDefinitionId
    ));
  }

  public ProcessEngineException historicProcessInstanceActive(HistoricProcessInstance historicProcessInstance) {
    return new ProcessEngineException(exceptionMessage(
      "040",
      "Historic process instance '{}' cannot be restarted. It is not completed or terminated.",
      historicProcessInstance.getId(),
      historicProcessInstance.getProcessDefinitionId()
    ));
  }

  public ProcessEngineException exceptionWhenStartFormScriptEvaluation(String processDefinitionId, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
        "041",
        "Unable to evaluate script when rendering start form of the process definition '{}'.",
        processDefinitionId));
  }

  public ProcessEngineException exceptionWhenEvaluatingConditionalStartEventByProcessDefinition(String processDefinitionId) {
    return new ProcessEngineException(exceptionMessage(
      "042",
      "Process definition with id '{}' does not declare conditional start event.",
      processDefinitionId));
  }

  public ProcessEngineException exceptionWhenEvaluatingConditionalStartEvent() {
    return new ProcessEngineException(exceptionMessage(
      "043",
      "No subscriptions were found during evaluation of the conditional start events."));
  }

  public BadUserRequestException exceptionSettingTransientVariablesAsyncNotSupported(String variableName) {
    return new BadUserRequestException(exceptionMessage(
        "044",
        "Setting transient variable '{}' asynchronously is currently not supported.",
        variableName));
  }

  public void crdbTransactionRetryAttempt(Throwable cause) {
    logDebug("045",
      "A CockroachDB transaction retry attempt will be made. Reason: {}",
      cause.getMessage());
  }

  public void debugNotAllowedToResolveCalledProcess(String calledProcessId, String callingProcessId, String callActivityId, Throwable cause) {
    logDebug("046",
      "Resolving a called process definition {} for {} in {} was not possible. Reason: {}",
      calledProcessId,
      callActivityId,
      callingProcessId,
      cause.getMessage());
  }

  public void warnFilteringDuplicatesEnabledWithNullDeploymentName() {
    logWarn("047", "Deployment name set to null. Filtering duplicates will not work properly.");
  }

  public void warnReservedErrorCode(int initialCode) {
    logWarn("048", "With error code {} you are using a reserved error code. Falling back to default error code 0. "
        + "If you want to override built-in error codes, please disable the built-in error code provider.", initialCode);
  }

  public void warnResetToBuiltinCode(Integer builtinCode, int initialCode) {
    logWarn("049", "You are trying to override the built-in code {} with {}. "
        + "Falling back to built-in code. If you want to override built-in error codes, "
        + "please disable the built-in error code provider.", builtinCode, initialCode);
  }

  public ProcessEngineException exceptionSettingJobRetriesAsyncNoJobsSpecified() {
    return new ProcessEngineException(exceptionMessage(
        "050",
        "You must specify at least one of jobIds or jobQuery."));
  }

  public ProcessEngineException exceptionSettingJobRetriesAsyncNoProcessesSpecified() {
    return new ProcessEngineException(exceptionMessage(
        "051",
        "You must specify at least one of or one of processInstanceIds, processInstanceQuery, or historicProcessInstanceQuery."));
  }

  public ProcessEngineException exceptionSettingJobRetriesJobsNotSpecifiedCorrectly() {
    return new ProcessEngineException(exceptionMessage(
        "052",
        "You must specify exactly one of jobId, jobIds or jobDefinitionId as parameter. The parameter can not be null."));
  }

  public ProcessEngineException exceptionNoJobFoundForId(String jobId) {
    return new ProcessEngineException(exceptionMessage(
        "053",
        "No job found with id '{}'.'", jobId));
    }

  public ProcessEngineException exceptionJobRetriesMustNotBeNegative(Integer retries) {
    return new ProcessEngineException(exceptionMessage(
        "054",
        "The number of job retries must be a non-negative Integer, but '{}' has been provided.", retries));
  }

}
