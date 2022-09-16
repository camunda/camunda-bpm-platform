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
package org.camunda.bpm.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.GetIdentityLinksTaskListener;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.Rule;
import org.junit.Test;

import ch.qos.logback.classic.Level;



/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TaskIdentityLinksTest extends PluggableProcessEngineTest {

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().level(Level.ERROR);

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  @Test
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

    identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());

    taskService.deleteCandidateUser(taskId, "kermit");

    assertEquals(0, taskService.getIdentityLinksForTask(taskId).size());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  @Test
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

  @Test
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

    // second call should return the same list size
    identityLinks = taskService.getIdentityLinksForTask(task.getId());
    assertEquals(1, identityLinks.size());

    taskService.deleteTask(task.getId(), true);
  }

  @Test
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

    // second call should return the same list size
    identityLinks = taskService.getIdentityLinksForTask(task.getId());
    assertEquals(1, identityLinks.size());

    taskService.deleteTask(task.getId(), true);
  }

  private Event findTaskEvent(List<Event> taskEvents, String action) {
    for (Event event: taskEvents) {
      if (action.equals(event.getAction())) {
        return event;
      }
    }
    throw new AssertionError("no task event found with action "+action);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  @Test
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
  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testAssigneeGetIdentityLinksInCompleteListener() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("task1")
      .camundaTaskListenerClass(TaskListener.EVENTNAME_COMPLETE, GetIdentityLinksTaskListener.class.getName())
      .userTask("task2")
      .endEvent()
      .done();

    testRule.deploy(model);
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    Task task = taskService.createTaskQuery().singleResult();
    String taskId = task.getId();
    // create identity links
    task.setAssignee("elmo");
    taskService.saveTask(task);
    taskService.addUserIdentityLink(taskId, "kermit", "interestee");

    // when
    taskService.complete(taskId);

    // then no NPE is thrown and there were 2 identity links during the listener execution
    assertThat(loggingRule.getLog()).isEmpty();
    assertEquals(2, runtimeService.getVariable(processInstanceId, "identityLinksSize"));
    assertEquals(2, runtimeService.getVariable(processInstanceId, "secondCallidentityLinksSize"));
  }

  @Test
  public void testOwnerGetIdentityLinksInCompleteListener() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("task1")
      .camundaTaskListenerClass(TaskListener.EVENTNAME_COMPLETE, GetIdentityLinksTaskListener.class.getName())
      .userTask("task2")
      .endEvent()
      .done();

    testRule.deploy(model);
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    Task task = taskService.createTaskQuery().singleResult();
    String taskId = task.getId();
    // create identity links
    task.setOwner("gonzo");
    taskService.saveTask(task);
    taskService.addUserIdentityLink(taskId, "kermit", "interestee");

    // when
    taskService.complete(taskId);

    // then no NPE is thrown and there were 2 identity links during the listener execution
    assertThat(loggingRule.getLog()).isEmpty();
    assertEquals(2, runtimeService.getVariable(processInstanceId, "identityLinksSize"));
    assertEquals(2, runtimeService.getVariable(processInstanceId, "secondCallidentityLinksSize"));
  }
}
