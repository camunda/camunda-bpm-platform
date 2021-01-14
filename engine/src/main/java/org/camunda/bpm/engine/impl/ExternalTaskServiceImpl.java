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
package org.camunda.bpm.engine.impl;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesSelectBuilder;
import org.camunda.bpm.engine.impl.cmd.*;
import org.camunda.bpm.engine.impl.externaltask.ExternalTaskQueryTopicBuilderImpl;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 * @author Askar Akhmerov
 */
public class ExternalTaskServiceImpl extends ServiceImpl implements ExternalTaskService {

  @Override
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId) {
    return fetchAndLock(maxTasks, workerId, false);
  }

  @Override
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId, boolean usePriority) {
    return new ExternalTaskQueryTopicBuilderImpl(commandExecutor, workerId, maxTasks, usePriority);
  }

  @Override
  public void lock(String externalTaskId, String workerId, long lockDuration) {
    commandExecutor.execute(new LockExternalTaskCmd(externalTaskId, workerId, lockDuration));
  }

  @Override
  public void complete(String externalTaskId, String workerId) {
    complete(externalTaskId, workerId, null, null);
  }

  @Override
  public void complete(String externalTaskId, String workerId, Map<String, Object> variables) {
    complete(externalTaskId, workerId, variables, null);
  }

  @Override
  public void complete(String externalTaskId, String workerId, Map<String, Object> variables, Map<String, Object> localVariables) {
    commandExecutor.execute(new CompleteExternalTaskCmd(externalTaskId, workerId, variables, localVariables));
  }

  @Override
  public void handleFailure(String externalTaskId, String workerId, String errorMessage, int retries, long retryDuration) {
    this.handleFailure(externalTaskId,workerId,errorMessage,null,retries,retryDuration);
  }

  @Override
  public void handleFailure(String externalTaskId, String workerId, String errorMessage, String errorDetails, int retries, long retryDuration) {
    this.handleFailure(externalTaskId, workerId, errorMessage, errorDetails, retries, retryDuration, null, null);
  }

  @Override
  public void handleFailure(String externalTaskId, String workerId, String errorMessage, String errorDetails, int retries, long retryDuration, Map<String, Object> variables, Map<String, Object> localVariables) {
    commandExecutor.execute(new HandleExternalTaskFailureCmd(externalTaskId, workerId, errorMessage, errorDetails, retries, retryDuration, variables, localVariables));
  }

  @Override
  public void handleBpmnError(String externalTaskId, String workerId, String errorCode) {
    handleBpmnError(externalTaskId, workerId, errorCode, null, null);
  }

  @Override
  public void handleBpmnError(String externalTaskId, String workerId, String errorCode, String errorMessage) {
    handleBpmnError(externalTaskId, workerId, errorCode, errorMessage, null);
  }

  @Override
  public void handleBpmnError(String externalTaskId, String workerId, String errorCode, String errorMessage, Map<String, Object> variables) {
    commandExecutor.execute(new HandleExternalTaskBpmnErrorCmd(externalTaskId, workerId, errorCode, errorMessage, variables));
  }

  @Override
  public void unlock(String externalTaskId) {
    commandExecutor.execute(new UnlockExternalTaskCmd(externalTaskId));
  }

  public void setRetries(String externalTaskId, int retries, boolean writeUserOperationLog) {
    commandExecutor.execute(new SetExternalTaskRetriesCmd(externalTaskId, retries, writeUserOperationLog));
  }

  @Override
  public void setPriority(String externalTaskId, long priority) {
    commandExecutor.execute(new SetExternalTaskPriorityCmd(externalTaskId, priority));
  }

  public ExternalTaskQuery createExternalTaskQuery() {
    return new ExternalTaskQueryImpl(commandExecutor);
  }

  @Override
  public List<String> getTopicNames() {
    return commandExecutor.execute(new GetTopicNamesCmd(false,false,false));
  }

  @Override
  public List<String> getTopicNames(boolean withLockedTasks, boolean withUnlockedTasks, boolean withRetriesLeft) {
    return commandExecutor.execute(new GetTopicNamesCmd(withLockedTasks, withUnlockedTasks, withRetriesLeft));
  }

  public String getExternalTaskErrorDetails(String externalTaskId) {
    return commandExecutor.execute(new GetExternalTaskErrorDetailsCmd(externalTaskId));
  }

  @Override
  public void setRetries(String externalTaskId, int retries) {
    setRetries(externalTaskId, retries, true);
  }

  @Override
  public void setRetries(List<String> externalTaskIds, int retries) {
    updateRetries()
      .externalTaskIds(externalTaskIds)
      .set(retries);
  }

  @Override
  public Batch setRetriesAsync(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, int retries) {
    return updateRetries()
        .externalTaskIds(externalTaskIds)
        .externalTaskQuery(externalTaskQuery)
        .setAsync(retries);
  }

  @Override
  public UpdateExternalTaskRetriesSelectBuilder updateRetries() {
    return new UpdateExternalTaskRetriesBuilderImpl(commandExecutor);
  }

  @Override
  public void extendLock(String externalTaskId, String workerId, long lockDuration) {
    commandExecutor.execute(new ExtendLockOnExternalTaskCmd(externalTaskId, workerId, lockDuration));
  }

}
