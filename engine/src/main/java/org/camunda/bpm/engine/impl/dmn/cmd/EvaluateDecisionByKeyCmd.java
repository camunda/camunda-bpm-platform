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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Evaluates the decision with the given key and version. If no version is
 * provided then the latest version is taken.
 *
 * @author Philipp Ossler
 */
public class EvaluateDecisionByKeyCmd implements Command<DmnDecisionTableResult> {

  protected String decisionDefinitionKey;
  protected Integer version;
  protected VariableMap variables;

  public EvaluateDecisionByKeyCmd(String decisionDefinitionKey, Integer version, Map<String, Object> variables) {
    this.decisionDefinitionKey = decisionDefinitionKey;
    this.version = version;
    this.variables = Variables.fromMap(variables);
  }

  public EvaluateDecisionByKeyCmd(String decisionDefinitionKey, Map<String, Object> variables) {
    this(decisionDefinitionKey, null, variables);
  }

  @Override
  public DmnDecisionTableResult execute(CommandContext commandContext) {
    ensureNotNull("decision definition key is null", "processDefinitionKey", decisionDefinitionKey);

    DecisionDefinition decisionDefinition = getDecisionDefinition(commandContext);

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
      throw new ProcessEngineException("Exception while evaluating decision with key '"+decisionDefinitionKey+"'", e);
    }
  }

  protected DecisionDefinition getDecisionDefinition(CommandContext commandContext) {
    DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();

    if (version == null) {
      return deploymentCache.findDeployedLatestDecisionDefinitionByKey(decisionDefinitionKey);
    } else {
      return deploymentCache.findDeployedDecisionDefinitionByKeyAndVersion(decisionDefinitionKey, version);
    }
  }

}
