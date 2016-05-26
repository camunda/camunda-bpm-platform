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
package org.camunda.bpm.engine.test.api.task;

import static org.camunda.bpm.engine.task.Event.ACTION_ADD_ATTACHMENT;
import static org.camunda.bpm.engine.task.Event.ACTION_ADD_GROUP_LINK;
import static org.camunda.bpm.engine.task.Event.ACTION_ADD_USER_LINK;
import static org.camunda.bpm.engine.task.Event.ACTION_DELETE_ATTACHMENT;
import static org.camunda.bpm.engine.task.Event.ACTION_DELETE_GROUP_LINK;
import static org.camunda.bpm.engine.task.Event.ACTION_DELETE_USER_LINK;
import static org.camunda.bpm.engine.task.IdentityLinkType.CANDIDATE;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.history.useroperationlog.AbstractUserOperationLogTest;

/**
 * @author Daniel Meyer
 */
@SuppressWarnings("deprecation")
public class TaskEventsTest extends AbstractUserOperationLogTest {

  static String JONNY = "jonny";
  static String ACCOUNTING = "accounting";
  static String IMAGE_PNG = "application/png";
  static String IMAGE_NAME = "my-image.png";
  static String IMAGE_DESC = "a super duper image";
  static String IMAGE_URL = "file://some/location/my-image.png";

  private Task task;

  @Override
  public void setUp() throws Exception {
    task = taskService.newTask();
    taskService.saveTask(task);
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    // delete task
    taskService.deleteTask(task.getId(), true);
  }

  public void testAddUserLinkEvents() {

    // initially there are no task events
    assertTrue(taskService.getTaskEvents(task.getId()).isEmpty());

    taskService.addCandidateUser(task.getId(), JONNY);

    // now there is a task event created
    List<Event> events = taskService.getTaskEvents(task.getId());
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals(JONNY, event.getMessageParts().get(0));
    assertEquals(CANDIDATE, event.getMessageParts().get(1));
    assertEquals(task.getId(), event.getTaskId());
    assertEquals(ACTION_ADD_USER_LINK, event.getAction());
    assertEquals(JONNY + CommentEntity.MESSAGE_PARTS_MARKER + CANDIDATE, event.getMessage());
    assertEquals(null, event.getProcessInstanceId());
    assertNotNull(event.getTime().getTime() <= ClockUtil.getCurrentTime().getTime());

    assertNoCommentsForTask();
  }

