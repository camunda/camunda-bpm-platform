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
package org.camunda.bpm.engine.impl.cmd.optimize;

import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.variable.type.ValueType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OptimizeHistoricVariableUpdateQueryCmd implements Command<List<HistoricVariableUpdate>> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected Date occurredAfter;
  protected Date occurredAt;
  protected boolean excludeObjectValues;
  protected int maxResults;

  public OptimizeHistoricVariableUpdateQueryCmd(Date occurredAfter,
                                                Date occurredAt,
                                                boolean excludeObjectValues,
                                                int maxResults) {
    this.occurredAfter = occurredAfter;
    this.occurredAt = occurredAt;
    this.excludeObjectValues = excludeObjectValues;
    this.maxResults = maxResults;
  }

  public List<HistoricVariableUpdate> execute(CommandContext commandContext) {
    List<HistoricVariableUpdate> historicVariableUpdates =
      commandContext.getOptimizeManager().getHistoricVariableUpdates(occurredAfter, occurredAt, maxResults);
    fetchVariableValues(historicVariableUpdates, commandContext);
    return historicVariableUpdates;
  }

  private void fetchVariableValues(List<HistoricVariableUpdate> historicVariableUpdates,
                                   CommandContext commandContext) {
    if (!CollectionUtil.isEmpty(historicVariableUpdates)) {

      List<String> byteArrayIds = getByteArrayIds(historicVariableUpdates);
      if (!byteArrayIds.isEmpty()) {
        // pre-fetch all byte arrays into dbEntityCache to avoid (n+1) number of queries
        commandContext.getOptimizeManager().fetchHistoricVariableUpdateByteArrays(byteArrayIds);
      }

      resolveTypedValues(historicVariableUpdates);
    }
  }

  protected boolean shouldFetchValue(HistoricDetailVariableInstanceUpdateEntity entity) {
    final ValueType entityType = entity.getSerializer().getType();
    // do no fetch values for byte arrays/blob variables (e.g. files or bytes)
    return !AbstractTypedValueSerializer.BINARY_VALUE_TYPES.contains(entityType.getName())
        // nor object values unless enabled
        && (!ValueType.OBJECT.equals(entityType) || !excludeObjectValues);
  }

  protected boolean isHistoricDetailVariableInstanceUpdateEntity(HistoricVariableUpdate variableUpdate) {
    return variableUpdate instanceof HistoricDetailVariableInstanceUpdateEntity;
  }

  protected List<String> getByteArrayIds(List<HistoricVariableUpdate> variableUpdates) {
    List<String> byteArrayIds = new ArrayList<>();

    for (HistoricVariableUpdate variableUpdate : variableUpdates) {
      if (isHistoricDetailVariableInstanceUpdateEntity(variableUpdate)) {
        HistoricDetailVariableInstanceUpdateEntity entity = (HistoricDetailVariableInstanceUpdateEntity) variableUpdate;

        if (shouldFetchValue(entity)) {
          String byteArrayId = entity.getByteArrayValueId();
          if (byteArrayId != null) {
            byteArrayIds.add(byteArrayId);
          }
        }

      }
    }

    return byteArrayIds;
  }

  protected void resolveTypedValues(List<HistoricVariableUpdate> variableUpdates) {
    for (HistoricVariableUpdate variableUpdate : variableUpdates) {
      if (isHistoricDetailVariableInstanceUpdateEntity(variableUpdate)) {
        HistoricDetailVariableInstanceUpdateEntity entity = (HistoricDetailVariableInstanceUpdateEntity) variableUpdate;

        if (shouldFetchValue(entity)) {
          try {
            entity.getTypedValue(false);
          } catch (Exception t) {
            // do not fail if one of the variables fails to load
            LOG.exceptionWhileGettingValueForVariable(t);
          }
        }

      }
    }
  }

}
