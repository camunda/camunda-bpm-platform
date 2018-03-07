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
package org.camunda.bpm.client.impl;

import org.camunda.bpm.client.LockedTaskService;
import org.camunda.bpm.client.impl.engineclient.EngineClient;
import org.camunda.bpm.client.impl.engineclient.EngineClientException;

/**
 * @author Tassilo Weidner
 */
public class LockedTaskServiceImpl implements LockedTaskService {

  private static final ClientLogger LOG = ClientLogger.CLIENT_LOGGER;

  private String taskId;
  private EngineClient engineClient;

  LockedTaskServiceImpl(String taskId, EngineClient engineClient) {
    this.taskId = taskId;
    this.engineClient = engineClient;
  }

  @Override
  public void unlock() {
    try {
      engineClient.unlock(taskId);
    } catch (EngineClientException e) {
      throw LOG.unlockTaskException(e);
    }
  }

  @Override
  public void complete() {
    try {
      engineClient.complete(taskId);
    } catch (EngineClientException e) {
      throw LOG.completeTaskException(e);
    }
  }

  @Override
  public void failure(String errorMessage, String errorDetails, int retries, long retryTimeout) {
    try {
      engineClient.failure(taskId, errorMessage, errorDetails, retries, retryTimeout);
    } catch (EngineClientException e) {
      throw LOG.taskFailureException(e);
    }
  }

  @Override
  public void bpmnError(String errorCode) {
    try {
      engineClient.bpmnError(taskId, errorCode);
    } catch (EngineClientException e) {
      throw LOG.bpmnErrorException(e);
    }
  }

  @Override
  public void extendLock(long newDuration) {
    try {
      engineClient.extendLock(taskId, newDuration);
    } catch (EngineClientException e) {
      throw LOG.extendLockException(e);
    }
  }

}
