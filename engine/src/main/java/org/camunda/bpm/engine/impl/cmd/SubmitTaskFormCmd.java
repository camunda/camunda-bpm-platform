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

import java.io.Serializable;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.handler.TaskFormHandler;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.DelegationState;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SubmitTaskFormCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected Map<String, Object> properties;

  public SubmitTaskFormCmd(String taskId, Map<String, Object> properties) {
    this.taskId = taskId;
    this.properties = properties;
  }

  public Object execute(CommandContext commandContext) {
    if(taskId == null) {
      throw new ProcessEngineException("taskId is null");
    }

    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);

    if (task == null) {
      throw new ProcessEngineException("Cannot find task with id " + taskId);
    }

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    int historyLevel = processEngineConfiguration.getHistoryLevel();
    ExecutionEntity execution = task.getExecution();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT && execution != null) {

      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();

      for (String propertyId: properties.keySet()) {
        Object propertyValue = properties.get(propertyId);

        HistoryEvent evt = eventProducer.createFormPropertyUpdateEvt(execution, propertyId, propertyValue, taskId);
        eventHandler.handleEvent(evt);

      }
    }

    TaskFormHandler taskFormHandler = task.getTaskDefinition().getTaskFormHandler();
    taskFormHandler.submitFormProperties(properties, task.getExecution());

    // complete or resolve the task
    if(DelegationState.PENDING.equals(task.getDelegationState())) {
      task.resolve();
      task.createHistoricTaskDetails(UserOperationLogEntry.OPERATION_TYPE_RESOLVE);
    } else {
      task.complete();
      task.createHistoricTaskDetails(UserOperationLogEntry.OPERATION_TYPE_COMPLETE);
    }

    return null;
  }
}
