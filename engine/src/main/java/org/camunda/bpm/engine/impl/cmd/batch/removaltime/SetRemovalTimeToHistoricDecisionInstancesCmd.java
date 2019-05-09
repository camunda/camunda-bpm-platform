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
package org.camunda.bpm.engine.impl.cmd.batch.removaltime;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.removaltime.SetRemovalTimeBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.batch.AbstractIDBasedBatchCmd;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl.Mode;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeToHistoricDecisionInstancesCmd extends AbstractIDBasedBatchCmd<Batch> {

  protected SetRemovalTimeToHistoricDecisionInstancesBuilderImpl builder;

  public SetRemovalTimeToHistoricDecisionInstancesCmd(SetRemovalTimeToHistoricDecisionInstancesBuilderImpl builder) {
    this.builder = builder;
  }

  public Batch execute(CommandContext commandContext) {
    Set<String> historicDecisionInstanceIds = new HashSet<>();

    List<String> instanceIds = builder.getIds();
    HistoricDecisionInstanceQuery instanceQuery = builder.getQuery();
    if (instanceQuery == null && instanceIds == null) {
      throw new BadUserRequestException("Either query nor ids provided.");

    }

    if (instanceQuery != null) {
      for (HistoricDecisionInstance historicDecisionInstance : instanceQuery.list()) {
        historicDecisionInstanceIds.add(historicDecisionInstance.getId());

      }
    }

    if (instanceIds != null) {
      historicDecisionInstanceIds.addAll(findHistoricInstanceIds(instanceIds, commandContext));

    }

    ensureNotNull(BadUserRequestException.class, "removalTime", builder.getMode());
    ensureNotEmpty(BadUserRequestException.class, "historicDecisionInstances", historicDecisionInstanceIds);

    checkAuthorizations(commandContext, BatchPermissions.CREATE_BATCH_SET_REMOVAL_TIME);

    writeUserOperationLog(commandContext, historicDecisionInstanceIds.size(), builder.getMode(), builder.getRemovalTime(),
      builder.isHierarchical(), true);

    BatchEntity batch = createBatch(commandContext, new ArrayList<>(historicDecisionInstanceIds));

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  protected List<String> findHistoricInstanceIds(List<String> instanceIds, CommandContext commandContext) {
    List<HistoricDecisionInstance> historicDecisionInstances = createHistoricDecisionInstanceQuery(commandContext)
      .decisionInstanceIdIn(instanceIds.toArray(new String[0]))
      .list();

    List<String> ids = new ArrayList<>();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      ids.add(historicDecisionInstance.getId());
    }

    return ids;
  }

  protected HistoricDecisionInstanceQuery createHistoricDecisionInstanceQuery(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
      .getHistoryService()
      .createHistoricDecisionInstanceQuery();
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances, Mode mode, Date removalTime,
                                       boolean hierarchical, boolean async) {
    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("mode", null, mode));
    propertyChanges.add(new PropertyChange("removalTime", null, removalTime));
    propertyChanges.add(new PropertyChange("hierarchical", null, hierarchical));
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));

    commandContext.getOperationLogManager()
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_SET_REMOVAL_TIME, propertyChanges);
  }

  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> ids) {
    return new SetRemovalTimeBatchConfiguration(ids)
      .setHierarchical(builder.isHierarchical())
      .setHasRemovalTime(hasRemovalTime(builder.getMode()))
      .setRemovalTime(builder.getRemovalTime());
  }

  protected boolean hasRemovalTime(Mode mode) {
    return builder.getMode() == Mode.ABSOLUTE_REMOVAL_TIME ||
      builder.getMode() == Mode.CLEARED_REMOVAL_TIME;
  }

  protected BatchJobHandler getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_DECISION_SET_REMOVAL_TIME);
  }

}
