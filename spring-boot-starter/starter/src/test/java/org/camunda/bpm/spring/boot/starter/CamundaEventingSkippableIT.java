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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.spring.boot.starter.event.TaskEvent;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * It's configurable if Spring eventing is treated as a skippable
 * execution listener. The default value is true, so this test
 * covers the case when the listener is treated as not skippable.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  webEnvironment = WebEnvironment.NONE
)
@ActiveProfiles("eventing-skippable")
public class CamundaEventingSkippableIT extends AbstractCamundaAutoConfigurationIT {

  public static final String SERVICE_TASK = "service_task";
  @Autowired
  private RuntimeService runtime;

  @Autowired
  private TaskService taskService;

  @Autowired
  private TestEventCaptor eventCaptor;

  private ProcessInstance instance;

  @Before
  public void init() {
    eventCaptor.clear();
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
  public final void shouldEventTaskDelete() {
    // given
    startEventingInstance();
    final Task task = taskService.createTaskQuery().active().singleResult();
    eventCaptor.clear();

    // when
    runtimeService.deleteProcessInstance(instance.getProcessInstanceId(), "no need", true);

    // then
    assertTaskEvents(task, TaskListener.EVENTNAME_DELETE);
  }

  @Test
  public final void shouldEventModificationWithSkipListeners() {
    // given
    startEventingInstance();
    final TaskEntity task = (TaskEntity)taskService.createTaskQuery().active().singleResult();
    eventCaptor.clear();

    // when
    runtimeService
        .createProcessInstanceModification(instance.getProcessInstanceId())
        .cancelAllForActivity(task.getTaskDefinitionKey())
        .startBeforeActivity(SERVICE_TASK, instance.getId())
        .execute(true, false);

    // then
    assertTaskEvents(task, TaskListener.EVENTNAME_DELETE);
    assertThat(eventCaptor.executionEvents).hasSize(7);
    assertThat(eventCaptor.executionEvents.get(0).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_END);
    assertThat(eventCaptor.executionEvents.get(0).getCurrentActivityId()).isEqualTo(task.getTaskDefinitionKey());
    assertThat(eventCaptor.executionEvents.get(1).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_START);
    assertThat(eventCaptor.executionEvents.get(1).getCurrentActivityId()).isEqualTo(SERVICE_TASK);
    assertThat(eventCaptor.executionEvents.get(2).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_END);
    assertThat(eventCaptor.executionEvents.get(2).getCurrentActivityId()).isEqualTo(SERVICE_TASK);
    assertThat(eventCaptor.executionEvents.get(3).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_TAKE);
    assertThat(eventCaptor.executionEvents.get(3).getCurrentActivityId()).isEqualTo(SERVICE_TASK);
    assertThat(eventCaptor.executionEvents.get(4).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_START);
    assertThat(eventCaptor.executionEvents.get(4).getCurrentActivityId()).startsWith("EndEvent");
    assertThat(eventCaptor.executionEvents.get(5).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_END);
    assertThat(eventCaptor.executionEvents.get(5).getCurrentActivityId()).startsWith("EndEvent");
    assertThat(eventCaptor.executionEvents.get(6).getEventName()).isEqualTo(ExecutionListener.EVENTNAME_END);
    assertThat(eventCaptor.executionEvents.get(6).getCurrentActivityId()).startsWith("EndEvent");
    assertThat(eventCaptor.executionEvents.get(6).getActivityInstanceId()).isEqualTo(instance.getProcessInstanceId());
  }


  protected void assertTaskEvents(final Task task, final String event) {
    assertTaskEvents(task, 1, event);
  }

  protected void assertTaskEvents(final Task task, final int numberOfEvents, final String...events) {
    assertThat(eventCaptor.taskEvents).hasSize(numberOfEvents);
    assertThat(eventCaptor.immutableTaskEvents).hasSize(numberOfEvents);
    assertThat(eventCaptor.transactionTaskEvents).hasSize(numberOfEvents);
    assertThat(eventCaptor.transactionImmutableTaskEvents).hasSize(numberOfEvents);

    for (int i = 0; i < numberOfEvents; i++) {
      /*
       * oldest event happened before latest does not work for mutable transaction
       * listener events since the delegate task was altered to assignment when the
       * event is dispatched to the listener
       */
      assertTaskEvent(task, eventCaptor.taskEvents.pop(), events[i]);
      assertTaskEvent(task, eventCaptor.immutableTaskEvents.pop(), events[i]);
      assertTaskEvent(task, eventCaptor.transactionTaskEvents.pop(), events[0]);
      assertTaskEvent(task, eventCaptor.transactionImmutableTaskEvents.pop(), events[i]);
    }
  }

  protected void assertTaskEvent(final Task task, final TaskEvent taskEvent, final String event) {
    assertThat(taskEvent.getEventName()).isEqualTo(event);
    assertThat(taskEvent.getId()).isEqualTo(task.getId());
    assertThat(taskEvent.getProcessInstanceId()).isEqualTo(task.getProcessInstanceId());
  }

  protected void startEventingInstance() {
    instance = runtime.startProcessInstanceByKey("eventing");
  }
}
