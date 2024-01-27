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


import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.SynchronousOperationLogProducer;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResultImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 */
public class CorrelateAllMessageCmd extends AbstractCorrelateMessageCmd implements Command<List<MessageCorrelationResultImpl>>, SynchronousOperationLogProducer<MessageCorrelationResultImpl> {

  /**
   * Initialize the command with a builder
   *
   * @param messageCorrelationBuilderImpl
   */
  public CorrelateAllMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl, boolean collectVariables, boolean deserializeVariableValues) {
    super(messageCorrelationBuilderImpl, collectVariables, deserializeVariableValues);
  }

  public List<MessageCorrelationResultImpl> execute(final CommandContext commandContext) {
    ensureAtLeastOneNotNull(
        "At least one of the following correlation criteria has to be present: " + "messageName, businessKey, correlationKeys, processInstanceId", messageName,
        builder.getBusinessKey(), builder.getCorrelationProcessInstanceVariables(), builder.getProcessInstanceId());

    final CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    final CorrelationSet correlationSet = new CorrelationSet(builder);
    List<CorrelationHandlerResult> correlationResults = commandContext.runWithoutAuthorization(new Callable<List<CorrelationHandlerResult>>() {
      public List<CorrelationHandlerResult> call() throws Exception {
        return correlationHandler.correlateMessages(commandContext, messageName, correlationSet);
      }
    });

    // check authorization
    for (CorrelationHandlerResult correlationResult : correlationResults) {
      checkAuthorization(correlationResult);
    }

    List<MessageCorrelationResultImpl> results = new ArrayList<>();
    for (CorrelationHandlerResult correlationResult : correlationResults) {
      results.add(createMessageCorrelationResult(commandContext, correlationResult));
    }

    produceOperationLog(commandContext, results);

    return results;
  }

  @Override
  public void createOperationLogEntry(CommandContext commandContext, MessageCorrelationResultImpl result, List<PropertyChange> propChanges, boolean isSummary) {
    String processInstanceId = null;
    String processDefinitionId = null;
    if(result.getProcessInstance() != null) {
      if(!isSummary) {
        processInstanceId = result.getProcessInstance().getId();
      }
      processDefinitionId = result.getProcessInstance().getProcessDefinitionId();
    }
    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE, processInstanceId, processDefinitionId, null, propChanges);
  }

  @Override
  public Map<MessageCorrelationResultImpl, List<PropertyChange>> getPropChangesForOperation(List<MessageCorrelationResultImpl> results) {
    Map<MessageCorrelationResultImpl, List<PropertyChange>> resultPropChanges = new HashMap<>();
    for (MessageCorrelationResultImpl messageCorrelationResultImpl : results) {
      List<PropertyChange> propChanges = getGenericPropChangesForOperation();
      ProcessInstance processInstance = messageCorrelationResultImpl.getProcessInstance();
      if(processInstance != null) {
        propChanges.add(new PropertyChange("processInstanceId", null, processInstance.getId()));
      }
      resultPropChanges.put(messageCorrelationResultImpl, propChanges);
    }
    return resultPropChanges;
  }

  @Override
  public List<PropertyChange> getSummarizingPropChangesForOperation(List<MessageCorrelationResultImpl> results) {
    List<PropertyChange> propChanges = getGenericPropChangesForOperation();
    propChanges.add(new PropertyChange("nrOfInstances", null, results.size()));
    return propChanges;
  }

  protected List<PropertyChange> getGenericPropChangesForOperation() {
    ArrayList<PropertyChange> propChanges = new ArrayList<>();

    propChanges.add(new PropertyChange("messageName", null, messageName));
    if(variablesCount > 0) {
      propChanges.add(new PropertyChange("nrOfVariables", null, variablesCount));
    }

    return propChanges;
  }
}
