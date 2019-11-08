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

import org.camunda.bpm.engine.TaskAlreadyClaimedException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;


/**
 * @author Joram Barrez
 */
public class ClaimTaskCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String userId;

  public ClaimTaskCmd(String taskId, String userId) {
    this.taskId = taskId;
    this.userId = userId;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskManager taskManager = commandContext.getTaskManager();
    TaskEntity task = taskManager.findTaskById(taskId);
    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    checkClaimTask(task, commandContext);

    if (userId != null) {
      if (task.getAssignee() != null) {
        if (!task.getAssignee().equals(userId)) {
          // When the task is already claimed by another user, throw exception. Otherwise, ignore
          // this, post-conditions of method already met.
          throw new TaskAlreadyClaimedException(task.getId(), task.getAssignee());
        }
      } else {
        task.setAssignee(userId);
      }
    } else {
      // Task should be assigned to no one
      task.setAssignee(null);
    }
    task.triggerUpdateEvent();
    task.logUserOperation(UserOperationLogEntry.OPERATION_TYPE_CLAIM);

    return null;
  }

  protected void checkClaimTask(TaskEntity task, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskWork(task);
    }
  }
}
