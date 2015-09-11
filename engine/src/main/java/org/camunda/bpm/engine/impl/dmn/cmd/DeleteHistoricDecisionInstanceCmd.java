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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

/**
 * Deletes historic decision instances with the given id of the decision definition.
 *
 * @author Philipp Ossler
 *
 */
public class DeleteHistoricDecisionInstanceCmd implements Command<Object> {

  protected final String decisionDefinitionId;

  public DeleteHistoricDecisionInstanceCmd(String decisionDefinitionId) {
    this.decisionDefinitionId = decisionDefinitionId;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    ensureNotNull("decisionDefinitionId", decisionDefinitionId);

    DecisionDefinitionEntity decisionDefinition = commandContext
        .getDecisionDefinitionManager()
        .findDecisionDefinitionById(decisionDefinitionId);
    ensureNotNull("No decision definition found with id: " + decisionDefinitionId, "decisionDefinition", decisionDefinition);

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkDeleteHistoricDecisionInstance(decisionDefinition.getKey());

    commandContext
      .getHistoricDecisionInstanceManager()
      .deleteHistoricDecisionInstancesByDecisionDefinitionId(decisionDefinitionId);

    return null;
  }

}
