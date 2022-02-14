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
package org.camunda.bpm.engine.impl.cmd.batch;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.MessageCorrelationAsyncBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.batch.message.MessageCorrelationBatchConfiguration;
import org.camunda.bpm.engine.impl.core.variable.VariableUtil;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class CorrelateAllMessageBatchCmd implements Command<Batch> {

  protected String messageName;
  protected Map<String, Object> variables;

  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;

  public CorrelateAllMessageBatchCmd(MessageCorrelationAsyncBuilderImpl asyncBuilder) {
    this.messageName = asyncBuilder.getMessageName();
    this.variables = asyncBuilder.getPayloadProcessInstanceVariables();
    this.processInstanceIds = asyncBuilder.getProcessInstanceIds();
    this.processInstanceQuery = asyncBuilder.getProcessInstanceQuery();
    this.historicProcessInstanceQuery = asyncBuilder.getHistoricProcessInstanceQuery();
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    ensureAtLeastOneNotNull(
        "No process instances found.",
        processInstanceIds,
        processInstanceQuery,
        historicProcessInstanceQuery
    );

    BatchElementConfiguration elementConfiguration = collectProcessInstanceIds(commandContext);

    List<String> ids = elementConfiguration.getIds();
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty", "process instance ids", ids);

    Batch batch = new BatchBuilder(commandContext)
        .type(Batch.TYPE_CORRELATE_MESSAGE)
        .config(getConfiguration(elementConfiguration))
        .permission(BatchPermissions.CREATE_BATCH_CORRELATE_MESSAGE)
        .operationLogHandler(this::writeUserOperationLog)
        .build();

    if (variables != null) {
      VariableUtil.setVariablesByBatchId(variables, batch.getId());
    }

    return batch;
  }

  protected BatchElementConfiguration collectProcessInstanceIds(CommandContext commandContext) {

    BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();

    if (!CollectionUtil.isEmpty(processInstanceIds)) {
      ProcessInstanceQueryImpl query = new ProcessInstanceQueryImpl();
      query.processInstanceIds(new HashSet<>(processInstanceIds));

      elementConfiguration.addDeploymentMappings(
          commandContext.runWithoutAuthorization(query::listDeploymentIdMappings), processInstanceIds);
    }

    if (processInstanceQuery != null) {
      elementConfiguration.addDeploymentMappings(((ProcessInstanceQueryImpl) processInstanceQuery).listDeploymentIdMappings());
    }

    if (historicProcessInstanceQuery != null) {
      elementConfiguration.addDeploymentMappings(((HistoricProcessInstanceQueryImpl) historicProcessInstanceQuery).listDeploymentIdMappings());
    }

    return elementConfiguration;
  }

  protected BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    return new MessageCorrelationBatchConfiguration(
        elementConfiguration.getIds(),
        elementConfiguration.getMappings(),
        messageName);
  }

  protected void writeUserOperationLog(CommandContext commandContext, int instancesCount) {
    List<PropertyChange> propChanges = new ArrayList<>();

    propChanges.add(new PropertyChange("messageName", null, messageName));
    propChanges.add(new PropertyChange("nrOfInstances", null, instancesCount));
    propChanges.add(new PropertyChange("nrOfVariables", null, variables == null ? 0 : variables.size()));
    propChanges.add(new PropertyChange("async", null, true));

    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE, propChanges);
  }

}
