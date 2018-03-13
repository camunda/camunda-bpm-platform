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
package org.camunda.bpm.client.task.impl;

import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskServiceImpl implements ExternalTaskService {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String taskId;
  protected EngineClient engineClient;

  public ExternalTaskServiceImpl(String taskId, EngineClient engineClient) {
    this.taskId = taskId;
    this.engineClient = engineClient;
  }

  @Override
  public void unlock(ExternalTask externalTask) {
    try {
      engineClient.unlock(externalTask.getId());
    } catch (EngineClientException e) {
      throw LOG.externalTaskServiceException("unlocking the external task", e);
    }
  }

  @Override
  public void complete(ExternalTask externalTask) {
    try {
      engineClient.complete(externalTask.getId());
    } catch (EngineClientException e) {
      throw LOG.externalTaskServiceException("completing the external task", e);
    }
  }

  @Override
  public void handleFailure(ExternalTask externalTask, String errorMessage, String errorDetails, int retries, long retryTimeout) {
    try {
      engineClient.failure(externalTask.getId(), errorMessage, errorDetails, retries, retryTimeout);
    } catch (EngineClientException e) {
      throw LOG.externalTaskServiceException("notifying a failure", e);
    }
  }

  @Override
  public void handleBpmnError(ExternalTask externalTask, String errorCode) {
    try {
      engineClient.bpmnError(externalTask.getId(), errorCode);
    } catch (EngineClientException e) {
      throw LOG.externalTaskServiceException("notifying a BPMN error", e);
    }
  }

  @Override
  public void extendLock(ExternalTask externalTask, long newDuration) {
    try {
      engineClient.extendLock(externalTask.getId(), newDuration);
    } catch (EngineClientException e) {
      throw LOG.externalTaskServiceException("extending lock", e);
    }
  }

}
