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

import java.io.Serializable;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureGreaterThanOrEqual;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Svetlana Dorokhova
 */
public class UpdateDecisionDefinitionHistoryTimeToLiveCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String decisionDefinitionId;
  protected Integer historyTimeToLive;

  public UpdateDecisionDefinitionHistoryTimeToLiveCmd(String decisionDefinitionId, Integer historyTimeToLive) {
    this.decisionDefinitionId = decisionDefinitionId;
    this.historyTimeToLive = historyTimeToLive;
  }

  public Void execute(CommandContext commandContext) {
    checkAuthorization(commandContext);

    ensureNotNull(BadUserRequestException.class, "decisionDefinitionId", decisionDefinitionId);
    if (historyTimeToLive != null) {
      ensureGreaterThanOrEqual(BadUserRequestException.class, "", "historyTimeToLive", historyTimeToLive, 0);
    }

    DecisionDefinitionEntity decisionDefinitionEntity = commandContext.getDecisionDefinitionManager().findDecisionDefinitionById(decisionDefinitionId);
    decisionDefinitionEntity.setHistoryTimeToLive(historyTimeToLive);

    return null;
  }

  protected void checkAuthorization(CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateDecisionDefinitionById(decisionDefinitionId);
    }
  }

}
