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
package org.camunda.bpm.engine.test.bpmn.subprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 */
public class InterruptingEventSubProcessTest extends PluggableProcessEngineTest {

  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/subprocess/InterruptingEventSubProcessTest.testCancelEventSubscriptions.bpmn")
  @Test
  public void testCancelEventSubscriptionsWhenReceivingAMessage() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    TaskQuery taskQuery = taskService.createTaskQuery();
    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery();

    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskBeforeInterruptingEventSuprocess", task.getTaskDefinitionKey());

    List<EventSubscription> eventSubscriptions = eventSubscriptionQuery.list();
    assertEquals(2, eventSubscriptions.size());

    runtimeService.messageEventReceived("newMessage", pi.getId());

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskAfterMessageStartEvent", task.getTaskDefinitionKey());

    assertEquals(0, eventSubscriptionQuery.count());

    try {
      runtimeService.signalEventReceived("newSignal", pi.getId());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // expected exception;
    }

    taskService.complete(task.getId());

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/subprocess/InterruptingEventSubProcessTest.testCancelEventSubscriptions.bpmn")
  @Test
  public void testCancelEventSubscriptionsWhenReceivingASignal() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    TaskQuery taskQuery = taskService.createTaskQuery();
    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery();

    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskBeforeInterruptingEventSuprocess", task.getTaskDefinitionKey());

    List<EventSubscription> eventSubscriptions = eventSubscriptionQuery.list();
    assertEquals(2, eventSubscriptions.size());

    runtimeService.signalEventReceived("newSignal", pi.getId());

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("tastAfterSignalStartEvent", task.getTaskDefinitionKey());

    assertEquals(0, eventSubscriptionQuery.count());

    try {
      runtimeService.messageEventReceived("newMessage", pi.getId());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // expected exception;
    }

    taskService.complete(task.getId());

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testCancelTimer() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    TaskQuery taskQuery = taskService.createTaskQuery();
    JobQuery jobQuery = managementService.createJobQuery().timers();

    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskBeforeInterruptingEventSuprocess", task.getTaskDefinitionKey());

    Job timer = jobQuery.singleResult();
    assertNotNull(timer);

    runtimeService.messageEventReceived("newMessage", pi.getId());

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskAfterMessageStartEvent", task.getTaskDefinitionKey());

    assertEquals(0, jobQuery.count());

    taskService.complete(task.getId());

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testKeepCompensation() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    TaskQuery taskQuery = taskService.createTaskQuery();
    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery();

    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskBeforeInterruptingEventSuprocess", task.getTaskDefinitionKey());

    List<EventSubscription> eventSubscriptions = eventSubscriptionQuery.list();
    assertEquals(2, eventSubscriptions.size());

    runtimeService.messageEventReceived("newMessage", pi.getId());

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskAfterMessageStartEvent", task.getTaskDefinitionKey());

    assertEquals(1, eventSubscriptionQuery.count());

    taskService.complete(task.getId());

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testTimeCycle() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery();
    assertEquals(0, eventSubscriptionQuery.count());

    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());
    Task task = taskQuery.singleResult();
    assertEquals("task", task.getTaskDefinitionKey());

    JobQuery jobQuery = managementService.createJobQuery().timers();
    assertEquals(1, jobQuery.count());

    String jobId = jobQuery.singleResult().getId();
    managementService.executeJob(jobId);

    assertEquals(0, jobQuery.count());

    assertEquals(1, taskQuery.count());
    task = taskQuery.singleResult();
    assertEquals("eventSubProcessTask", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstanceId);
  }

}