  public void testDeleteUserLinkEvents() {

    // initially there are no task events
    assertTrue(taskService.getTaskEvents(task.getId()).isEmpty());

    taskService.addCandidateUser(task.getId(), JONNY);

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 5000));

    taskService.deleteCandidateUser(task.getId(), JONNY);

    // now there is a task event created
    List<Event> events = taskService.getTaskEvents(task.getId());
    assertEquals(2, events.size());
    Event event = events.get(0);
    assertEquals(JONNY, event.getMessageParts().get(0));
    assertEquals(CANDIDATE, event.getMessageParts().get(1));
    assertEquals(task.getId(), event.getTaskId());
    assertEquals(ACTION_DELETE_USER_LINK, event.getAction());
    assertEquals(JONNY + CommentEntity.MESSAGE_PARTS_MARKER + CANDIDATE, event.getMessage());
    assertEquals(null, event.getProcessInstanceId());
    assertNotNull(event.getTime().getTime() <= ClockUtil.getCurrentTime().getTime());

    assertNoCommentsForTask();
  }

  public void testAddGroupLinkEvents() {

    // initially there are no task events
    assertTrue(taskService.getTaskEvents(task.getId()).isEmpty());

    taskService.addCandidateGroup(task.getId(), ACCOUNTING);

    // now there is a task event created
    List<Event> events = taskService.getTaskEvents(task.getId());
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals(ACCOUNTING, event.getMessageParts().get(0));
    assertEquals(CANDIDATE, event.getMessageParts().get(1));
    assertEquals(task.getId(), event.getTaskId());
    assertEquals(ACTION_ADD_GROUP_LINK, event.getAction());
    assertEquals(ACCOUNTING + CommentEntity.MESSAGE_PARTS_MARKER + CANDIDATE, event.getMessage());
    assertEquals(null, event.getProcessInstanceId());
    assertNotNull(event.getTime().getTime() <= ClockUtil.getCurrentTime().getTime());

    assertNoCommentsForTask();
  }

  public void testDeleteGroupLinkEvents() {

    // initially there are no task events
    assertTrue(taskService.getTaskEvents(task.getId()).isEmpty());

    taskService.addCandidateGroup(task.getId(), ACCOUNTING);

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 5000));

    taskService.deleteCandidateGroup(task.getId(), ACCOUNTING);

    // now there is a task event created
    List<Event> events = taskService.getTaskEvents(task.getId());
    assertEquals(2, events.size());
    Event event = events.get(0);
    assertEquals(ACCOUNTING, event.getMessageParts().get(0));
    assertEquals(CANDIDATE, event.getMessageParts().get(1));
    assertEquals(task.getId(), event.getTaskId());
    assertEquals(ACTION_DELETE_GROUP_LINK, event.getAction());
    assertEquals(ACCOUNTING + CommentEntity.MESSAGE_PARTS_MARKER + CANDIDATE, event.getMessage());
    assertEquals(null, event.getProcessInstanceId());
    assertNotNull(event.getTime().getTime() <= ClockUtil.getCurrentTime().getTime());

    assertNoCommentsForTask();
  }

  public void testAddAttachmentEvents() {
    // initially there are no task events
    assertTrue(taskService.getTaskEvents(task.getId()).isEmpty());

    identityService.setAuthenticatedUserId(JONNY);
    taskService.createAttachment(IMAGE_PNG, task.getId(), null, IMAGE_NAME, IMAGE_DESC, IMAGE_URL);

    // now there is a task event created
    List<Event> events = taskService.getTaskEvents(task.getId());
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals(1, event.getMessageParts().size());
    assertEquals(IMAGE_NAME, event.getMessageParts().get(0));
    assertEquals(task.getId(), event.getTaskId());
    assertEquals(ACTION_ADD_ATTACHMENT, event.getAction());
    assertEquals(IMAGE_NAME, event.getMessage());
    assertEquals(null, event.getProcessInstanceId());
    assertNotNull(event.getTime().getTime() <= ClockUtil.getCurrentTime().getTime());

    assertNoCommentsForTask();
  }

  public void testDeleteAttachmentEvents() {
    // initially there are no task events
    assertTrue(taskService.getTaskEvents(task.getId()).isEmpty());

    identityService.setAuthenticatedUserId(JONNY);
    Attachment attachment = taskService.createAttachment(IMAGE_PNG, task.getId(), null, IMAGE_NAME, IMAGE_DESC, IMAGE_URL);

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 5000));

    taskService.deleteAttachment(attachment.getId());

    // now there is a task event created
    List<Event> events = taskService.getTaskEvents(task.getId());
    assertEquals(2, events.size());
    Event event = events.get(0);
    assertEquals(1, event.getMessageParts().size());
    assertEquals(IMAGE_NAME, event.getMessageParts().get(0));
    assertEquals(task.getId(), event.getTaskId());
    assertEquals(ACTION_DELETE_ATTACHMENT, event.getAction());
    assertEquals(IMAGE_NAME, event.getMessage());
    assertEquals(null, event.getProcessInstanceId());
    assertNotNull(event.getTime().getTime() <= ClockUtil.getCurrentTime().getTime());

    assertNoCommentsForTask();
  }


  private void assertNoCommentsForTask() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        assertTrue(commandContext.getCommentManager().findCommentsByTaskId(task.getId()).isEmpty());
        return null;
      }
    });
  }

}
