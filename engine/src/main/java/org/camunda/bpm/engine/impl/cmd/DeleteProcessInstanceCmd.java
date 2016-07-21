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

import java.io.Serializable;
import java.util.Collections;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;



/**
 * @author Joram Barrez
 */
public class DeleteProcessInstanceCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected boolean externallyTerminated;
  protected String processInstanceId;
  protected String deleteReason;
  protected boolean skipCustomListeners;

  public DeleteProcessInstanceCmd(String processInstanceId, String deleteReason, boolean skipCustomListeners) {
    this(processInstanceId,deleteReason,skipCustomListeners,false);
  }

  public DeleteProcessInstanceCmd(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated) {
    this.processInstanceId = processInstanceId;
    this.deleteReason = deleteReason;
    this.skipCustomListeners = skipCustomListeners;
    this.externallyTerminated = externallyTerminated;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull(BadUserRequestException.class, "processInstanceId is null", "processInstanceId", processInstanceId);

    // fetch process instance
    ExecutionManager executionManager = commandContext.getExecutionManager();
    final ExecutionEntity execution = executionManager.findExecutionById(processInstanceId);

    ensureNotNull(BadUserRequestException.class, "No process instance found for id '" + processInstanceId + "'", "processInstance", execution);

    checkDeleteProcessInstance(execution, commandContext);

    // delete process instance
    commandContext
      .getExecutionManager()
      .deleteProcessInstance(processInstanceId, deleteReason, false, skipCustomListeners,externallyTerminated);

    handleHistory(commandContext, execution);

    // create user operation log
    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE, processInstanceId,
          null, null, Collections.singletonList(PropertyChange.EMPTY_CHANGE));

    return null;
  }

  private void handleHistory(CommandContext commandContext, final ExecutionEntity execution) {
    HistoryLevel historyLevel = commandContext.getProcessEngineConfiguration().getHistoryLevel();
    if(historyLevel.isHistoryEventProduced(HistoryEventTypes.PROCESS_INSTANCE_END, execution)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createProcessInstanceEndEvt(execution);
        }
      });
    }
  }

  protected void checkDeleteProcessInstance(ExecutionEntity execution, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteProcessInstance(execution);
    }
  }
}
