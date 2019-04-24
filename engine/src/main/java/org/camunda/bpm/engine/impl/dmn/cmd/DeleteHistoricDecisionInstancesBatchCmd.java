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
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.batch.AbstractIDBasedBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

public class DeleteHistoricDecisionInstancesBatchCmd extends AbstractIDBasedBatchCmd<Batch> {

  protected List<String> historicDecisionInstanceIds;
  protected HistoricDecisionInstanceQuery historicDecisionInstanceQuery;
  protected String deleteReason;

  public DeleteHistoricDecisionInstancesBatchCmd(List<String> historicDecisionInstanceIds, HistoricDecisionInstanceQuery historicDecisionInstanceQuery, String deleteReason) {
    this.historicDecisionInstanceIds = historicDecisionInstanceIds;
    this.historicDecisionInstanceQuery = historicDecisionInstanceQuery;
    this.deleteReason = deleteReason;
  }

  protected List<String> collectHistoricDecisionInstanceIds() {

    Set<String> collectedDecisionInstanceIds = new HashSet<String>();

    List<String> decisionInstanceIds = getHistoricDecisionInstanceIds();
    if (decisionInstanceIds != null) {
      collectedDecisionInstanceIds.addAll(decisionInstanceIds);
    }

    final HistoricDecisionInstanceQueryImpl decisionInstanceQuery = (HistoricDecisionInstanceQueryImpl) historicDecisionInstanceQuery;
    if (decisionInstanceQuery != null) {
      for (HistoricDecisionInstance hdi : decisionInstanceQuery.list()) {
        collectedDecisionInstanceIds.add(hdi.getId());
      }
    }

    return new ArrayList<String>(collectedDecisionInstanceIds);
  }

  public List<String> getHistoricDecisionInstanceIds() {
    return historicDecisionInstanceIds;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> decisionInstanceIds = collectHistoricDecisionInstanceIds();
    ensureNotEmpty(BadUserRequestException.class, "historicDecisionInstanceIds", decisionInstanceIds);

    checkAuthorizations(commandContext, BatchPermissions.CREATE_BATCH_DELETE_DECISION_INSTANCES);
    writeUserOperationLog(commandContext, decisionInstanceIds.size());

    BatchEntity batch = createBatch(commandContext, decisionInstanceIds);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances) {
    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, true));
    propertyChanges.add(new PropertyChange("deleteReason", null, deleteReason));

    commandContext.getOperationLogManager()
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY, propertyChanges);
  }

  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> processInstanceIds) {
    return new BatchConfiguration(processInstanceIds);
  }

  protected BatchJobHandler<BatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<BatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION);
  }

}
