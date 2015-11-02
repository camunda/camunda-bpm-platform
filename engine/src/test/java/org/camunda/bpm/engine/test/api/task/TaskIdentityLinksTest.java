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

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

import junit.framework.AssertionFailedError;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TaskIdentityLinksTest extends PluggableProcessEngineTestCase {

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();

    taskService.addCandidateUser(taskId, "kermit");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertNull(identityLink.getGroupId());
    assertEquals("kermit", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());

    assertEquals(1, identityLinks.size());

    taskService.deleteCandidateUser(taskId, "kermit");

    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateGroupLink() {
    try {
      identityService.setAuthenticatedUserId("demo");

      runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

      String taskId = taskService
          .createTaskQuery()
          .singleResult()
          .getId();

      taskService.addCandidateGroup(taskId, "muppets");

      List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
      IdentityLink identityLink = identityLinks.get(0);

      assertEquals("muppets", identityLink.getGroupId());
      assertNull("kermit", identityLink.getUserId());
      assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
      assertEquals(taskId, identityLink.getTaskId());

      assertEquals(1, identityLinks.size());

      if (processEngineConfiguration.getHistoryLevel().getId()>= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
        List<Event> taskEvents = taskService.getTaskEvents(taskId);
        assertEquals(1, taskEvents.size());
        Event taskEvent = taskEvents.get(0);
        assertEquals(Event.ACTION_ADD_GROUP_LINK, taskEvent.getAction());
        List<String> taskEventMessageParts = taskEvent.getMessageParts();
        assertEquals("muppets", taskEventMessageParts.get(0));
        assertEquals(IdentityLinkType.CANDIDATE, taskEventMessageParts.get(1));
        assertEquals(2, taskEventMessageParts.size());
      }

      taskService.deleteCandidateGroup(taskId, "muppets");

      if (processEngineConfiguration.getHistoryLevel().getId()>= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
        List<Event> taskEvents = taskService.getTaskEvents(taskId);
        Event taskEvent = findTaskEvent(taskEvents, Event.ACTION_DELETE_GROUP_LINK);
        assertEquals(Event.ACTION_DELETE_GROUP_LINK, taskEvent.getAction());
        List<String> taskEventMessageParts = taskEvent.getMessageParts();
        assertEquals("muppets", taskEventMessageParts.get(0));
        assertEquals(IdentityLinkType.CANDIDATE, taskEventMessageParts.get(1));
        assertEquals(2, taskEventMessageParts.size());
        assertEquals(2, taskEvents.size());
      }

      assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
    } finally {
      identityService.clearAuthentication();
    }
  }

  public void testAssigneeLink() {
    Task task = taskService.newTask("task");
    task.setAssignee("assignee");
    taskService.saveTask(task);

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
    assertNotNull(identityLinks);
    assertEquals(1, identityLinks.size());

    IdentityLink identityLink = identityLinks.get(0);
    assertEquals(IdentityLinkType.ASSIGNEE, identityLink.getType());
    assertEquals("assignee", identityLink.getUserId());
    assertEquals("task", identityLink.getTaskId());

    taskService.deleteTask(task.getId(), true);
  }

  public void testOwnerLink() {
    Task task = taskService.newTask("task");
    task.setOwner("owner");
    taskService.saveTask(task);

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
    assertNotNull(identityLinks);
    assertEquals(1, identityLinks.size());

    IdentityLink identityLink = identityLinks.get(0);
    assertEquals(IdentityLinkType.OWNER, identityLink.getType());
    assertEquals("owner", identityLink.getUserId());
    assertEquals("task", identityLink.getTaskId());

    taskService.deleteTask(task.getId(), true);
  }

  private Event findTaskEvent(List<Event> taskEvents, String action) {
    for (Event event: taskEvents) {
      if (action.equals(event.getAction())) {
        return event;
      }
    }
    throw new AssertionFailedError("no task event found with action "+action);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCustomTypeUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();

    taskService.addUserIdentityLink(taskId, "kermit", "interestee");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertNull(identityLink.getGroupId());
    assertEquals("kermit", identityLink.getUserId());
    assertEquals("interestee", identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());

    assertEquals(1, identityLinks.size());

    taskService.deleteUserIdentityLink(taskId, "kermit", "interestee");

    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCustomLinkGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService
      .createTaskQuery()
      .singleResult()
      .getId();

    taskService.addGroupIdentityLink(taskId, "muppets", "playing");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertEquals("muppets", identityLink.getGroupId());
    assertNull("kermit", identityLink.getUserId());
    assertEquals("playing", identityLink.getType());
    assertEquals(taskId, identityLink.getTaskId());

    assertEquals(1, identityLinks.size());

    taskService.deleteGroupIdentityLink(taskId, "muppets", "playing");

    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  public void testDeleteAssignee() {
    Task task = taskService.newTask();
    task.setAssignee("nonExistingUser");
    taskService.saveTask(task);

    taskService.deleteUserIdentityLink(task.getId(), "nonExistingUser", IdentityLinkType.ASSIGNEE);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertNull(task.getAssignee());
    assertEquals(0, taskService.getIdentityLinksForTask(task.getId()).size());

    // cleanup
    taskService.deleteTask(task.getId(), true);
  }

  public void testDeleteOwner() {
    Task task = taskService.newTask();
    task.setOwner("nonExistingUser");
    taskService.saveTask(task);

    taskService.deleteUserIdentityLink(task.getId(), "nonExistingUser", IdentityLinkType.OWNER);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertNull(task.getOwner());
    assertEquals(0, taskService.getIdentityLinksForTask(task.getId()).size());

    // cleanup
    taskService.deleteTask(task.getId(), true);
  }

}
