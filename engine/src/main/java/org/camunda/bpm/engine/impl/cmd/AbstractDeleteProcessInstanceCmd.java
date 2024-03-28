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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.SynchronousOperationLogProducer;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * Created by aakhmerov on 16.09.16.
 * <p>
 * Provide common logic for process instance deletion operations.
 * Permissions checking and single process instance removal included.
 */
public abstract class AbstractDeleteProcessInstanceCmd implements SynchronousOperationLogProducer<ProcessInstance>{

  protected boolean externallyTerminated;
  protected String deleteReason;
  protected boolean skipCustomListeners;
  protected boolean skipSubprocesses;
  protected boolean failIfNotExists = true;
  protected boolean skipIoMappings;

  protected List<ProcessInstance> deletedInstances = new ArrayList<>();

  protected void checkDeleteProcessInstance(ExecutionEntity execution, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteProcessInstance(execution);
    }
  }

  protected void deleteProcessInstances(final CommandContext commandContext, List<String> processInstanceIds) {
    processInstanceIds.forEach(processInstance -> deleteProcessInstance(commandContext, processInstance, false));
    // create user operation log
    produceOperationLog(commandContext, deletedInstances);
  }

  protected void deleteProcessInstance(final CommandContext commandContext, String processInstanceId, boolean writeOpLogImmediately) {
    ensureNotNull(BadUserRequestException.class, "processInstanceId is null", "processInstanceId", processInstanceId);

    // fetch process instance
    ExecutionManager executionManager = commandContext.getExecutionManager();
    final ExecutionEntity execution = executionManager.findExecutionById(processInstanceId);

    if(!failIfNotExists && execution == null) {
      return;
    }

    ensureNotNull(NotFoundException.class, "No process instance found for id '" + processInstanceId + "'",
        "processInstance", execution);

    checkDeleteProcessInstance(execution, commandContext);

    // delete process instance
    commandContext
        .getExecutionManager()
        .deleteProcessInstance(processInstanceId, deleteReason, false, skipCustomListeners,
            externallyTerminated, skipIoMappings, skipSubprocesses);

    if (skipSubprocesses) {
      List<ProcessInstance> superProcesslist = commandContext.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery()
          .superProcessInstanceId(processInstanceId).list();
      triggerHistoryEvent(superProcesslist);
    }

    final ExecutionEntity superExecution = execution.getSuperExecution();

    if (superExecution != null) {
      commandContext.runWithoutAuthorization((Callable<Void>) () -> {
        ProcessInstanceModificationBuilderImpl builder = (ProcessInstanceModificationBuilderImpl) new ProcessInstanceModificationBuilderImpl(commandContext, superExecution.getProcessInstanceId(), deleteReason)
          .cancellationSourceExternal(externallyTerminated).cancelActivityInstance(superExecution.getActivityInstanceId());
        builder.execute(false, skipCustomListeners, skipIoMappings);
        return null;
      });

    }

    // create user operation log
    if(writeOpLogImmediately) {
      produceOperationLog(commandContext, List.of(execution));
    } else {
      deletedInstances.add(execution);
    }
  }

  public void triggerHistoryEvent(List<ProcessInstance> subProcesslist) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();

    for (final ProcessInstance processInstance : subProcesslist) {
      // TODO: This smells bad, as the rest of the history is done via the
      // ParseListener
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.PROCESS_INSTANCE_UPDATE, processInstance)) {

        HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
          @Override
          public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
            return producer.createProcessInstanceUpdateEvt((DelegateExecution) processInstance);
          }
        });
      }
    }
  }

  @Override
  public Map<ProcessInstance, List<PropertyChange>> getPropChangesForOperation(List<ProcessInstance> results) {
    return null;
  }

  @Override
  public List<PropertyChange> getSummarizingPropChangesForOperation(List<ProcessInstance> results) {
    ArrayList<PropertyChange> propChanges = new ArrayList<>();
    propChanges.add(new PropertyChange("nrOfInstances", null, results.size()));
    return propChanges;
  }

  @Override
  public void createOperationLogEntry(CommandContext commandContext, ProcessInstance result,
      List<PropertyChange> propChanges, boolean isSummary) {

    String processInstanceId = null;
    String processDefinitionId = null;
    if (!isSummary) {
      processInstanceId = result.getId();
      processDefinitionId = result.getProcessDefinitionId();
    }

    commandContext.getOperationLogManager()
    .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE, processInstanceId,
        processDefinitionId, null, propChanges);
  }
}
