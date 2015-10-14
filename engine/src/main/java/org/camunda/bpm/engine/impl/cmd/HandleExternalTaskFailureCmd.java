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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class HandleExternalTaskFailureCmd implements Command<Void> {

  protected String externalTaskId;
  protected String workerId;
  protected String errorMessage;
  protected long retryDuration;
  protected int retries;

  public HandleExternalTaskFailureCmd(String externalTaskId, String workerId,
      String errorMessage, int retries, long retryDuration) {
    this.externalTaskId = externalTaskId;
    this.workerId = workerId;
    this.errorMessage = errorMessage;
    this.retries = retries;
    this.retryDuration = retryDuration;
  }

  public Void execute(CommandContext commandContext) {
    validateInput();

    ExternalTaskEntity externalTask = commandContext.getExternalTaskManager().findExternalTaskById(externalTaskId);
    EnsureUtil.ensureNotNull(NotFoundException.class,
        "Cannot find external task with id " + externalTaskId, "externalTask", externalTask);

    if (!workerId.equals(externalTask.getWorkerId())) {
      throw new BadUserRequestException("Failure of External Task " + externalTaskId + " cannot be reported by worker '" + workerId
          + "'. It is locked by worker '" + externalTask.getWorkerId() + "'.");
    }

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkUpdateProcessInstanceById(externalTask.getProcessInstanceId());

    externalTask.failed(errorMessage, retries, retryDuration);

    return null;
  }

  protected void validateInput() {
    EnsureUtil.ensureNotNull("externalTaskId", externalTaskId);
    EnsureUtil.ensureNotNull("workerId", workerId);

    EnsureUtil.ensureGreaterThanOrEqual("retries", retries, 0);
    EnsureUtil.ensureGreaterThanOrEqual("retryDuration", retryDuration, 0);

  }


}
