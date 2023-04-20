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

import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Deletes historic decision instances with the given id of the instance.
 *
 * @author Johannes Heinemann
 */
public class DeleteHistoricDecisionInstanceByInstanceIdCmd implements Command<Object> {

  protected final String historicDecisionInstanceId;

  public DeleteHistoricDecisionInstanceByInstanceIdCmd(String historicDecisionInstanceId) {
    this.historicDecisionInstanceId = historicDecisionInstanceId;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    ensureNotNull("historicDecisionInstanceId", historicDecisionInstanceId);

    HistoricDecisionInstance historicDecisionInstance = commandContext
        .getHistoricDecisionInstanceManager()
        .findHistoricDecisionInstance(historicDecisionInstanceId);
    ensureNotNull("No historic decision instance found with id: " + historicDecisionInstanceId,
        "historicDecisionInstance", historicDecisionInstance);
    writeUserOperationLog(commandContext, historicDecisionInstance);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricDecisionInstance(historicDecisionInstance);
    }

    commandContext
        .getHistoricDecisionInstanceManager()
        .deleteHistoricDecisionInstanceByIds(Arrays.asList(historicDecisionInstanceId));

    return null;
  }

  protected void writeUserOperationLog(CommandContext commandContext, HistoricDecisionInstance historicDecisionInstance) {
    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, 1));
    propertyChanges.add(new PropertyChange("async", null, false));

    commandContext.getOperationLogManager().logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY,
        historicDecisionInstance.getTenantId(),
        propertyChanges);
  }
}
