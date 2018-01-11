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

  public void complete(String externalTaskId, String workerId) {
    complete(externalTaskId, workerId, null, null);
  }

  public void complete(String externalTaskId, String workerId, Map<String, Object> variables) {
    complete(externalTaskId, workerId, variables, null);
  }

  public void complete(String externalTaskId, String workerId, Map<String, Object> variables, Map<String, Object> localVariables) {
    commandExecutor.execute(new CompleteExternalTaskCmd(externalTaskId, workerId, variables, localVariables));
  }

  public void handleFailure(String externalTaskId, String workerId, String errorMessage, int retries, long retryDuration) {
    this.handleFailure(externalTaskId,workerId,errorMessage,null,retries,retryDuration);
  }

  public void handleFailure(String externalTaskId, String workerId, String errorMessage, String errorDetails, int retries, long retryDuration) {
    commandExecutor.execute(new HandleExternalTaskFailureCmd(externalTaskId, workerId, errorMessage, errorDetails, retries, retryDuration));
  }

  @Override
  public void handleBpmnError(String externalTaskId, String workerId, String errorCode) {
    commandExecutor.execute(new HandleExternalTaskBpmnErrorCmd(externalTaskId, workerId, errorCode));
  }

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

  public String getExternalTaskErrorDetails(String externalTaskId) {
    return commandExecutor.execute(new GetExternalTaskErrorDetailsCmd(externalTaskId));
  }

  public void setRetries(String externalTaskId, int retries) {
    setRetries(externalTaskId, retries, true);
  }

  public void setRetries(List<String> externalTaskIds, int retries) {
    updateRetries()
      .externalTaskIds(externalTaskIds)
      .set(retries);
  }

  public Batch setRetriesAsync(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, int retries) {
    return updateRetries()
        .externalTaskIds(externalTaskIds)
        .externalTaskQuery(externalTaskQuery)
        .setAsync(retries);
  }

  public UpdateExternalTaskRetriesSelectBuilder updateRetries() {
    return new UpdateExternalTaskRetriesBuilderImpl(commandExecutor);
  }

  @Override
  public void extendLock(String externalTaskId, String workerId, long lockDuration) {
    commandExecutor.execute(new ExtendLockOnExternalTaskCmd(externalTaskId, workerId, lockDuration));
  }

}
