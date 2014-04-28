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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.camunda.bpm.engine.rest.sub.task.TaskCommentResource;
import org.camunda.bpm.engine.task.Comment;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.camunda.bpm.engine.rest.mapper.MultipartFormData.FormPart;

public class TaskCommentResourceImpl implements TaskCommentResource {

  private ProcessEngine engine;
  private String taskId;
  private String rootResourcePath;

  public TaskCommentResourceImpl(ProcessEngine engine, String taskId, String rootResourcePath) {
    this.engine = engine;
    this.taskId = taskId;
    this.rootResourcePath = rootResourcePath;
  }

  public List<CommentDto> getComments() {
    if (!isHistoryEnabled()) {
      return Collections.emptyList();
    }

    ensureTaskExists(Status.NOT_FOUND);

    List<Comment> taskComments = engine.getTaskService().getTaskComments(taskId);

    List<CommentDto> comments = new ArrayList<CommentDto>();
    for (Comment comment : taskComments) {
      comments.add(CommentDto.fromComment(comment));
    }

    return comments;
  }

  public CommentDto getComment(String commentId) {
    ensureHistoryEnabled(Status.NOT_FOUND);

    Comment comment = engine.getTaskService().getTaskComment(taskId, commentId);
    if (comment == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Task comment with id " + commentId + " does not exist for task id '" + taskId + "'.");
    }

    return CommentDto.fromComment(comment);
  }

  public CommentDto createComment(UriInfo uriInfo, MultipartFormData payload) {
    ensureHistoryEnabled(Status.FORBIDDEN);
    ensureTaskExists(Status.BAD_REQUEST);

    FormPart messagePart = payload.getNamedPart("message");

    String message = null;
    if (messagePart != null) {
      message = messagePart.getTextContent();
    }

    Comment comment;

    try {
      comment = engine.getTaskService().addComment(taskId, null, message);
    }
    catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Not enough parameters submitted");
    }

    URI uri = uriInfo.getBaseUriBuilder()
      .path(rootResourcePath)
      .path(TaskRestService.class)
      .path(taskId + "/comment/" + comment.getId())
      .build();

    CommentDto commentDto = CommentDto.fromComment(comment);

    // GET /
    commentDto.addReflexiveLink(uri, HttpMethod.GET, "self");

    return commentDto;
  }

  private boolean isHistoryEnabled() {
    int historyLevel = engine.getManagementService().getHistoryLevel();
    return historyLevel > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE;
  }

  private void ensureHistoryEnabled(Status status) {
    if (!isHistoryEnabled()) {
      throw new InvalidRequestException(status, "History is not enabled");
    }
  }

  private void ensureTaskExists(Status status) {
    HistoricTaskInstance historicTaskInstance = engine.getHistoryService().createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
    if (historicTaskInstance == null) {
      throw new InvalidRequestException(status, "No task found for task id " + taskId);
    }
  }

}
