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

package org.camunda.bpm.engine.impl.dmn.cmd;

import java.util.Arrays;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Deletes historic decision instances with the given id of the instance.
 *
 * @author Johannes Heinemann
 */
public class DeleteHistoricDecisionInstanceByInstanceIdCmd implements Command<Object> {

  protected final String historicDecisionInstanceId;

  public DeleteHistoricDecisionInstanceByInstanceIdCmd(String historicDecisionInstanceId) {
    this.historicDecisionInstanceId = historicDecisionInstanceId;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    ensureNotNull("historicDecisionInstanceId", historicDecisionInstanceId);

    HistoricDecisionInstance historicDecisionInstance = commandContext
        .getHistoricDecisionInstanceManager()
        .findHistoricDecisionInstance(historicDecisionInstanceId);
    ensureNotNull("No historic decision instance found with id: " + historicDecisionInstanceId,
        "historicDecisionInstance", historicDecisionInstance);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricDecisionInstance(historicDecisionInstance);
    }

    commandContext
        .getHistoricDecisionInstanceManager()
        .deleteHistoricDecisionInstanceByIds(Arrays.asList(historicDecisionInstanceId));

    return null;
  }

}
