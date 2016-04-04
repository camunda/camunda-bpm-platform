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
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExternalTaskActivityBehavior;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Command to handle an external task BPMN error.
 * 
 * @author Christopher Zell
 */
public class HandleExternalTaskBpmnErrorCmd implements Command<Void> {

  protected String externalTaskId;
  protected String workerId;
  protected String errorCode;
  
  public HandleExternalTaskBpmnErrorCmd(String externalTaskId, String workerId, String errorCode) {
    this.externalTaskId = externalTaskId;
    this.workerId = workerId;    
    this.errorCode = errorCode;
  }
  
  @Override
  public Void execute(CommandContext commandContext) throws Exception  {
    validateInput();

    ExternalTaskEntity externalTask = commandContext.getExternalTaskManager().findExternalTaskById(externalTaskId);
    EnsureUtil.ensureNotNull(NotFoundException.class,
        "Cannot find external task with id " + externalTaskId, "externalTask", externalTask);

    if (!workerId.equals(externalTask.getWorkerId())) {
      throw new BadUserRequestException("Failure of External Task " + externalTaskId + " cannot be reported by worker '" + workerId
          + "'. It is locked by worker '" + externalTask.getWorkerId() + "'.");
    }    
        
    ActivityExecution activityExecution = externalTask.getExecution();
    BpmnError bpmnError = new BpmnError(errorCode);
    ( (ExternalTaskActivityBehavior) activityExecution.getActivity().getActivityBehavior()).propagateBpmnError(bpmnError, activityExecution);
    return null;
  }
  
  /**
   * Validates the current input of the command.
   */
  protected void validateInput() {
    EnsureUtil.ensureNotNull("externalTaskId", externalTaskId);
    EnsureUtil.ensureNotNull("workerId", workerId);
    EnsureUtil.ensureNotNull("errorCode", errorCode);
  }
}
