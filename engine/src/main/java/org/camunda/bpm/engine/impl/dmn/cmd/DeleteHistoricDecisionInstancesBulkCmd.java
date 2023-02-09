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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * Deletes historic decision instances with the given ids in bulk manner.
 *
 * @author Svetlana Dorokhova
 *
 */
public class DeleteHistoricDecisionInstancesBulkCmd implements Command<Object> {

  protected final List<String> decisionInstanceIds;

  public DeleteHistoricDecisionInstancesBulkCmd(List<String> decisionInstanceIds) {
    this.decisionInstanceIds = decisionInstanceIds;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkAuthorization(Permissions.DELETE_HISTORY, Resources.DECISION_DEFINITION);

    ensureNotEmpty(BadUserRequestException.class, "decisionInstanceIds", decisionInstanceIds);
    writeUserOperationLog(commandContext, decisionInstanceIds.size());

    commandContext.getHistoricDecisionInstanceManager().deleteHistoricDecisionInstanceByIds(decisionInstanceIds);

    return null;
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances) {
    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, false));

    commandContext.getOperationLogManager()
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY,
          null,
          propertyChanges);
  }
}
