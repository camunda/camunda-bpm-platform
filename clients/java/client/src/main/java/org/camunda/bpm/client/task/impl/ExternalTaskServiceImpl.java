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
package org.camunda.bpm.client.task.impl;

import java.util.Map;

import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.listener.ExternalTaskClientListener;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskServiceImpl implements ExternalTaskService {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected EngineClient engineClient;

  protected ExternalTaskClientListener externalTaskClientListener;

  public ExternalTaskServiceImpl(EngineClient engineClient) {
    this.engineClient = engineClient;
  }

  @Override
  public void lock(ExternalTask externalTask, long lockDuration) {
    lock(externalTask.getId(), lockDuration);
  }

  @Override
  public void lock(String externalTaskId, long lockDuration) {
    try {
      engineClient.lock(externalTaskId, lockDuration);
    } catch (EngineClientException e) {
      throw LOG.externalTaskServiceException("locking task", e);
    }
  }

  @Override
  public void unlock(ExternalTask externalTask) {
    try {
      externalTaskClientListener.onUnlock(externalTask.getId());
      engineClient.unlock(externalTask.getId());
      externalTaskClientListener.unlockDone(externalTask.getId());
    } catch (EngineClientException e) {
      externalTaskClientListener.unlockFail(externalTask.getId(), e);
      throw LOG.externalTaskServiceException("unlocking the external task", e);
    }
  }

  @Override
  public void complete(ExternalTask externalTask) {
    complete(externalTask, null, null);
  }

  @Override
  public void setVariables(String processInstanceId, Map<String, Object> variables) {
    try {
      externalTaskClientListener.onSetVariable(processInstanceId, variables);
      engineClient.setVariables(processInstanceId, variables);
      externalTaskClientListener.setVariableDone(processInstanceId, variables);
    } catch (EngineClientException e) {
      externalTaskClientListener.setVariableFail(processInstanceId, variables, e);
      throw LOG.externalTaskServiceException("setting variables for external task", e);
    }
  }

  @Override
  public void setVariables(ExternalTask externalTask, Map<String, Object> variables) {
    String processId = externalTask.getProcessInstanceId();
    try {
      externalTaskClientListener.onSetVariable(processId, variables);
      engineClient.setVariables(processId, variables);
      externalTaskClientListener.setVariableDone(processId, variables);
    } catch (EngineClientException e) {
      externalTaskClientListener.setVariableFail(processId, variables, e);
      throw LOG.externalTaskServiceException("setting variables for external task", e);
    }
  }

  @Override
  public void complete(ExternalTask externalTask, Map<String, Object> variables) {
    complete(externalTask, variables, null);
  }

  @Override
  public void complete(ExternalTask externalTask, Map<String, Object> variables,  Map<String, Object> localVariables) {
    complete(externalTask.getId(), variables, localVariables);
  }

  public void complete(String externalTaskId, Map<String, Object> variables, Map<String, Object> localVariables) {
    try {
      externalTaskClientListener.onComplete(externalTaskId, variables, localVariables);
      engineClient.complete(externalTaskId, variables, localVariables);
      externalTaskClientListener.completeDone(externalTaskId, variables, localVariables);
    } catch (EngineClientException e) {
      externalTaskClientListener.completeFail(externalTaskId, variables, localVariables, e);
      throw LOG.externalTaskServiceException("completing the external task", e);
    }
  }



  @Override
  public void handleFailure(ExternalTask externalTask, String errorMessage, String errorDetails, int retries, long retryTimeout) {
    handleFailure(externalTask.getId(), errorMessage, errorDetails, retries, retryTimeout);
  }

  @Override
  public void handleFailure(String externalTaskId, String errorMessage, String errorDetails, int retries, long retryTimeout) {
    handleFailure(externalTaskId, errorMessage, errorDetails, retries, retryTimeout, null, null);
  }

  @Override
  public void handleFailure(String externalTaskId, String errorMessage, String errorDetails, int retries, long retryTimeout, Map<String, Object> variables, Map<String, Object> locaclVariables) {
    try {
      externalTaskClientListener.onFailure(externalTaskId, errorMessage, errorDetails, retries, retryTimeout);
      engineClient.failure(externalTaskId, errorMessage, errorDetails, retries, retryTimeout, variables, locaclVariables);
      externalTaskClientListener.failureDone(externalTaskId, errorMessage, errorDetails, retries, retryTimeout);
    } catch (EngineClientException e) {
      externalTaskClientListener.failureFail(externalTaskId, errorMessage, errorDetails, retries, retryTimeout, e);
      throw LOG.externalTaskServiceException("notifying a failure", e);
    }
  }

  @Override
  public void handleBpmnError(ExternalTask externalTask, String errorCode) {
    handleBpmnError(externalTask, errorCode, null, null);
  }

  @Override
  public void handleBpmnError(ExternalTask externalTask, String errorCode, String errorMessage) {
    handleBpmnError(externalTask, errorCode, errorMessage, null);
  }

  @Override
  public void handleBpmnError(ExternalTask externalTask, String errorCode, String errorMessage, Map<String, Object> variables) {
    handleBpmnError(externalTask.getId(), errorCode, errorMessage, variables);
  }

  @Override
  public void handleBpmnError(String externalTaskId, String errorCode, String errorMessage, Map<String, Object> variables) {
    try {
      externalTaskClientListener.onBpmnError(externalTaskId, errorCode, errorMessage, variables);
      engineClient.bpmnError(externalTaskId, errorCode, errorMessage, variables);
      externalTaskClientListener.bpmnErrorDone(externalTaskId, errorCode, errorMessage, variables);
    } catch (EngineClientException e) {
      externalTaskClientListener.bpmnErrorFail(externalTaskId, errorCode, errorMessage, variables, e);
      throw LOG.externalTaskServiceException("notifying a BPMN error", e);
    }
  }

  @Override
  public void extendLock(ExternalTask externalTask, long newDuration) {
    extendLock(externalTask.getId(), newDuration);
  }

  @Override
  public void extendLock(String externalTaskId, long newDuration) {
    try {
      externalTaskClientListener.onExtendLock(externalTaskId, newDuration);
      engineClient.extendLock(externalTaskId, newDuration);
      externalTaskClientListener.extendLockDone(externalTaskId, newDuration);
    } catch (EngineClientException e) {
      externalTaskClientListener.extendLockFail(externalTaskId, newDuration, e);
      throw LOG.externalTaskServiceException("extending lock", e);
    }
  }
}
