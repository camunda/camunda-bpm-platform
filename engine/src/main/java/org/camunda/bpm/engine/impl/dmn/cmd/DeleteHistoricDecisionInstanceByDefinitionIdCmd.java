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

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Deletes historic decision instances with the given id of the decision definition.
 *
 * @author Philipp Ossler
 *
 */
public class DeleteHistoricDecisionInstanceByDefinitionIdCmd implements Command<Object> {

  protected final String decisionDefinitionId;

  public DeleteHistoricDecisionInstanceByDefinitionIdCmd(String decisionDefinitionId) {
    this.decisionDefinitionId = decisionDefinitionId;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    ensureNotNull("decisionDefinitionId", decisionDefinitionId);

    DecisionDefinitionEntity decisionDefinition = commandContext
        .getDecisionDefinitionManager()
        .findDecisionDefinitionById(decisionDefinitionId);
    ensureNotNull("No decision definition found with id: " + decisionDefinitionId, "decisionDefinition", decisionDefinition);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricDecisionInstance(decisionDefinition.getKey());
    }

    long numInstances = getDecisionInstanceCount(commandContext);
    writeUserOperationLog(commandContext, numInstances);

    commandContext
      .getHistoricDecisionInstanceManager()
      .deleteHistoricDecisionInstancesByDecisionDefinitionId(decisionDefinitionId);

    return null;
  }

  protected void writeUserOperationLog(CommandContext commandContext, long numInstances) {
    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, false));

    commandContext.getOperationLogManager()
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY,
          null,
          propertyChanges);
  }

  protected long getDecisionInstanceCount(CommandContext commandContext) {
    HistoricDecisionInstanceQueryImpl historicDecisionInstanceQuery = new HistoricDecisionInstanceQueryImpl();
    historicDecisionInstanceQuery.decisionDefinitionId(decisionDefinitionId);

    return commandContext.getHistoricDecisionInstanceManager()
      .findHistoricDecisionInstanceCountByQueryCriteria(historicDecisionInstanceQuery);
  }
}
