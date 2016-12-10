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

import static org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil.evaluateDecision;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.dmn.DecisionEvaluationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Evaluates the decision with the given key or id.
 *
 * If the decision definition key given then specify the version and tenant-id.
 * If no version is provided then the latest version is taken.
 */
public class EvaluateDecisionCmd implements Command<DmnDecisionResult> {

  protected String decisionDefinitionKey;
  protected String decisionDefinitionId;
  protected Integer version;
  protected VariableMap variables;
  protected String decisionDefinitionTenantId;
  protected boolean isTenandIdSet;

  public EvaluateDecisionCmd(DecisionEvaluationBuilderImpl builder) {
    this.decisionDefinitionKey = builder.getDecisionDefinitionKey();
    this.decisionDefinitionId = builder.getDecisionDefinitionId();
    this.version = builder.getVersion();
    this.variables = Variables.fromMap(builder.getVariables());
    this.decisionDefinitionTenantId = builder.getDecisionDefinitionTenantId();
    this.isTenandIdSet = builder.isTenantIdSet();
  }

  @Override
  public DmnDecisionResult execute(CommandContext commandContext) {
    ensureOnlyOneNotNull("either decision definition id or key must be set", decisionDefinitionId, decisionDefinitionKey);

    DecisionDefinition decisionDefinition = getDecisionDefinition(commandContext);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkEvaluateDecision(decisionDefinition);
    }

    return doEvaluateDecision(decisionDefinition, variables);

  }

  protected DmnDecisionResult doEvaluateDecision(DecisionDefinition decisionDefinition, VariableMap variables) {
    try {
      return evaluateDecision(decisionDefinition, variables);
    }
    catch (Exception e) {
      throw new ProcessEngineException("Exception while evaluating decision with key '"+decisionDefinitionKey+"'", e);
    }
  }

  protected DecisionDefinition getDecisionDefinition(CommandContext commandContext) {
    DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();

    if (decisionDefinitionId != null) {
      return findById(deploymentCache);
    } else {
      return findByKey(deploymentCache);
    }
  }

  protected DecisionDefinition findById(DeploymentCache deploymentCache) {
    return deploymentCache.findDeployedDecisionDefinitionById(decisionDefinitionId);
  }

  protected DecisionDefinition findByKey(DeploymentCache deploymentCache) {
    DecisionDefinition decisionDefinition = null;

    if (version == null && !isTenandIdSet) {
      decisionDefinition = deploymentCache.findDeployedLatestDecisionDefinitionByKey(decisionDefinitionKey);
    }
    else if (version == null && isTenandIdSet) {
      decisionDefinition = deploymentCache.findDeployedLatestDecisionDefinitionByKeyAndTenantId(decisionDefinitionKey, decisionDefinitionTenantId);
    }
    else if (version != null && !isTenandIdSet) {
      decisionDefinition = deploymentCache.findDeployedDecisionDefinitionByKeyAndVersion(decisionDefinitionKey, version);
    }
    else if (version != null && isTenandIdSet) {
      decisionDefinition = deploymentCache.findDeployedDecisionDefinitionByKeyVersionAndTenantId(decisionDefinitionKey, version, decisionDefinitionTenantId);
    }

    return decisionDefinition;
  }

}
