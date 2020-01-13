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
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.removaltime.SetRemovalTimeBatchConfiguration;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl.Mode;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeToHistoricDecisionInstancesCmd implements Command<Batch> {

  protected SetRemovalTimeToHistoricDecisionInstancesBuilderImpl builder;

  public SetRemovalTimeToHistoricDecisionInstancesCmd(SetRemovalTimeToHistoricDecisionInstancesBuilderImpl builder) {
    this.builder = builder;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    Collection<String> collectedInstanceIds = new HashSet<>();

    List<String> instanceIds = builder.getIds();
    HistoricDecisionInstanceQuery instanceQuery = builder.getQuery();
    if (instanceQuery == null && instanceIds == null) {
      throw new BadUserRequestException("Neither query nor ids provided.");

    }

    if (instanceQuery != null) {
      for (HistoricDecisionInstance historicDecisionInstance : instanceQuery.list()) {
        collectedInstanceIds.add(historicDecisionInstance.getId());

      }
    }

    if (instanceIds != null) {
      collectedInstanceIds.addAll(findHistoricInstanceIds(instanceIds, commandContext));

    }

    ensureNotNull(BadUserRequestException.class, "removalTime", builder.getMode());
    ensureNotEmpty(BadUserRequestException.class, "historicDecisionInstances", collectedInstanceIds);

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_DECISION_SET_REMOVAL_TIME)
        .config(getConfiguration(collectedInstanceIds))
        .permission(BatchPermissions.CREATE_BATCH_SET_REMOVAL_TIME)
        .operationLogHandler(this::writeUserOperationLog)
        .build();
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

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances) {
    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("mode", null, builder.getMode()));
    propertyChanges.add(new PropertyChange("removalTime", null, builder.getRemovalTime()));
    propertyChanges.add(new PropertyChange("hierarchical", null, builder.isHierarchical()));
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, true));

    commandContext.getOperationLogManager()
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_SET_REMOVAL_TIME, propertyChanges);
  }

  protected boolean hasRemovalTime() {
    return builder.getMode() == Mode.ABSOLUTE_REMOVAL_TIME ||
      builder.getMode() == Mode.CLEARED_REMOVAL_TIME;
  }

  public BatchConfiguration getConfiguration(Collection<String> instances) {
    return new SetRemovalTimeBatchConfiguration(new ArrayList<>(instances))
        .setHierarchical(builder.isHierarchical())
        .setHasRemovalTime(hasRemovalTime())
        .setRemovalTime(builder.getRemovalTime());
  }

}
