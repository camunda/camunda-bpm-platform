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
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.batch.removaltime.SetRemovalTimeBatchConfiguration;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricDecisionInstancesBuilderImpl.Mode;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

import java.util.ArrayList;
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
    if (builder.getQuery() == null && builder.getIds() == null) {
      throw new BadUserRequestException("Neither query nor ids provided.");
    }

    BatchElementConfiguration elementConfiguration = collectInstances(commandContext);

    ensureNotNull(BadUserRequestException.class, "removalTime", builder.getMode());
    ensureNotEmpty(BadUserRequestException.class, "historicDecisionInstances", elementConfiguration.getIds());

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_DECISION_SET_REMOVAL_TIME)
        .config(getConfiguration(elementConfiguration))
        .permission(BatchPermissions.CREATE_BATCH_SET_REMOVAL_TIME)
        .operationLogHandler(this::writeUserOperationLog)
        .build();
  }

  protected BatchElementConfiguration collectInstances(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();

    if (builder.getQuery() != null) {
      elementConfiguration.addDeploymentMappings(((HistoricDecisionInstanceQueryImpl) builder.getQuery()).listDeploymentIdMappings());
    }

    List<String> idList = builder.getIds();
    if (!CollectionUtil.isEmpty(idList)) {
      HistoricDecisionInstanceQueryImpl query = new HistoricDecisionInstanceQueryImpl();
      query.decisionInstanceIdIn(idList.toArray(new String[0]));
      elementConfiguration.addDeploymentMappings(commandContext.runWithoutAuthorization(query::listDeploymentIdMappings));
    }

    return elementConfiguration;
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
      .logDecisionInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_SET_REMOVAL_TIME, null, propertyChanges);
  }

  protected boolean hasRemovalTime() {
    return builder.getMode() == Mode.ABSOLUTE_REMOVAL_TIME ||
      builder.getMode() == Mode.CLEARED_REMOVAL_TIME;
  }

  public BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    return new SetRemovalTimeBatchConfiguration(elementConfiguration.getIds(), elementConfiguration.getMappings())
        .setHierarchical(builder.isHierarchical())
        .setHasRemovalTime(hasRemovalTime())
        .setRemovalTime(builder.getRemovalTime());
  }

}
