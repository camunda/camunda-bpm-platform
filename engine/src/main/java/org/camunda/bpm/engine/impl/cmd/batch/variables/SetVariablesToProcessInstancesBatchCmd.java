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
package org.camunda.bpm.engine.impl.cmd.batch.variables;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.core.variable.VariableUtil;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

public class SetVariablesToProcessInstancesBatchCmd implements Command<Batch> {

  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;
  protected Map<String, ?> variables;

  public SetVariablesToProcessInstancesBatchCmd(List<String> processInstanceIds,
                                                ProcessInstanceQuery processInstanceQuery,
                                                HistoricProcessInstanceQuery historicProcessInstanceQuery,
                                                Map<String, ?> variables) {
    this.processInstanceIds = processInstanceIds;
    this.processInstanceQuery = processInstanceQuery;
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    this.variables = variables;

  }

  @Override
  public Batch execute(CommandContext commandContext) {
    ensureNotNull("variables", variables);
    ensureNotEmpty(BadUserRequestException.class, "variables", variables);
    ensureAtLeastOneNotNull(
        "No process instances found.",
        processInstanceIds,
        processInstanceQuery,
        historicProcessInstanceQuery
    );

    BatchElementConfiguration elementConfiguration = collectProcessInstanceIds(commandContext);

    List<String> ids = elementConfiguration.getIds();
    ensureNotEmpty(BadUserRequestException.class, "processInstanceIds", ids);

    BatchConfiguration configuration = getConfiguration(elementConfiguration);
    Batch batch = new BatchBuilder(commandContext)
        .type(Batch.TYPE_SET_VARIABLES)
        .config(configuration)
        .permission(BatchPermissions.CREATE_BATCH_SET_VARIABLES)
        .operationLogHandler(this::writeUserOperationLog)
        .build();

    String batchId = batch.getId();
    VariableUtil.setVariablesByBatchId(variables, batchId);

    return batch;
  }

  protected void writeUserOperationLog(CommandContext commandContext, int instancesCount) {
    List<PropertyChange> propChanges = new ArrayList<>();

    int variablesCount = variables.size();
    propChanges.add(new PropertyChange("nrOfInstances", null, instancesCount));
    propChanges.add(new PropertyChange("nrOfVariables", null, variablesCount));
    propChanges.add(new PropertyChange("async", null, true));

    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLES, propChanges);
  }

  public BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    DeploymentMappings mappings = elementConfiguration.getMappings();
    List<String> ids = elementConfiguration.getIds();

    return new BatchConfiguration(ids, mappings);
  }


  protected BatchElementConfiguration collectProcessInstanceIds(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();

    if (!CollectionUtil.isEmpty(processInstanceIds)) {
      ProcessInstanceQueryImpl query = new ProcessInstanceQueryImpl();
      query.processInstanceIds(new HashSet<>(processInstanceIds));
      List<ImmutablePair<String, String>> mappings =
          commandContext.runWithoutAuthorization(query::listDeploymentIdMappings);
      elementConfiguration.addDeploymentMappings(mappings);
    }

    ProcessInstanceQueryImpl processInstanceQuery =
        (ProcessInstanceQueryImpl) this.processInstanceQuery;
    if (processInstanceQuery != null) {
      List<ImmutablePair<String, String>> mappings =
          processInstanceQuery.listDeploymentIdMappings();
      elementConfiguration.addDeploymentMappings(mappings);
    }

    HistoricProcessInstanceQueryImpl historicProcessInstanceQuery =
        (HistoricProcessInstanceQueryImpl) this.historicProcessInstanceQuery;
    if (historicProcessInstanceQuery != null) {
      historicProcessInstanceQuery.unfinished();
      List<ImmutablePair<String, String>> mappings =
          historicProcessInstanceQuery.listDeploymentIdMappings();
      elementConfiguration.addDeploymentMappings(mappings);
    }

    return elementConfiguration;
  }

}
