/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.dmn.cmd;

import static org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil.evaluateDecisionTable;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.dmn.DecisionTableEvaluationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Evaluates the decision table with the given key or id.
 *
 * If the decision definition key given then specify the version and tenant-id.
 * If no version is provided then the latest version is taken.
 *
 * @author Kristin Polenz
 */
public class EvaluateDecisionTableCmd implements Command<DmnDecisionTableResult> {

  protected String decisionDefinitionKey;
  protected String decisionDefinitionId;
  protected Integer version;
  protected VariableMap variables;
  protected String decisionDefinitionTenantId;
  protected boolean isTenandIdSet;

  public EvaluateDecisionTableCmd(DecisionTableEvaluationBuilderImpl builder) {
    this.decisionDefinitionKey = builder.getDecisionDefinitionKey();
    this.decisionDefinitionId = builder.getDecisionDefinitionId();
    this.version = builder.getVersion();
    this.variables = Variables.fromMap(builder.getVariables());
    this.decisionDefinitionTenantId = builder.getDecisionDefinitionTenantId();
    this.isTenandIdSet = builder.isTenantIdSet();
  }

  @Override
  public DmnDecisionTableResult execute(CommandContext commandContext) {
    ensureOnlyOneNotNull("either decision definition id or key must be set", decisionDefinitionId, decisionDefinitionKey);

    DecisionDefinition decisionDefinition = getDecisionDefinition(commandContext);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkEvaluateDecision(decisionDefinition);
    }

    writeUserOperationLog(commandContext, decisionDefinition);

    return doEvaluateDecision(decisionDefinition, variables);

  }

  protected void writeUserOperationLog(CommandContext commandContext, DecisionDefinition decisionDefinition) {
    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("decisionDefinitionId", null, decisionDefinition.getId()));
    propertyChanges.add(new PropertyChange("decisionDefinitionKey", null, decisionDefinition.getKey()));
    commandContext.getOperationLogManager().logDecisionDefinitionOperation(UserOperationLogEntry.OPERATION_TYPE_EVALUATE, decisionDefinition.getTenantId(), propertyChanges);
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
