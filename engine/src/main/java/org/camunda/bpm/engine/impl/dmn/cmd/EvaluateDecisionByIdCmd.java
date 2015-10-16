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

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;

/**
 * Evaluates the decision with the given id.
 *
 * @author Philipp Ossler
 */
public class EvaluateDecisionByIdCmd implements Command<DmnDecisionResult> {

  protected String decisionDefinitionId;
  protected Map<String, Object> variables;

  public EvaluateDecisionByIdCmd(String decisionDefinitionId, Map<String, Object> variables) {
    this.decisionDefinitionId = decisionDefinitionId;
    this.variables = variables;
  }

  @Override
  public DmnDecisionResult execute(CommandContext commandContext) {
    ensureNotNull("decision definition id is null", "processDefinitionId", decisionDefinitionId);

    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    DmnEngine dmnEngine = commandContext.getProcessEngineConfiguration().getDmnEngine();

    DecisionDefinitionEntity decisionDefinition = deploymentCache.findDeployedDecisionDefinitionById(decisionDefinitionId);
    ensureNotNull("No decision definition found for id '" + decisionDefinitionId + "'", "decisionDefinition", decisionDefinition);

    DmnDecisionResult decisionResult = dmnEngine.evaluate(decisionDefinition, variables);
    return decisionResult;
  }

}
