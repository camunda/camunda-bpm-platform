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

import static org.camunda.bpm.engine.impl.util.DecisionTableUtil.evaluateDecisionTable;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Evaluates the decision with the given id.
 *
 * @author Philipp Ossler
 */
public class EvaluateDecisionByIdCmd implements Command<DmnDecisionTableResult> {

  protected String decisionDefinitionId;
  protected VariableMap variables;

  public EvaluateDecisionByIdCmd(String decisionDefinitionId, Map<String, Object> variables) {
    this.decisionDefinitionId = decisionDefinitionId;
    this.variables = Variables.fromMap(variables);
  }

  @Override
  public DmnDecisionTableResult execute(CommandContext commandContext) {
    ensureNotNull("decision definition id is null", "processDefinitionId", decisionDefinitionId);

    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    DecisionDefinitionEntity decisionDefinition = deploymentCache.findDeployedDecisionDefinitionById(decisionDefinitionId);

    // check authorization
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkEvaluateDecision(decisionDefinition.getKey());

    return doEvaluateDecision(decisionDefinition, variables);
  }


  protected DmnDecisionTableResult doEvaluateDecision(DecisionDefinition decisionDefinition, VariableMap variables) {
    try {
      return evaluateDecisionTable(decisionDefinition, variables);
    }
    catch (Exception e) {
      throw new ProcessEngineException("Exception while evaluating decision with id '"+decisionDefinitionId+"'", e);
    }
  }

}
