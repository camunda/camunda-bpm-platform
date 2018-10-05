/* Licensed under the Apache License, Version 2.0 (the "License");
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

import java.io.Serializable;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Event;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 */
public class AddCommentCmd implements Command<Comment>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String processInstanceId;
  protected String message;

  public AddCommentCmd(String taskId, String processInstanceId, String message) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.message = message;
  }

  public Comment execute(CommandContext commandContext) {

    if (processInstanceId == null && taskId == null) {
      throw new ProcessEngineException("Process instance id and task id is null");
    }

    ensureNotNull("Message", message);

    String userId = commandContext.getAuthenticatedUserId();
    CommentEntity comment = new CommentEntity();
    comment.setUserId(userId);
    comment.setType(CommentEntity.TYPE_COMMENT);
    comment.setTime(ClockUtil.getCurrentTime());
    comment.setTaskId(taskId);
    comment.setProcessInstanceId(processInstanceId);
    comment.setAction(Event.ACTION_ADD_COMMENT);

    ExecutionEntity execution = getExecution(commandContext, taskId, processInstanceId);
    if (execution != null) {
      comment.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    }

    String eventMessage = message.replaceAll("\\s+", " ");
    if (eventMessage.length() > 163) {
      eventMessage = eventMessage.substring(0, 160) + "...";
    }
    comment.setMessage(eventMessage);

    comment.setFullMessage(message);

    commandContext
      .getCommentManager()
      .insert(comment);

    return comment;
  }

  protected ExecutionEntity getExecution(CommandContext commandContext, String taskId, String processInstanceId) {
    ExecutionEntity execution = null;
    if (taskId != null) {
      TaskEntity task = commandContext.getTaskManager()
        .findTaskById(taskId);

      if (task != null) {
        execution = task.getExecution();
      }
    } else if (processInstanceId != null) {
      execution = commandContext.getExecutionManager()
        .findExecutionById(processInstanceId);
    }

    return execution;
  }

}
