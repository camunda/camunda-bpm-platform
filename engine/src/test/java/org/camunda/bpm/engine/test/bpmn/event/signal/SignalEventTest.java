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

package org.camunda.bpm.engine.test.bpmn.event.signal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class SignalEventTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  public void testSignalCatchIntermediate() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundary.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  public void testSignalCatchBoundary() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  public void testSignalCatchBoundaryWithVariables() {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("catchSignal", variables1);

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    runtimeService.startProcessInstanceByKey("throwSignal", variables2);

    assertEquals("catchSignal", runtimeService.getVariable(pi.getId(), "processName"));
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsynch.bpmn20.xml"})
  public void testSignalCatchIntermediateAsynch() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // there is a job:
    assertEquals(1, managementService.createJobQuery().count());

    try {
      ClockUtil.setCurrentTime( new Date(System.currentTimeMillis() + 1000));
      waitForJobExecutorToProcessAllJobs(10000);

      assertEquals(0, createEventSubscriptionQuery().count());
      assertEquals(0, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, managementService.createJobQuery().count());
    }finally {
     ClockUtil.setCurrentTime(new Date());
    }

  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchMultipleSignals.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAbortSignal.bpmn20.xml"})
  public void testSignalCatchDifferentSignals() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(2, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwAbort");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    Task taskAfterAbort = taskService.createTaskQuery().taskAssignee("gonzo").singleResult();
    assertNotNull(taskAfterAbort);
    taskService.complete(taskAfterAbort.getId());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  /**
   * Verifies the solution of https://jira.codehaus.org/browse/ACT-1309
   */
  @Deployment
  public void testSignalBoundaryOnSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("signalEventOnSubprocess");
    runtimeService.signalEventReceived("stopSignal");
    assertProcessEnded(pi.getProcessInstanceId());
  }

  public void testDuplicateSignalNames() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.duplicateSignalNames.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("duplicate signal name")) {
        fail("different exception expected");
      }
    }
  }

  public void testNoSignalName() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.noSignalName.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("has no name")) {
        fail("different exception expected");
      }
    }
  }

  public void testSignalNoId() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.signalNoId.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("signal must have an id")) {
        fail("different exception expected");
      }
    }
  }

  public void testSignalNoRef() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.signalNoRef.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("signalEventDefinition does not have required property 'signalRef'")) {
        fail("different exception expected");
      }
    }
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }

  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  public void testNonInterruptingSignal() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalEvent");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1,  tasks.size());
    Task currentTask = tasks.get(0);
    assertEquals("My User Task", currentTask.getName());

    runtimeService.signalEventReceived("alert");

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(2,  tasks.size());

    for (Task task : tasks) {
      if (!task.getName().equals("My User Task") && !task.getName().equals("My Second User Task")) {
        fail("Expected: <My User Task> or <My Second User Task> but was <" + task.getName() + ">.");
      }
    }

    taskService.complete(taskService.createTaskQuery().taskName("My User Task").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1,  tasks.size());
    currentTask = tasks.get(0);
    assertEquals("My Second User Task", currentTask.getName());
  }


  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  public void testNonInterruptingSignalWithSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalWithSubProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1,  tasks.size());

    Task currentTask = tasks.get(0);
    assertEquals("Approve", currentTask.getName());

    runtimeService.signalEventReceived("alert");

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      if (!task.getName().equals("Approve") && !task.getName().equals("Review")) {
        fail("Expected: <Approve> or <Review> but was <" + task.getName() + ">.");
      }
    }

    taskService.complete(taskService.createTaskQuery().taskName("Approve").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

    currentTask = tasks.get(0);
    assertEquals("Review", currentTask.getName());

    taskService.complete(taskService.createTaskQuery().taskName("Review").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

  }

  @Deployment
  public void testSignalStartEventInEventSubProcess() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalStartEventInEventSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // send interrupting signal to event sub process
    runtimeService.signalEventReceived("alert");

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task doesn't exist because signal start event is interrupting
    taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because signal start event is interrupting
    executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(0, executionQuery.count());
  }

  @Deployment
  public void testNonInterruptingSignalStartEventInEventSubProcess() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingSignalStartEventInEventSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // send non interrupting signal to event sub process
    runtimeService.signalEventReceived("alert");

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task still exists because signal start event is non interrupting
    taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // check if execution still exists because signal start event is non interrupting
    executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());
  }

}
