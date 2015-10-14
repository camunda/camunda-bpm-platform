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
public class SetExternalTaskRetriesCmd implements Command<Void> {

  protected String externalTaskId;
  protected int retries;

  public SetExternalTaskRetriesCmd(String externalTaskId, int retries) {
    this.externalTaskId = externalTaskId;
    this.retries = retries;
  }

  public Void execute(CommandContext commandContext) {
    validateInput();

    ExternalTaskEntity externalTask =
        commandContext.getExternalTaskManager().findExternalTaskById(externalTaskId);

    EnsureUtil.ensureNotNull(NotFoundException.class, "External task with id '" + externalTaskId + "' not found",
        "externalTask", externalTask);

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkUpdateProcessInstanceById(externalTask.getProcessInstanceId());

    externalTask.setRetriesAndManageIncidents(retries);

    return null;
  }

  protected void validateInput() {
    EnsureUtil.ensureNotNull("externalTaskId", externalTaskId);
    EnsureUtil.ensureGreaterThanOrEqual("retries", retries, 0);
  }

}
