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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.task.IdentityLinkType;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public abstract class DeleteIdentityLinkCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String userId;

  protected String groupId;

  protected String type;

  protected String taskId;

  protected TaskEntity task;

  public DeleteIdentityLinkCmd(String taskId, String userId, String groupId, String type) {
    validateParams(userId, groupId, type, taskId);
    this.taskId = taskId;
    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }

  protected void validateParams(String userId, String groupId, String type, String taskId) {
    ensureNotNull("taskId", taskId);
    ensureNotNull("type is required when adding a new task identity link", "type", type);

    // Special treatment for assignee and owner: group cannot be used and userId may be null
    if (IdentityLinkType.ASSIGNEE.equals(type) || IdentityLinkType.OWNER.equals(type)) {
      if (groupId != null) {
        throw new ProcessEngineException("Incompatible usage: cannot use type '" + type
          + "' together with a groupId");
      }
    } else {
      if (userId == null && groupId == null) {
        throw new ProcessEngineException("userId and groupId cannot both be null");
      }
    }
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskManager taskManager = commandContext.getTaskManager();
    task = taskManager.findTaskById(taskId);
    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    checkDeleteIdentityLink(task, commandContext);

    if (IdentityLinkType.ASSIGNEE.equals(type)) {
      task.setAssignee(null);
    } else if (IdentityLinkType.OWNER.equals(type)) {
      task.setOwner(null);
    } else {
      task.deleteIdentityLink(userId, groupId, type);
    }
    task.triggerUpdateEvent();

    return null;
  }

  protected void checkDeleteIdentityLink(TaskEntity task, CommandContext commandContext) {
     for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskAssign(task);
    }
  }

}
