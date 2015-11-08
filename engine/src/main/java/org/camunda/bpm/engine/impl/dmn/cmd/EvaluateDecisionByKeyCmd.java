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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.dmn.invocation.DecisionInvocation;
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
public class EvaluateDecisionByKeyCmd implements Command<DmnDecisionResult> {

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
  public DmnDecisionResult execute(CommandContext commandContext) {
    ensureNotNull("decision definition key is null", "processDefinitionKey", decisionDefinitionKey);

    DecisionDefinition decisionDefinition = getDecisionDefinition(commandContext);
    ensureNotNull("No decision definition found for key '" + decisionDefinitionKey + "' and version '" + version + "'", "decisionDefinition",
        decisionDefinition);

    // check authorization
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkEvaluateDecision(decisionDefinition.getKey());

    return doEvaluateDecision(commandContext, decisionDefinition, variables);

  }

  protected DmnDecisionResult doEvaluateDecision(CommandContext commandContext, DecisionDefinition decisionDefinition, VariableMap variables) {

    final DecisionInvocation invocation = new DecisionInvocation(decisionDefinition, variables.asVariableContext());

    try {
      commandContext.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);

      return invocation.getInvocationResult();

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
