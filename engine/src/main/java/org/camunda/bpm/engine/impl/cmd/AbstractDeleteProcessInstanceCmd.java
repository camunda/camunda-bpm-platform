/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
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
public abstract class AbstractDeleteProcessInstanceCmd {

  protected boolean externallyTerminated;
  protected String deleteReason;
  protected boolean skipCustomListeners;
  protected boolean skipSubprocesses;

  protected void checkDeleteProcessInstance(ExecutionEntity execution, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteProcessInstance(execution);
    }
  }

  protected void deleteProcessInstance(
      final CommandContext commandContext,
      String processInstanceId,
      final String deleteReason,
      final boolean skipCustomListeners,
      boolean externallyTerminated,
      final boolean skipIoMappings,
      boolean skipSubprocesses) {
    ensureNotNull(BadUserRequestException.class, "processInstanceId is null", "processInstanceId", processInstanceId);

    // fetch process instance
    ExecutionManager executionManager = commandContext.getExecutionManager();
    final ExecutionEntity execution = executionManager.findExecutionById(processInstanceId);

    ensureNotNull(BadUserRequestException.class, "No process instance found for id '" + processInstanceId + "'", "processInstance", execution);

    checkDeleteProcessInstance(execution, commandContext);

    // delete process instance
    commandContext
        .getExecutionManager()
        .deleteProcessInstance(processInstanceId, deleteReason, false, skipCustomListeners, externallyTerminated, skipIoMappings, skipSubprocesses);

    if (skipSubprocesses) {
      List<ProcessInstance> superProcesslist = commandContext.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery()
          .superProcessInstanceId(processInstanceId).list();
      triggerHistoryEvent(superProcesslist);
    }

    final ExecutionEntity superExecution = execution.getSuperExecution();
    if (superExecution != null) {
      commandContext.runWithoutAuthorization(new Callable<Void>() {
        public Void call() {
          ProcessInstanceModificationBuilderImpl builder = (ProcessInstanceModificationBuilderImpl) new ProcessInstanceModificationBuilderImpl(commandContext, superExecution.getProcessInstanceId(), deleteReason)
            .cancelActivityInstance(superExecution.getActivityInstanceId());
          builder.execute(false, skipCustomListeners, skipIoMappings);
          return null;
        }
      });

    }

    // create user operation log
    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE, processInstanceId,
            null, null, Collections.singletonList(PropertyChange.EMPTY_CHANGE));
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

}
