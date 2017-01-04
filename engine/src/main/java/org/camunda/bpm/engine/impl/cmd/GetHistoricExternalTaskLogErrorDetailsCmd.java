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

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

public class GetHistoricExternalTaskLogErrorDetailsCmd implements Command<String> {

  protected String historicExternalTaskLogId;

  public GetHistoricExternalTaskLogErrorDetailsCmd(String historicExternalTaskLogId) {
    this.historicExternalTaskLogId = historicExternalTaskLogId;
  }

  public String execute(CommandContext commandContext) {
    ensureNotNull("historicExternalTaskLogId", historicExternalTaskLogId);

    HistoricExternalTaskLogEntity event = commandContext
        .getHistoricExternalTaskLogManager()
        .findHistoricExternalTaskLogById(historicExternalTaskLogId);

    ensureNotNull("No historic external task log found with id " + historicExternalTaskLogId, "historicExternalTaskLog", event);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadHistoricExternalTaskLog(event);
    }

    return event.getErrorDetails();
  }

}
