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
package org.camunda.bpm.engine.rest.sub.task.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.task.TaskCommentResource;
import org.camunda.bpm.engine.task.Comment;

public class TaskCommentResourceImpl implements TaskCommentResource {

  private ProcessEngine engine;
  private String taskId;
  private String commentId;

  public TaskCommentResourceImpl(ProcessEngine engine, String taskId, String commentId) {
    this.engine = engine;
    this.taskId = taskId;
    this.commentId = commentId;
  }

  @Override
  public CommentDto getComment() {
    Comment comment = engine.getTaskService().getTaskComment(taskId, commentId);
    if (comment == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Task comment with id " + commentId + " does not exist for task id '" + taskId + "'.");
    }

    return CommentDto.fromComment(comment);
  }

}
