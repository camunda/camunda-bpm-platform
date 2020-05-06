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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

public abstract class AbstractSetExternalTaskRetriesCmd<T> implements Command<T> {

  protected UpdateExternalTaskRetriesBuilderImpl builder;

  public AbstractSetExternalTaskRetriesCmd(UpdateExternalTaskRetriesBuilderImpl builder) {
    this.builder = builder;
  }

  protected List<String> collectProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<>();

    List<String> processInstanceIds = builder.getProcessInstanceIds();
    if (processInstanceIds != null && !processInstanceIds.isEmpty()) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    ProcessInstanceQueryImpl processInstanceQuery = (ProcessInstanceQueryImpl) builder.getProcessInstanceQuery();
    if (processInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(processInstanceQuery.listIds());
    }

    HistoricProcessInstanceQueryImpl historicProcessInstanceQuery = (HistoricProcessInstanceQueryImpl) builder.getHistoricProcessInstanceQuery();
    if (historicProcessInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(historicProcessInstanceQuery.listIds());
    }

    return new ArrayList<>(collectedProcessInstanceIds);
  }

  protected BatchElementConfiguration collectExternalTaskIds(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();

    List<String> externalTaskIds = builder.getExternalTaskIds();
    if (!CollectionUtil.isEmpty(externalTaskIds)) {
      ensureNotContainsNull(BadUserRequestException.class, "External task id cannot be null", "externalTaskIds", externalTaskIds);
      ExternalTaskQueryImpl taskQuery = new ExternalTaskQueryImpl();
      taskQuery.externalTaskIdIn(new HashSet<>(externalTaskIds));
      elementConfiguration.addDeploymentMappings(commandContext.runWithoutAuthorization(
          taskQuery::listDeploymentIdMappings), externalTaskIds);
    }

    ExternalTaskQueryImpl externalTaskQuery = (ExternalTaskQueryImpl) builder.getExternalTaskQuery();
    if (externalTaskQuery != null) {
      elementConfiguration.addDeploymentMappings(externalTaskQuery.listDeploymentIdMappings());
    }

    final List<String> collectedProcessInstanceIds = collectProcessInstanceIds();
    if (!collectedProcessInstanceIds.isEmpty()) {
      ExternalTaskQueryImpl query = new ExternalTaskQueryImpl();
      query.processInstanceIdIn(collectedProcessInstanceIds.toArray(new String[0]));
      elementConfiguration.addDeploymentMappings(commandContext.runWithoutAuthorization(query::listDeploymentIdMappings));
    }

    return elementConfiguration;
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances,
                                       boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));
    propertyChanges.add(new PropertyChange("retries", null, builder.getRetries()));

    commandContext.getOperationLogManager().logExternalTaskOperation(
        UserOperationLogEntry.OPERATION_TYPE_SET_EXTERNAL_TASK_RETRIES, null, propertyChanges);
  }

  protected void writeUserOperationLogAsync(CommandContext commandContext, int numInstances) {
    writeUserOperationLog(commandContext, numInstances, true);
  }

}
