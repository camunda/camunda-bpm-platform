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
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * Command to update a comment by a given task ID or a process Instance ID
 */
public class UpdateCommentCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String commentId;
  protected String processInstanceId;
  protected String message;

  public UpdateCommentCmd(String taskId, String processInstanceId, String commentId, String message) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.commentId = commentId;
    this.message = message;
  }

  public Object execute(CommandContext commandContext) {
    if (processInstanceId == null && taskId == null) {
      throw new ProcessEngineException("Both process instance and task ids are null");
    }

    ensureNotNull("commentId", commentId);
    ensureNotNull("message", message);

    if(null == processInstanceId) {
      ensureNotNull("taskId", taskId);
      CommentEntity comment = getComment(commandContext);
      ensureNotNull("No comment exists with commentId: " + commentId + " and taskId: " + taskId, "comment", comment);
      TaskEntity task = updateComment(taskId, commandContext, comment);
      commandContext.getOperationLogManager().logCommentOperation(UserOperationLogEntry.OPERATION_TYPE_UPDATE_COMMENT, task, getPropertyChange(comment));
      task.triggerUpdateEvent();
    }
    else {
      ensureNotNull("processInstanceId", processInstanceId);
      CommentEntity comment = getComment(commandContext);
      ensureNotNull("No comment exists with commentId: " + commentId + " and processInstanceId: " + processInstanceId, "comment", comment);
      ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
      ensureNotNull("No processInstance exists with processInstanceId: " + processInstanceId, "processInstance", processInstance);
      updateComment(comment.getTaskId(), commandContext, comment);
      commandContext.getOperationLogManager().logCommentOperation(UserOperationLogEntry.OPERATION_TYPE_UPDATE_COMMENT, processInstance, getPropertyChange(comment));
    }

    return null;
  }

  private TaskEntity updateComment(String taskId, CommandContext commandContext, CommentEntity comment) {
    TaskEntity task = commandContext.getTaskManager().findTaskById(taskId);
    ensureNotNull("No task exists with taskId: " + taskId, "task", task);

    checkTaskWork(task, commandContext);
    updateComment(commandContext, comment);
    return  task;
  }

  private CommentEntity getComment(CommandContext commandContext) {
    if(taskId !=null) {
      return commandContext.getCommentManager().findCommentByTaskIdAndCommentId(taskId, commentId);
    }
    return commandContext.getCommentManager().findCommentByProcessInstanceIdAndCommentId(processInstanceId, commentId);
  }

  private PropertyChange getPropertyChange(CommentEntity comment) {
    return new PropertyChange("comment", comment.getMessage(), message);
  }

  protected void checkTaskWork(TaskEntity task, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkTaskWork(task);
    }
  }

  private void updateComment(CommandContext commandContext, CommentEntity comment) {
    String eventMessage = comment.toEventMessage(message);

    String userId = commandContext.getAuthenticatedUserId();

    comment.setMessage(eventMessage);
    comment.setFullMessage(message);
    comment.setTime(ClockUtil.getCurrentTime());
    comment.setAction(UserOperationLogEntry.OPERATION_TYPE_UPDATE_COMMENT);
    comment.setUserId(userId);

    commandContext.getDbEntityManager().update(CommentEntity.class, "updateComment", comment);
  }
}
