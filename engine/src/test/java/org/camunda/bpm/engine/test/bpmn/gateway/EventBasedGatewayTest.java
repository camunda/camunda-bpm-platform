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

package org.camunda.bpm.engine.test.bpmn.gateway;

import java.util.Date;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class EventBasedGatewayTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/gateway/EventBasedGatewayTest.testCatchAlertAndTimer.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/gateway/EventBasedGatewayTest.throwAlertSignal.bpmn20.xml"})
  public void testCatchSignalCancelsTimer() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, managementService.createJobQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, managementService.createJobQuery().count());

    Task task = taskService.createTaskQuery()
      .taskName("afterSignal")
      .singleResult();

    assertNotNull(task);

    taskService.complete(task.getId());

  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/gateway/EventBasedGatewayTest.testCatchAlertAndTimer.bpmn20.xml"
          })
  public void testCatchTimerCancelsSignal() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, managementService.createJobQuery().count());

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() +10000));
    try {
      // wait for timer to fire
      waitForJobExecutorToProcessAllJobs(10000);

      assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
      assertEquals(1, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, managementService.createJobQuery().count());

      Task task = taskService.createTaskQuery()
        .taskName("afterTimer")
        .singleResult();

      assertNotNull(task);

      taskService.complete(task.getId());
    }finally{
      ClockUtil.setCurrentTime(new Date());
    }
  }

  @Deployment
  public void testCatchSignalAndMessageAndTimer() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(2, runtimeService.createEventSubscriptionQuery().count());
    EventSubscriptionQuery messageEventSubscriptionQuery = runtimeService.createEventSubscriptionQuery().eventType("message");
    assertEquals(1, messageEventSubscriptionQuery.count());
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, managementService.createJobQuery().count());

    // we can query for an execution with has both a signal AND message subscription
    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("newInvoice")
      .signalEventSubscriptionName("alert")
      .singleResult();
    assertNotNull(execution);

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() +10000));
    try {

      EventSubscription messageEventSubscription = messageEventSubscriptionQuery.singleResult();
      runtimeService.messageEventReceived(messageEventSubscription.getEventName(), messageEventSubscription.getExecutionId());

      assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
      assertEquals(1, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, managementService.createJobQuery().count());

      Task task = taskService.createTaskQuery()
        .taskName("afterMessage")
        .singleResult();

      assertNotNull(task);

      taskService.complete(task.getId());
    }finally{
      ClockUtil.setCurrentTime(new Date());
    }
  }

  public void testConnectedToActitiy() {

    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/gateway/EventBasedGatewayTest.testConnectedToActivity.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("Event based gateway can only be connected to elements of type intermediateCatchEvent")) {
        fail("different exception expected");
      }
    }

  }

  public void testInvalidSequenceFlow() {

    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/gateway/EventBasedGatewayTest.testEventInvalidSequenceFlow.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("Invalid incoming sequenceflow for intermediateCatchEvent")) {
        fail("different exception expected");
      }
    }

  }

  @Deployment
  public void testTimeCycle() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    String jobId = jobQuery.singleResult().getId();
    managementService.executeJob(jobId);

    assertEquals(0, jobQuery.count());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    assertProcessEnded(processInstanceId);
  }

}
