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
package org.camunda.bpm.spring.boot.starter;

import org.assertj.core.util.DateUtil;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestEventCaptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Date;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  webEnvironment = WebEnvironment.NONE
)
@ActiveProfiles("eventing")
@Transactional
public class CamundaEventingIT extends AbstractCamundaAutoConfigurationIT {

  @Autowired
  private RuntimeService runtime;

  @Autowired
  private TaskService taskService;

  @Autowired
  private TestEventCaptor eventCaptor;

  private ProcessInstance instance;

  @Before
  public void init() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("eventing")
      .singleResult();
    assertThat(processDefinition).isNotNull();

    eventCaptor.historyEvents.clear();
    instance = runtime.startProcessInstanceByKey("eventing");
  }

  @After
  public void stop() {
    if (instance != null) {
      // update stale instance
      instance = runtime.createProcessInstanceQuery().processInstanceId(instance.getProcessInstanceId()).active().singleResult();
      if (instance != null) {
        runtime.deleteProcessInstance(instance.getProcessInstanceId(), "eventing shutdown");
      }
    }
  }

  @Test
  public final void shouldEventTaskCreation() {

    assertThat(eventCaptor.taskEvents).isNotEmpty();

    Task task = taskService.createTaskQuery().active().singleResult();
    TestEventCaptor.TaskEvent taskEvent = eventCaptor.taskEvents.pop();

    assertThat(taskEvent.eventName).isEqualTo("create");
    assertThat(taskEvent.id).isEqualTo(task.getId());
    assertThat(taskEvent.processInstanceId).isEqualTo(task.getProcessInstanceId());
  }

  @Test
  public final void shouldEventTaskAssignment() {

    // given
    assertThat(eventCaptor.taskEvents).isNotEmpty();
    eventCaptor.taskEvents.clear();
    Task task = taskService.createTaskQuery().active().singleResult();

    // when
    taskService.setAssignee(task.getId(), "kermit");

    // then
    TestEventCaptor.TaskEvent taskEvent = eventCaptor.taskEvents.pop();
    assertThat(taskEvent.eventName).isEqualTo("assignment");
    assertThat(taskEvent.id).isEqualTo(task.getId());
    assertThat(taskEvent.processInstanceId).isEqualTo(task.getProcessInstanceId());
  }


  @Test
  public final void shouldEventTaskComplete() {

    // given
    assertThat(eventCaptor.taskEvents).isNotEmpty();
    eventCaptor.taskEvents.clear();
    Task task = taskService.createTaskQuery().active().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    TestEventCaptor.TaskEvent taskEvent = eventCaptor.taskEvents.pop();
    assertThat(taskEvent.eventName).isEqualTo("complete");
    assertThat(taskEvent.id).isEqualTo(task.getId());
    assertThat(taskEvent.processInstanceId).isEqualTo(task.getProcessInstanceId());
  }

  @Test
  public final void shouldEventTaskDelete() {

    // given
    assertThat(eventCaptor.taskEvents).isNotEmpty();
    eventCaptor.taskEvents.clear();
    Task task = taskService.createTaskQuery().active().singleResult();

    // when
    runtimeService.deleteProcessInstance(instance.getProcessInstanceId(), "no need");

    // then
    TestEventCaptor.TaskEvent taskEvent = eventCaptor.taskEvents.pop();
    assertThat(taskEvent.eventName).isEqualTo("delete");
    assertThat(taskEvent.id).isEqualTo(task.getId());
    assertThat(taskEvent.processInstanceId).isEqualTo(task.getProcessInstanceId());
  }

  @Test
  public final void shouldEventExecution() {
    // given
    assertThat(eventCaptor.executionEvents).isNotEmpty();
    eventCaptor.executionEvents.clear();
    Task task = taskService.createTaskQuery().active().singleResult();

    // when
    taskService.complete(task.getId());

    // then 7
    // 2 for user task (take, end)
    // 3 for service task (start, take, end)
    // 2 for end event (start, end)
    // 1 for process (end)
    assertThat(eventCaptor.executionEvents.size()).isEqualTo(2 + 3 + 2 + 1);
  }

  @Test
  public final void shouldEventHistoryTaskAssignmentChanges() {
    // given
    assertThat(eventCaptor.historyEvents).isNotEmpty();
    eventCaptor.historyEvents.clear();
    assertThat(eventCaptor.historyEvents).isEmpty();

    Task task = taskService.createTaskQuery().active().singleResult();

    // when
    taskService.addCandidateUser(task.getId(), "userId");
    taskService.addCandidateGroup(task.getId(), "groupId");
    taskService.deleteCandidateUser(task.getId(), "userId");
    taskService.deleteCandidateGroup(task.getId(), "groupId");

    // then in reverse order

    // Remove candidate group
    HistoryEvent candidateGroupEvent = eventCaptor.historyEvents.pop();
    assertThat(candidateGroupEvent.getEventType()).isEqualTo("delete-identity-link");
    if (candidateGroupEvent instanceof HistoricIdentityLinkLogEventEntity) {
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateGroupEvent).getType()).isEqualTo("candidate");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateGroupEvent).getOperationType()).isEqualTo("delete");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateGroupEvent).getGroupId()).isEqualTo("groupId");
    } else {
      fail("Expected identity link log event");
    }


    // Remove candidate user
    HistoryEvent candidateUserEvent = eventCaptor.historyEvents.pop();
    assertThat(candidateUserEvent.getEventType()).isEqualTo("delete-identity-link");
    if (candidateUserEvent instanceof HistoricIdentityLinkLogEventEntity) {
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getType()).isEqualTo("candidate");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getOperationType()).isEqualTo("delete");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getUserId()).isEqualTo("userId");
    } else {
      fail("Expected identity link log event");
    }

    // Add candidate group
    candidateGroupEvent = eventCaptor.historyEvents.pop();
    assertThat(candidateGroupEvent.getEventType()).isEqualTo("add-identity-link");
    if (candidateGroupEvent instanceof HistoricIdentityLinkLogEventEntity) {
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateGroupEvent).getType()).isEqualTo("candidate");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateGroupEvent).getOperationType()).isEqualTo("add");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateGroupEvent).getGroupId()).isEqualTo("groupId");
    } else {
      fail("Expected identity link log event");
    }

    // Add candidate user
    candidateUserEvent = eventCaptor.historyEvents.pop();
    assertThat(candidateUserEvent.getEventType()).isEqualTo("add-identity-link");
    if (candidateUserEvent instanceof HistoricIdentityLinkLogEventEntity) {
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getType()).isEqualTo("candidate");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getOperationType()).isEqualTo("add");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getUserId()).isEqualTo("userId");
    } else {
      fail("Expected identity link log event");
    }

    assertThat(eventCaptor.historyEvents).isEmpty();
  }


  @Test
  public void shouldEventHistoryTaskAttributeChanges() {
    assertThat(eventCaptor.historyEvents).isNotEmpty();
    eventCaptor.historyEvents.clear();

    Task task = taskService.createTaskQuery().active().singleResult();

    task.setName("new Name");
    taskService.saveTask(task);


    HistoryEvent taskChangeEvent = eventCaptor.historyEvents.pop();
    assertThat(taskChangeEvent.getEventType()).isEqualTo("update");
    if (taskChangeEvent instanceof HistoricTaskInstanceEventEntity) {
      assertThat(((HistoricTaskInstanceEventEntity) taskChangeEvent).getName()).isEqualTo("new Name");
    } else {
      fail("Expected task instance change event");
    }

  }

  @Test
  public void shouldEventHistoryTaskMultipleAssignmentChanges() {

    // given
    assertThat(eventCaptor.historyEvents).isNotEmpty();
    eventCaptor.historyEvents.clear();
    assertThat(eventCaptor.historyEvents).isEmpty();

    Task task = taskService.createTaskQuery().active().singleResult();

    // when
    taskService.addCandidateUser(task.getId(), "user1");
    taskService.addCandidateUser(task.getId(), "user2");

    // then in reverse order

    // Add candidate user
    HistoryEvent candidateUserEvent = eventCaptor.historyEvents.pop();
    assertThat(candidateUserEvent.getEventType()).isEqualTo("add-identity-link");
    if (candidateUserEvent instanceof HistoricIdentityLinkLogEventEntity) {
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getType()).isEqualTo("candidate");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getOperationType()).isEqualTo("add");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getUserId()).isEqualTo("user2");
    } else {
      fail("Expected identity link log event");
    }

    // Add candidate user
    candidateUserEvent = eventCaptor.historyEvents.pop();
    assertThat(candidateUserEvent.getEventType()).isEqualTo("add-identity-link");
    if (candidateUserEvent instanceof HistoricIdentityLinkLogEventEntity) {
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getType()).isEqualTo("candidate");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getOperationType()).isEqualTo("add");
      assertThat(((HistoricIdentityLinkLogEventEntity) candidateUserEvent).getUserId()).isEqualTo("user1");
    } else {
      fail("Expected identity link log event");
    }

    assertThat(eventCaptor.historyEvents).isEmpty();
  }

  @Test
  public void shouldEventHistoryTaskFollowUpDateChanges() {
    assertThat(eventCaptor.historyEvents).isNotEmpty();
    eventCaptor.historyEvents.clear();

    Task task = taskService.createTaskQuery().active().singleResult();

    Date now = DateUtil.now();

    task.setFollowUpDate(now);
    taskService.saveTask(task);

    HistoryEvent taskChangeEvent = eventCaptor.historyEvents.pop();
    assertThat(taskChangeEvent.getEventType()).isEqualTo("update");
    if (taskChangeEvent instanceof HistoricTaskInstanceEventEntity) {
      assertThat(((HistoricTaskInstanceEventEntity) taskChangeEvent).getFollowUpDate()).isEqualTo(now);
    } else {
      fail("Expected task instance change event");
    }
  }
}
