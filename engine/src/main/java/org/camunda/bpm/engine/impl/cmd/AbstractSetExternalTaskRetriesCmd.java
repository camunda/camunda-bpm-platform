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
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

public abstract class AbstractSetExternalTaskRetriesCmd<T> implements Command<T> {

  protected UpdateExternalTaskRetriesBuilderImpl builder;

  public AbstractSetExternalTaskRetriesCmd(UpdateExternalTaskRetriesBuilderImpl builder) {
    this.builder = builder;
  }

  protected List<String> collectProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

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

    return new ArrayList<String>(collectedProcessInstanceIds);
  }

  protected List<String> collectExternalTaskIds() {

    final Set<String> collectedIds = new HashSet<String>();

    List<String> externalTaskIds = builder.getExternalTaskIds();
    if (externalTaskIds != null) {
      ensureNotContainsNull(BadUserRequestException.class, "External task id cannot be null", "externalTaskIds", externalTaskIds);
      collectedIds.addAll(externalTaskIds);
    }

    ExternalTaskQueryImpl externalTaskQuery = (ExternalTaskQueryImpl) builder.getExternalTaskQuery();
    if (externalTaskQuery != null) {
      collectedIds.addAll(externalTaskQuery.listIds());
    }

    final List<String> collectedProcessInstanceIds = collectProcessInstanceIds();
    if (!collectedProcessInstanceIds.isEmpty()) {

      Context.getCommandContext().runWithoutAuthorization(new Callable<Void>() {

        public Void call() throws Exception {
          ExternalTaskQueryImpl query = new ExternalTaskQueryImpl();
          query.processInstanceIdIn(collectedProcessInstanceIds.toArray(new String[collectedProcessInstanceIds.size()]));
          collectedIds.addAll(query.listIds());
          return null;
        }

      });
    }

    return new ArrayList<String>(collectedIds);
  }

  protected void writeUserOperationLog(CommandContext commandContext, int retries, int numInstances, boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));
    propertyChanges.add(new PropertyChange("retries", null, retries));

    commandContext.getOperationLogManager().logExternalTaskOperation(
        UserOperationLogEntry.OPERATION_TYPE_SET_EXTERNAL_TASK_RETRIES, null, propertyChanges);
  }
}
