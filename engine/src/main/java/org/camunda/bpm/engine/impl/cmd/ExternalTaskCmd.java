/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Represents a base class for the external task commands.
 * Contains functionality to get the external task by id and check
 * the authorization for the execution of a command on the requested external task.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public abstract class ExternalTaskCmd implements Command<Void> {

  /**
   * The corresponding external task id.
   */
  protected String externalTaskId;

  public ExternalTaskCmd(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull("externalTaskId", externalTaskId);
    validateInput();

    ExternalTaskEntity externalTask = commandContext.getExternalTaskManager().findExternalTaskById(externalTaskId);
    ensureNotNull(NotFoundException.class,
        "Cannot find external task with id " + externalTaskId, "externalTask", externalTask);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstanceById(externalTask.getProcessInstanceId());
    }

    execute(externalTask);

    return null;
  }

  /**
   * Executes the specific external task commands, which belongs to the current sub class.
   *
   * @param externalTask the external task which is used for the command execution
   */
  protected abstract void execute(ExternalTaskEntity externalTask);

  /**
   * Validates the current input of the command.
   */
  protected abstract void validateInput();

}
