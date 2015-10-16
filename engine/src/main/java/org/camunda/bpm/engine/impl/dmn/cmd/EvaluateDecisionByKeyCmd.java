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

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.repository.DecisionDefinition;

/**
 * Evaluates the decision with the given key and version. If no version is
 * provided then the latest version is taken.
 *
 * @author Philipp Ossler
 */
public class EvaluateDecisionByKeyCmd implements Command<DmnDecisionResult> {

  protected String decisionDefinitionKey;
  protected Integer version;
  protected Map<String, Object> variables;

  public EvaluateDecisionByKeyCmd(String decisionDefinitionKey, Integer version, Map<String, Object> variables) {
    this.decisionDefinitionKey = decisionDefinitionKey;
    this.version = version;
    this.variables = variables;
  }

  public EvaluateDecisionByKeyCmd(String decisionDefinitionKey, Map<String, Object> variables) {
    this(decisionDefinitionKey, null, variables);
  }

  @Override
  public DmnDecisionResult execute(CommandContext commandContext) {
    ensureNotNull("decision definition key is null", "processDefinitionKey", decisionDefinitionKey);

    DmnEngine dmnEngine = commandContext.getProcessEngineConfiguration().getDmnEngine();

    DecisionDefinition decisionDefinition = getDecisionDefinition();
    ensureNotNull("No decision definition found for key '" + decisionDefinitionKey + "' and version '" + version + "'", "decisionDefinition",
        decisionDefinition);

    DmnDecisionResult decisionResult = dmnEngine.evaluate((DmnDecision) decisionDefinition, variables);
    return decisionResult;
  }

  protected DecisionDefinition getDecisionDefinition() {
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();

    if (version == null) {
      return deploymentCache.findDeployedLatestDecisionDefinitionByKey(decisionDefinitionKey);
    } else {
      return deploymentCache.findDeployedDecisionDefinitionByKeyAndVersion(decisionDefinitionKey, version);
    }
  }

}
