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
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;

/**
 * Abstract command class, meant to encapsulate boilerplate logic for concrete commands that wish to set a property
 * on a task and inherit authorization, multi-tenancy
 *
 * @param <T> the type of the value to set by this command
 */
public abstract class AbstractSetTaskPropertyCmd<T> implements Command<Void>, Serializable {

  protected final String taskId;
  protected final T value;

  /**
   * Constructor of Commands that wish to set a property to a given task.
   *
   * @param taskId the id of the task whose property should be changed
   * @param value  the new value to set to the referenced task
   * @throws NullValueException in case the given taskId or the given value are null
   * @throws NotFoundException  in case the referenced task does not exist
   */
  public AbstractSetTaskPropertyCmd(String taskId, T value) {
    this(taskId, value, false);
  }

  /**
   * Constructor with parameterized validation for the given input. Used by implementations that wish to
   * avoid validation e.g {@link SetTaskPriorityCmd}.
   *
   * @param taskId                 the id of the task whose property should be changed
   * @param value                  the new value to set to the referenced task
   * @param skipValueValidation if true, the validation of the value will be excluded
   */
  protected AbstractSetTaskPropertyCmd(String taskId, T value, boolean skipValueValidation) {
    this.taskId = ensureNotNullAndGet("taskId", taskId);
    this.value = skipValueValidation ? value : ensureNotNullAndGet("value", value);
  }

  /**
   * Executes this command against the command context to perform the set-operation.
   *
   * @param context the command context
   * @throws NotFoundException in case the referenced task does not exist
   */
  @Override
  public Void execute(CommandContext context) {
    TaskEntity task = validateAndGet(taskId, context);

    executeSetOperation(task, value);

    task.triggerUpdateEvent();
    logOperation(context, task);

    return null;
  }

  protected void logOperation(CommandContext context, TaskEntity task) {
    task.logUserOperation(getUserOperationLogName());
  }

  /**
   * Validates the given taskId against to verify it references an existing task before returning the task.
   *
   * @param taskId  the given taskId, non-null
   * @param context the context, non-null
   * @return the corresponding task entity
   */
  protected TaskEntity validateAndGet(String taskId, CommandContext context) {
    TaskManager taskManager = context.getTaskManager();
    TaskEntity task = taskManager.findTaskById(taskId);

    ensureNotNull(NotFoundException.class, "Cannot find task with id " + taskId, "task", task);

    checkTaskAgainstContext(task, context);

    return task;
  }

  /**
   * Perform multi-tenancy & authorization checks on the given task against the given command context.
   *
   * @param task    the given task
   * @param context the given command context to check against
   */
  protected void checkTaskAgainstContext(TaskEntity task, CommandContext context) {
    for (CommandChecker checker : context.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskAssign(task);
    }
  }

  /**
   * Returns the User Operation Log name that corresponds to this command. Meant to be implemented by concretions.
   *
   * @return the user operation log name
   */
  protected abstract String getUserOperationLogName();

  /**
   * Executes the set operation of the concrete command.
   *
   * @param task  the task entity on which to set a property
   * @param value the value to se
   */
  protected abstract void executeSetOperation(TaskEntity task, T value);

  /**
   * Ensures the value is not null and returns the value.
   *
   * @param value the value
   * @param <T>   the type of the value
   * @return the value
   * @throws NullValueException in case the given value is null
   */
  protected <T> T ensureNotNullAndGet(String variableName, T value) {
    ensureNotNull(variableName, value);
    return value;
  }

}