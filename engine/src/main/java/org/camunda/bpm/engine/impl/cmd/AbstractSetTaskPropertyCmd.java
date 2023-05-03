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

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;

public abstract class AbstractSetTaskPropertyCmd<T> implements Command<Void>, Serializable {

  private final String taskId;
  private final T value;

  public AbstractSetTaskPropertyCmd(String taskId, T value) {
    this.taskId = requireNonNull(taskId);
    this.value = requireNonNull(value);
  }

  @Override
  public Void execute(CommandContext context) {
    TaskEntity task = validateAndGet(taskId, context);

    executeSetOperation(task, value);

    task.triggerUpdateEvent();
    task.logUserOperation(getUserOperationLogName());

    return null;
  }

  protected TaskEntity validateAndGet(String taskId, CommandContext context) {
    TaskManager taskManager = context.getTaskManager();
    TaskEntity task = requireNonNull(taskManager.findTaskById(taskId), "Cannot find task with id " + taskId);

    checkTaskPriority(task, context);

    return task;
  }

  protected void checkTaskPriority(TaskEntity task, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskAssign(task);
    }
  }

  protected abstract String getUserOperationLogName();

  protected abstract void executeSetOperation(TaskEntity task, T value);

}