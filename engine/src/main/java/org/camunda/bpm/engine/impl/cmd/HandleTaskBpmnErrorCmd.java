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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * Command to handle a task BPMN error.
 */
public class HandleTaskBpmnErrorCmd implements Command<Void>, Serializable{

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String errorCode;
  protected String errorMessage;
  protected Map<String, Object> variables;


  public HandleTaskBpmnErrorCmd(String taskId, String errorCode) {
    this.taskId = taskId;
    this.errorCode = errorCode;
  }
  public HandleTaskBpmnErrorCmd(String taskId, String errorCode, String errorMessage) {
    this(taskId, errorCode);
    this.errorMessage = errorMessage;
  }
  public HandleTaskBpmnErrorCmd(String taskId, String errorCode, String errorMessage, Map<String, Object> variables) {
    this(taskId, errorCode, errorMessage);
    this.variables = variables;
  }

  protected void validateInput() {
    ensureNotEmpty(BadUserRequestException.class,"taskId", taskId);
    ensureNotEmpty(BadUserRequestException.class, "errorCode", errorCode);
  }

  @Override
  public Void execute(CommandContext commandContext) {
    validateInput();

    TaskEntity task = commandContext.getTaskManager().findTaskById(taskId);
    ensureNotNull(NotFoundException.class,"Cannot find task with id " + taskId, "task", task);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskWork(task);
    }

    task.bpmnError(errorCode, errorMessage, variables);

    return null;
  }
}
