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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
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

}
