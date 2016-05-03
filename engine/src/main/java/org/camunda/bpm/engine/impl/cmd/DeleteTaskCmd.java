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
import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;


/**
 * @author Joram Barrez
 */
public class DeleteTaskCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected Collection<String> taskIds;
  protected boolean cascade;
  protected String deleteReason;

  public DeleteTaskCmd(String taskId, String deleteReason, boolean cascade) {
    this.taskId = taskId;
    this.cascade = cascade;
    this.deleteReason = deleteReason;
  }

  public DeleteTaskCmd(Collection<String> taskIds, String deleteReason, boolean cascade) {
    this.taskIds = taskIds;
    this.cascade = cascade;
    this.deleteReason = deleteReason;
  }

  public Void execute(CommandContext commandContext) {
    if (taskId != null) {
      deleteTask(taskId, commandContext);
    } else if (taskIds != null) {
        for (String taskId : taskIds) {
          deleteTask(taskId, commandContext);
        }
    } else {
      throw new ProcessEngineException("taskId and taskIds are null");
    }


    return null;
  }

  protected void deleteTask(String taskId, CommandContext commandContext) {
    TaskManager taskManager = commandContext.getTaskManager();
    TaskEntity task = taskManager.findTaskById(taskId);

    if (task != null) {
      if(task.getExecutionId() != null) {
        throw new ProcessEngineException("The task cannot be deleted because is part of a running process");
      } else if (task.getCaseExecutionId() != null) {
        throw new ProcessEngineException("The task cannot be deleted because is part of a running case instance");
      }

      checkDeleteTask(task, commandContext);

      String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;
      task.delete(reason, cascade);
    } else if (cascade) {
      Context
        .getCommandContext()
        .getHistoricTaskInstanceManager()
        .deleteHistoricTaskInstanceById(taskId);
    }
  }

  protected void checkDeleteTask(TaskEntity task,  CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteTask(task);
    }
  }
}
