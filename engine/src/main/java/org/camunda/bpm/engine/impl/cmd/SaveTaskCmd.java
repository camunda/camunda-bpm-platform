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

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Joram Barrez
 */
public class SaveTaskCmd implements Command<Void>, Serializable {

	private static final long serialVersionUID = 1L;

	protected TaskEntity task;

	public SaveTaskCmd(Task task) {
		this.task = (TaskEntity) task;
	}

	public Void execute(CommandContext commandContext) {
    ensureNotNull("task", task);

    String operation;

    if (task.getRevision() == 0) {

      try {
        checkCreateTask(task, commandContext);
        task.insert(null);
        commandContext.getHistoricTaskInstanceManager().createHistoricTask(task);
        operation = UserOperationLogEntry.OPERATION_TYPE_CREATE;
        task.executeMetrics(Metrics.ACTIVTY_INSTANCE_START);
      } catch (NullValueException e) {
        throw new NotValidException(e.getMessage(), e);
      }


    } else {
      checkTaskAssign(task, commandContext);
      task.update();
      operation = UserOperationLogEntry.OPERATION_TYPE_UPDATE;
    }

    task.fireAuthorizationProvider();
    task.fireEvents();
    task.createHistoricTaskDetails(operation);

    return null;
  }

  protected void checkTaskAssign(TaskEntity task, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskAssign(task);
    }
  }

  protected void checkCreateTask(TaskEntity task, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateTask(task);
    }
  }
}
