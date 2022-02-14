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

import java.io.Serializable;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.TaskState;
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
    validateStandaloneTask(task, commandContext);

    String operation;
    if (task.getRevision() == 0) {

      try {
        checkCreateTask(task, commandContext);
        task.ensureParentTaskActive();
        task.propagateParentTaskTenantId();
        task.insert();
        operation = UserOperationLogEntry.OPERATION_TYPE_CREATE;
        task.executeMetrics(Metrics.ACTIVTY_INSTANCE_START, commandContext);
      } catch (NullValueException e) {
        throw new NotValidException(e.getMessage(), e);
      }

      task.fireAuthorizationProvider();
      task.transitionTo(TaskState.STATE_CREATED);
    } else {
      checkTaskAssign(task, commandContext);
      task.update();
      operation = UserOperationLogEntry.OPERATION_TYPE_UPDATE;

      task.fireAuthorizationProvider();
      task.triggerUpdateEvent();
    }

    task.executeMetrics(Metrics.UNIQUE_TASK_WORKERS, commandContext);
    task.logUserOperation(operation);

    return null;
  }

	protected void validateStandaloneTask(TaskEntity task, CommandContext commandContext) {
	  boolean standaloneTasksEnabled = commandContext.getProcessEngineConfiguration().isStandaloneTasksEnabled();
	  if (!standaloneTasksEnabled && task.isStandaloneTask()) {
      throw new NotAllowedException("Cannot save standalone task. They are disabled in the process engine configuration.");
	  }
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
