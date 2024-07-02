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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.task.Comment;
import org.junit.Test;

public class TaskCommentAuthorizationTest extends AuthorizationTest {

  @Test
  public void testDeleteTaskCommentWithoutAuthorization() {
    // given
    createTask(TASK_ID);
    Comment createdComment = createComment(TASK_ID, null, "aComment");

    try {
      // when
      taskService.deleteTaskComment(TASK_ID, createdComment.getId());
      fail("Exception expected: It should not be possible to delete a comment.");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent(
          "The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'",
          e.getMessage());
    }

    // triggers a db clean up
    deleteTask(TASK_ID, true);
  }

  @Test
  public void testDeleteTaskComment() {
    // given
    createTask(TASK_ID);
    Comment createdComment = taskService.createComment(TASK_ID, null, "aComment");
    createGrantAuthorization(TASK, TASK_ID, userId, UPDATE);

    // when
    taskService.deleteTaskComment(TASK_ID, createdComment.getId());

    // then
    Comment shouldBeDeleletedComment = taskService.getTaskComment(TASK_ID, createdComment.getId());
    assertNull(shouldBeDeleletedComment);

    // triggers a db clean up
    deleteTask(TASK_ID, true);
  }

  @Test
  public void testDeleteTaskCommentsWithoutAuthorization() {
    // given
    createTask(TASK_ID);
    createComment(TASK_ID, null, "aComment");

    try {
      // when
      taskService.deleteTaskComments(TASK_ID);
      fail("Exception expected: It should not be possible to delete a comment.");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent(
          "The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'",
          e.getMessage());
    }

    // triggers a db clean up
    deleteTask(TASK_ID, true);
  }

  @Test
  public void testDeleteTaskComments() {
    // given
    createTask(TASK_ID);
    taskService.createComment(TASK_ID, null, "aCommentOne");
    taskService.createComment(TASK_ID, null, "aCommentTwo");

    createGrantAuthorization(TASK, TASK_ID, userId, UPDATE);

    // when
    taskService.deleteTaskComments(TASK_ID);

    // then
    List<Comment> comments = taskService.getTaskComments(TASK_ID);
    assertEquals("The comments list should be empty", Collections.emptyList(), comments);

    // triggers a db clean up
    deleteTask(TASK_ID, true);
  }

  @Test
  public void testUpdateTaskCommentWithoutAuthorization() {
    // given
    createTask(TASK_ID);
    Comment createdComment = createComment(TASK_ID, null, "originalComment");

    try {
      // when
      taskService.updateTaskComment(TASK_ID, createdComment.getId(), "updateMessage");
      fail("Exception expected: It should not be possible to delete a comment.");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent(
          "The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'",
          e.getMessage());
    }

    // triggers a db clean up
    deleteTask(TASK_ID, true);
  }

  @Test
  public void testUpdateTaskComment() {
    // given
    createTask(TASK_ID);
    String commentMessage = "OriginalCommentMessage";
    String updatedMessage = "UpdatedCommentMessage";
    Comment comment = taskService.createComment(TASK_ID, null, commentMessage);
    createGrantAuthorization(TASK, TASK_ID, userId, UPDATE);

    // when
    taskService.updateTaskComment(TASK_ID, comment.getId(), updatedMessage);

    // then
    List<Comment> comments = taskService.getTaskComments(TASK_ID);
    assertFalse("The comments list should not be empty", comments.isEmpty());
    assertEquals(updatedMessage, comments.get(0).getFullMessage());

    // triggers a db clean up
    deleteTask(TASK_ID, true);
  }

}
