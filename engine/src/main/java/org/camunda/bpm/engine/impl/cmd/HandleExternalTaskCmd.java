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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Represents an abstract class for the handle of external task commands.
 * 
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public abstract class HandleExternalTaskCmd extends ExternalTaskCmd {
  
  /**
   * The reported worker id.
   */
  protected String workerId;
  
  public HandleExternalTaskCmd(String externalTaskId, String workerId) {
    super(externalTaskId);
    this.workerId = workerId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    validateInput();    

    ExternalTaskEntity externalTask = commandContext.getExternalTaskManager().findExternalTaskById(externalTaskId);
    EnsureUtil.ensureNotNull(NotFoundException.class,
        "Cannot find external task with id " + externalTaskId, "externalTask", externalTask);

    if (!workerId.equals(externalTask.getWorkerId())) {      
      throw new BadUserRequestException(getErrorMessageOnWrongWorkerAccess() + "'. It is locked by worker '" + externalTask.getWorkerId() + "'.");
    }

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstanceById(externalTask.getProcessInstanceId());
    }
    
    execute(externalTask);
    
    return null;
  }
  
  /**
   * Returns the error message. Which is used to create an specific message
   *  for the BadUserRequestException if an worker has no rights to execute commands of the external task.
   * 
   * @return the specific error message
   */
  public abstract String getErrorMessageOnWrongWorkerAccess();
    
  /**
   * Validates the current input of the command.
   */
  @Override
  protected void validateInput() {
    EnsureUtil.ensureNotNull("workerId", workerId);
  }
}
