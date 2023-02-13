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
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

public class DeleteHistoricDecisionInstancesBatchCmd implements Command<Batch> {

  protected List<String> historicDecisionInstanceIds;
  protected HistoricDecisionInstanceQuery historicDecisionInstanceQuery;
  protected String deleteReason;

  public DeleteHistoricDecisionInstancesBatchCmd(List<String> ids,
                                                 HistoricDecisionInstanceQuery query,
                                                 String deleteReason) {
    this.historicDecisionInstanceIds = ids;
    this.historicDecisionInstanceQuery = query;
    this.deleteReason = deleteReason;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = collectHistoricDecisionInstanceIds(commandContext);
    ensureNotEmpty(BadUserRequestException.class, "historicDecisionInstanceIds", elementConfiguration.getIds());

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION)
        .config(getConfiguration(elementConfiguration))
        .permission(BatchPermissions.CREATE_BATCH_DELETE_DECISION_INSTANCES)
        .operationLogHandler(this::writeUserOperationLog)
        .build();
  }

  protected BatchElementConfiguration collectHistoricDecisionInstanceIds(CommandContext commandContext) {

    BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();

    if (!CollectionUtil.isEmpty(historicDecisionInstanceIds)) {
      HistoricDecisionInstanceQueryImpl query = new HistoricDecisionInstanceQueryImpl();
      query.decisionInstanceIdIn(historicDecisionInstanceIds.toArray(new String[0]));
      elementConfiguration.addDeploymentMappings(
          commandContext.runWithoutAuthorization(query::listDeploymentIdMappings), historicDecisionInstanceIds);
    }

    final HistoricDecisionInstanceQueryImpl decisionInstanceQuery =
        (HistoricDecisionInstanceQueryImpl) historicDecisionInstanceQuery;
    if (decisionInstanceQuery != null) {
      elementConfiguration.addDeploymentMappings(decisionInstanceQuery.listDeploymentIdMappings());
    }

    return elementConfiguration;
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances) {
    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, true));
    propertyChanges.add(new PropertyChange("deleteReason", null, deleteReason));

    commandContext.getOperationLogManager()
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY,
          null,
          propertyChanges);
  }

  public BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    return new BatchConfiguration(elementConfiguration.getIds(), elementConfiguration.getMappings());
  }

}
