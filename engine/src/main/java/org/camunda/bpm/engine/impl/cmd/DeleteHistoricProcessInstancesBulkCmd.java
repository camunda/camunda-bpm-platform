/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureEquals;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * @author Svetlana Dorokhova
 */
public class DeleteHistoricProcessInstancesBulkCmd implements Command<Void>, Serializable {

  protected final List<String> processInstanceIds;

  public DeleteHistoricProcessInstancesBulkCmd(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkAuthorization(Permissions.DELETE_HISTORY, Resources.PROCESS_DEFINITION);

    ensureNotEmpty(BadUserRequestException.class, "processInstanceIds", processInstanceIds);
    // Check if process instances are all finished
    commandContext.runWithoutAuthorization(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        ensureEquals(BadUserRequestException.class, "FinishedProcessInstanceIds",
          new HistoricProcessInstanceQueryImpl().finished().processInstanceIds(new HashSet<String>(processInstanceIds)).count(), processInstanceIds.size());
        return null;
      }
    });

    commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(processInstanceIds);

    return null;
  }
}
