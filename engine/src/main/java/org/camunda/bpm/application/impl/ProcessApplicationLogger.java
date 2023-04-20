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
package org.camunda.bpm.application.impl;

import java.util.Set;
import javax.naming.NamingException;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationLogger extends ProcessEngineLogger {

  public void taskNotRelatedToExecution(DelegateTask delegateTask) {
    logDebug(
        "001",
        "Task {} not related to an execution, target process application cannot be determined.",
        delegateTask);
  }

  public ProcessEngineException exceptionWhileNotifyingPaTaskListener(Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "002",
        "Exception while notifying process application task listener: " + e.getMessage()), e);
  }

  public void noTargetProcessApplicationForExecution(DelegateExecution execution) {
    logDebug(
        "003",
        "No target process application found for execution {}",
        execution);
  }

  public void paDoesNotProvideExecutionListener(String paName) {
    logDebug(
        "004",
        "Target process application '{}' does not provide an ExecutionListener.",
        paName);
  }

  public void cannotInvokeListenerPaUnavailable(String paName, ProcessApplicationUnavailableException e) {
    logDebug("005",
        "Exception while invoking listener: target process application '{}' unavailable", paName, e);
  }

  public void paDoesNotProvideTaskListener(String paName) {
    logDebug(
        "006",
        "Target process application '{}' does not provide a TaskListener.",
        paName);
  }

  public void paElResolversDiscovered(String summary) {
    logDebug("007", summary);
  }

  public void noElResolverProvided(String paName, String string) {
    logWarn("008",
        "Process Application '{}': No ELResolver provided by ProcessApplicationElResolver {}",
        paName,
        string);

  }

  public ProcessApplicationExecutionException processApplicationExecutionException(Exception e) {
    return new ProcessApplicationExecutionException(e);
  }

  public ProcessEngineException ejbPaCannotLookupSelfReference(NamingException e) {
    return new ProcessEngineException(exceptionMessage(
        "009",
        "Cannot lookup self reference to EjbProcessApplication"), e);
  }

  public ProcessEngineException ejbPaCannotAutodetectName(NamingException e) {
    return new ProcessEngineException(exceptionMessage(
        "010",
        "Could not autodetect EjbProcessApplicationName"), e);
  }

  public ProcessApplicationUnavailableException processApplicationUnavailableException(String name, Throwable cause) {
    return new ProcessApplicationUnavailableException(exceptionMessage(
        "011",
        "Process Application '{}' unavailable", name), cause);
  }

  public ProcessApplicationUnavailableException processApplicationUnavailableException(String name) {
    return new ProcessApplicationUnavailableException(exceptionMessage(
        "011",
        "Process Application '{}' unavailable", name));
  }

  public void servletDeployerNoPaFound(String ctxName) {
    logDebug("012",
        "Listener invoked for context '{}' but no process application annotation detected.", ctxName);
  }

  public String multiplePasException(Set<Class<?>> c, String appId) {

    StringBuilder builder = new StringBuilder();
    builder.append("An application must not contain more than one class annotated with @ProcessApplication.\n Application '");
    builder.append(appId);
    builder.append("' contains the following @ProcessApplication classes:\n");
    for (Class<?> clazz : c) {
      builder.append("  ");
      builder.append(clazz.getName());
      builder.append("\n");
    }
    String msg = builder.toString();

    return exceptionMessage("013", msg);
  }

  public String paWrongTypeException(Class<?> paClass) {
    return exceptionMessage(
        "014",
        "Class '{}' is annotated with @{} but is not a subclass of {}",
        paClass, ProcessApplication.class.getName(), AbstractProcessApplication.class.getName());
  }

  public void detectedPa(Class<?> paClass) {
    logInfo(
        "015",
        "Detected @ProcessApplication class '{}'",
        paClass.getName());
  }

  public void alreadyDeployed() {
    logWarn(
        "016",
        "Ignoring call of deploy() on process application that is already deployed.");
  }

  public void notDeployed() {
    logWarn(
        "017",
        "Calling undeploy() on process application that is not deployed.");
  }

  public void couldNotRemoveDefinitionsFromCache(Throwable t) {
    logError(
        "018",
        "Unregistering process application for deployment but could not remove process definitions from deployment cache.", t);
  }

  public ProcessEngineException exceptionWhileRegisteringDeploymentsWithJobExecutor(Exception e) {
    return new ProcessEngineException(exceptionMessage(
        "019",
        "Exception while registering deployment with job executor"), e);
  }

  public void exceptionWhileUnregisteringDeploymentsWithJobExecutor(Exception e) {
    logError(
        "020",
        "Exceptions while unregistering deployments with job executor", e);

  }

  public void registrationSummary(String string) {
    logInfo(
        "021",
        string);
  }

  public void exceptionWhileLoggingRegistrationSummary(Throwable e) {
    logError(
        "022",
        "Exception while logging registration summary",
        e);
  }

  public boolean isContextSwitchLoggable() {
    return isDebugEnabled();
  }

  public void debugNoTargetProcessApplicationFound(ExecutionEntity execution, ProcessApplicationManager processApplicationManager) {
    logDebug("023",
        "No target process application found for Execution[{}], ProcessDefinition[{}], Deployment[{}] Registrations[{}]",
            execution.getId(),
            execution.getProcessDefinitionId(),
            execution.getProcessDefinition().getDeploymentId(),
            processApplicationManager.getRegistrationSummary());
  }

  public void debugNoTargetProcessApplicationFoundForCaseExecution(CaseExecutionEntity execution, ProcessApplicationManager processApplicationManager) {
    logDebug("024",
        "No target process application found for CaseExecution[{}], CaseDefinition[{}], Deployment[{}] Registrations[{}]",
            execution.getId(),
            execution.getCaseDefinitionId(),
            ((CaseDefinitionEntity)execution.getCaseDefinition()).getDeploymentId(),
            processApplicationManager.getRegistrationSummary());
  }
}
