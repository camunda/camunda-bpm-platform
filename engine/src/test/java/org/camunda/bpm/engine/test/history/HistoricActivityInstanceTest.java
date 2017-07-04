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

package org.camunda.bpm.engine.test.history;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Marcel Wieczorek
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricActivityInstanceTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testHistoricActivityInstanceNoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("noop").singleResult();

    assertEquals("noop", historicActivityInstance.getActivityId());
    assertEquals("serviceTask", historicActivityInstance.getActivityType());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());
    assertNotNull(historicActivityInstance.getEndTime());
    assertTrue(historicActivityInstance.getDurationInMillis() >= 0);
  }

  @Deployment
  public void testHistoricActivityInstanceReceive() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("receive").singleResult();

    assertEquals("receive", historicActivityInstance.getActivityId());
    assertEquals("receiveTask", historicActivityInstance.getActivityType());
    assertNull(historicActivityInstance.getEndTime());
    assertNull(historicActivityInstance.getDurationInMillis());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());

    // move clock by 1 second
    Date now = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(new Date(now.getTime() + 1000));

    runtimeService.signal(processInstance.getId());

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("receive").singleResult();

    assertEquals("receive", historicActivityInstance.getActivityId());
    assertEquals("receiveTask", historicActivityInstance.getActivityType());
    assertNotNull(historicActivityInstance.getEndTime());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());
    assertTrue(historicActivityInstance.getDurationInMillis() >= 1000);
    assertTrue(((HistoricActivityInstanceEventEntity)historicActivityInstance).getDurationRaw() >= 1000);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceReceive.bpmn20.xml" })
  public void testLongRunningHistoricActivityInstanceReceive() {
    final long ONE_YEAR = 1000 * 60 * 60 * 24 * 365;

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    ClockUtil.setCurrentTime(cal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("receive").singleResult();

    assertEquals("receive", historicActivityInstance.getActivityId());
    assertEquals("receiveTask", historicActivityInstance.getActivityType());
    assertNull(historicActivityInstance.getEndTime());
    assertNull(historicActivityInstance.getDurationInMillis());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());

    // move clock by 1 year
    cal.add(Calendar.YEAR, 1);
    ClockUtil.setCurrentTime(cal.getTime());

    runtimeService.signal(processInstance.getId());

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("receive").singleResult();

    assertEquals("receive", historicActivityInstance.getActivityId());
    assertEquals("receiveTask", historicActivityInstance.getActivityType());
    assertNotNull(historicActivityInstance.getEndTime());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());
    assertTrue(historicActivityInstance.getDurationInMillis() >= ONE_YEAR);
    assertTrue(((HistoricActivityInstanceEventEntity)historicActivityInstance).getDurationRaw() >= ONE_YEAR);
  }

  @Deployment
  public void testHistoricActivityInstanceQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("nonExistingActivityId").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("noop").list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityType("nonExistingActivityType").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityType("serviceTask").list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityName("nonExistingActivityName").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityName("No operation").list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().taskAssignee("nonExistingAssignee").list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().executionId("nonExistingExecutionId").list().size());

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId("nonExistingProcessInstanceId").list().size());

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId("nonExistingProcessDefinitionId").list().size());

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().unfinished().list().size());

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().finished().list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().finished().list().size());
    }

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().list().get(0);
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityInstanceId(historicActivityInstance.getId()).list().size());
    }
  }

  @Deployment
  public void testHistoricActivityInstanceForEventsQuery() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("eventProcess");
    assertEquals(1, taskService.createTaskQuery().count());
    runtimeService.signalEventReceived("signal");
    assertProcessEnded(pi.getId());

    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("noop").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("userTask").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("intermediate-event").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("start").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("end").list().size());

     assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("boundaryEvent").list().size());

    HistoricActivityInstance intermediateEvent = historyService.createHistoricActivityInstanceQuery().activityId("intermediate-event").singleResult();
    assertNotNull(intermediateEvent.getStartTime());
    assertNotNull(intermediateEvent.getEndTime());

    HistoricActivityInstance startEvent = historyService.createHistoricActivityInstanceQuery().activityId("start").singleResult();
    assertNotNull(startEvent.getStartTime());
    assertNotNull(startEvent.getEndTime());

    HistoricActivityInstance endEvent = historyService.createHistoricActivityInstanceQuery().activityId("end").singleResult();
    assertNotNull(endEvent.getStartTime());
    assertNotNull(endEvent.getEndTime());
  }

  @Deployment
  public void testHistoricActivityInstanceProperties() {
    // Start process instance
    runtimeService.startProcessInstanceByKey("taskAssigneeProcess");

    // Get task list
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("theTask").singleResult();

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals(task.getId(), historicActivityInstance.getTaskId());
    assertEquals("kermit", historicActivityInstance.getAssignee());

    // change assignee of the task
    taskService.setAssignee(task.getId(), "gonzo");
    task = taskService.createTaskQuery().singleResult();

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("theTask").singleResult();
    assertEquals("gonzo", historicActivityInstance.getAssignee());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/calledProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testCallSimpleSubProcess.bpmn20.xml" })
  public void testHistoricActivityInstanceCalledProcessId() {
    runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("callSubProcess").singleResult();

    HistoricProcessInstance oldInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("calledProcess").singleResult();

    assertEquals(oldInstance.getId(), historicActivityInstance.getCalledProcessInstanceId());
  }

  @Deployment
  public void testSorting() {
    runtimeService.startProcessInstanceByKey("process");

    int expectedActivityInstances = -1;
    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      expectedActivityInstances = 2;
    } else {
      expectedActivityInstances = 0;
    }

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().list().size());

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().list()
        .size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().list().size());

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().count());

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().count());
  }

  public void testInvalidSorting() {
    try {
      historyService.createHistoricActivityInstanceQuery().asc().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricActivityInstanceQuery().desc().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricActivityInstanceQueryStartFinishAfterBefore() {
    Calendar startTime = Calendar.getInstance();

    ClockUtil.setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // Start/end dates
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedAfter(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").startedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").startedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").startedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").startedAfter(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").startedAfter(hourFromNow.getTime()).startedBefore(hourAgo.getTime()).count());

    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finished().count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedAfter(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourAgo.getTime()).finishedAfter(hourFromNow.getTime()).count());
  }

  @Deployment
  public void testHistoricActivityInstanceQueryByCompleteScope() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    List<Task> tasks = taskService.createTaskQuery().list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().completeScope();

    assertEquals(3, query.count());

    List<HistoricActivityInstance> instances = query.list();

    for (HistoricActivityInstance instance : instances) {
      if (!instance.getActivityId().equals("innerEnd") && !instance.getActivityId().equals("end1") && !instance.getActivityId().equals("end2")) {
        fail("Unexpected instance with activity id " + instance.getActivityId() + " found.");
      }
    }

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceQueryByCompleteScope.bpmn")
  public void testHistoricActivityInstanceQueryByCanceled() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().canceled();

    assertEquals(3, query.count());

    List<HistoricActivityInstance> instances = query.list();

    for (HistoricActivityInstance instance : instances) {
      if (!instance.getActivityId().equals("subprocess") && !instance.getActivityId().equals("userTask1") && !instance.getActivityId().equals("userTask2")) {
        fail("Unexpected instance with activity id " + instance.getActivityId() + " found.");
      }
    }

    assertProcessEnded(processInstance.getId());
  }

  public void testHistoricActivityInstanceQueryByCompleteScopeAndCanceled() {
    try {
      historyService
          .createHistoricActivityInstanceQuery()
          .completeScope()
          .canceled()
          .list();
      fail("It should not be possible to query by completeScope and canceled.");
    } catch (ProcessEngineException e) {
      // exception expected
    }
  }

  /**
   * https://app.camunda.com/jira/browse/CAM-1537
   */
  @Deployment
  public void testHistoricActivityInstanceGatewayEndTimes() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("gatewayEndTimes");

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    List<Task> tasks = query.list();
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // process instance should have finished
    assertNotNull(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult().getEndTime());
    // gateways should have end timestamps
    assertNotNull(historyService.createHistoricActivityInstanceQuery().activityId("Gateway_0").singleResult().getEndTime());

    // there exists two historic activity instances for "Gateway_1" (parallel join)
    HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("Gateway_1");

    assertEquals(2, historicActivityInstanceQuery.count());
    // they should have an end timestamp
    assertNotNull(historicActivityInstanceQuery.list().get(0).getEndTime());
    assertNotNull(historicActivityInstanceQuery.list().get(1).getEndTime());
  }

  @Deployment
  public void testHistoricActivityInstanceTimerEvent() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    Job timer = jobQuery.singleResult();
    managementService.executeJob(timer.getId());

    TaskQuery taskQuery = taskService.createTaskQuery();
    Task task = taskQuery.singleResult();

    assertEquals("afterTimer", task.getName());

    HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("gw1");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNotNull(historicActivityInstanceQuery.singleResult().getEndTime());

    historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("timerEvent");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNotNull(historicActivityInstanceQuery.singleResult().getEndTime());
    assertEquals("intermediateTimer", historicActivityInstanceQuery.singleResult().getActivityType());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceTimerEvent.bpmn20.xml"})
  public void testHistoricActivityInstanceMessageEvent() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery();
    assertEquals(1, eventSubscriptionQuery.count());

    runtimeService.correlateMessage("newInvoice");

    TaskQuery taskQuery = taskService.createTaskQuery();
    Task task = taskQuery.singleResult();

    assertEquals("afterMessage", task.getName());

    HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("gw1");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNotNull(historicActivityInstanceQuery.singleResult().getEndTime());

    historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("messageEvent");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNotNull(historicActivityInstanceQuery.singleResult().getEndTime());
    assertEquals("intermediateMessageCatch", historicActivityInstanceQuery.singleResult().getActivityType());
  }

  @Deployment
  public void testUserTaskStillRunning() {
    runtimeService.startProcessInstanceByKey("nonInterruptingEvent");

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    managementService.executeJob(jobQuery.singleResult().getId());

    HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("userTask");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNull(historicActivityInstanceQuery.singleResult().getEndTime());

    historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("end1");
    assertEquals(0, historicActivityInstanceQuery.count());

    historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("timer");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNotNull(historicActivityInstanceQuery.singleResult().getEndTime());

    historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery().activityId("end2");
    assertEquals(1, historicActivityInstanceQuery.count());
    assertNotNull(historicActivityInstanceQuery.singleResult().getEndTime());
  }

  @Deployment
  public void testInterruptingBoundaryMessageEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("newMessage").singleResult();

    runtimeService.messageEventReceived("newMessage", execution.getId());

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("message");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundaryMessage", query.singleResult().getActivityType());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testNonInterruptingBoundaryMessageEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("newMessage").singleResult();

    runtimeService.messageEventReceived("newMessage", execution.getId());

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("message");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundaryMessage", query.singleResult().getActivityType());

    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testInterruptingBoundarySignalEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Execution execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("newSignal").singleResult();

    runtimeService.signalEventReceived("newSignal", execution.getId());

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("signal");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundarySignal", query.singleResult().getActivityType());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testNonInterruptingBoundarySignalEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Execution execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("newSignal").singleResult();

    runtimeService.signalEventReceived("newSignal", execution.getId());

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("signal");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundarySignal", query.singleResult().getActivityType());

    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testInterruptingBoundaryTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("timer");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundaryTimer", query.singleResult().getActivityType());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testNonInterruptingBoundaryTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("timer");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundaryTimer", query.singleResult().getActivityType());

    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testBoundaryErrorEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("error");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("boundaryError", query.singleResult().getActivityType());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testBoundaryCancelEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("catchCancel");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());
    assertEquals("cancelBoundaryCatch", query.singleResult().getActivityType());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testBoundaryCompensateEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    // the compensation boundary event should not appear in history!
    query.activityId("compensate");
    assertEquals(0, query.count());

    assertProcessEnded(pi.getId());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testBoundaryCompensateEvent.bpmn20.xml")
  public void testCompensationServiceTaskHasEndTime() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("compensationServiceTask");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());

    assertProcessEnded(pi.getId());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testBoundaryCancelEvent.bpmn20.xml")
  public void testTransaction() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("transaction");
    assertEquals(1, query.count());
    assertNotNull(query.singleResult().getEndTime());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testScopeActivity() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    query.activityId("userTask");
    assertEquals(1, query.count());

    HistoricActivityInstance historicActivityInstance = query.singleResult();

    assertEquals(pi.getId(), historicActivityInstance.getParentActivityInstanceId());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testMultiInstanceScopeActivity() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    HistoricActivityInstance miBodyInstance = query.activityId("userTask#multiInstanceBody").singleResult();

    query.activityId("userTask");
    assertEquals(5, query.count());


    List<HistoricActivityInstance> result = query.list();

    for (HistoricActivityInstance instance : result) {
      assertEquals(miBodyInstance.getId(), instance.getParentActivityInstanceId());
    }

    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testMultiInstanceReceiveActivity() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
    HistoricActivityInstance miBodyInstance = query.activityId("receiveTask#multiInstanceBody").singleResult();

    query.activityId("receiveTask");
    assertEquals(5, query.count());

    List<HistoricActivityInstance> result = query.list();

    for (HistoricActivityInstance instance : result) {
      assertEquals(miBodyInstance.getId(), instance.getParentActivityInstanceId());
    }

  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testEvents.bpmn")
  public void testIntermediateCatchEventTypes() {
    HistoricActivityInstanceQuery query = startEventTestProcess("");

    query.activityId("intermediateSignalCatchEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateSignalCatch", query.singleResult().getActivityType());

    query.activityId("intermediateMessageCatchEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateMessageCatch", query.singleResult().getActivityType());

    query.activityId("intermediateTimerCatchEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateTimer", query.singleResult().getActivityType());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testEvents.bpmn")
  public void testIntermediateThrowEventTypes() {
    HistoricActivityInstanceQuery query = startEventTestProcess("");

    query.activityId("intermediateSignalThrowEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateSignalThrow", query.singleResult().getActivityType());

    query.activityId("intermediateMessageThrowEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateMessageThrowEvent", query.singleResult().getActivityType());

    query.activityId("intermediateNoneThrowEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateNoneThrowEvent", query.singleResult().getActivityType());

    query.activityId("intermediateCompensationThrowEvent");
    assertEquals(1, query.count());
    assertEquals("intermediateCompensationThrowEvent", query.singleResult().getActivityType());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testEvents.bpmn")
  public void testStartEventTypes() {
    HistoricActivityInstanceQuery query = startEventTestProcess("");

    query.activityId("timerStartEvent");
    assertEquals(1, query.count());
    assertEquals("startTimerEvent", query.singleResult().getActivityType());

    query.activityId("noneStartEvent");
    assertEquals(1, query.count());
    assertEquals("startEvent", query.singleResult().getActivityType());

    query = startEventTestProcess("CAM-2365");
    query.activityId("messageStartEvent");
    assertEquals(1, query.count());
    assertEquals("messageStartEvent", query.singleResult().getActivityType());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testEvents.bpmn")
  public void testEndEventTypes() {
    HistoricActivityInstanceQuery query = startEventTestProcess("");

    query.activityId("cancellationEndEvent");
    assertEquals(1, query.count());
    assertEquals("cancelEndEvent", query.singleResult().getActivityType());

    query.activityId("messageEndEvent");
    assertEquals(1, query.count());
    assertEquals("messageEndEvent", query.singleResult().getActivityType());

    query.activityId("errorEndEvent");
    assertEquals(1, query.count());
    assertEquals("errorEndEvent", query.singleResult().getActivityType());

    query.activityId("signalEndEvent");
    assertEquals(1, query.count());
    assertEquals("signalEndEvent", query.singleResult().getActivityType());

    query.activityId("terminationEndEvent");
    assertEquals(1, query.count());
    assertEquals("terminateEndEvent", query.singleResult().getActivityType());

    query.activityId("noneEndEvent");
    assertEquals(1, query.count());
    assertEquals("noneEndEvent", query.singleResult().getActivityType());
  }

  private HistoricActivityInstanceQuery startEventTestProcess(String message) {
    if(message.equals("")) {
      runtimeService.startProcessInstanceByKey("testEvents");
    } else {
      runtimeService.startProcessInstanceByMessage("CAM-2365");
    }

    return historyService.createHistoricActivityInstanceQuery();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.startEventTypesForEventSubprocess.bpmn20.xml")
  public void testMessageEventSubprocess() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("shouldThrowError", false);
    runtimeService.startProcessInstanceByKey("process", vars);

    runtimeService.correlateMessage("newMessage");

    HistoricActivityInstance historicActivity = historyService.createHistoricActivityInstanceQuery()
        .activityId("messageStartEvent").singleResult();

    assertEquals("messageStartEvent", historicActivity.getActivityType());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.startEventTypesForEventSubprocess.bpmn20.xml")
  public void testSignalEventSubprocess() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("shouldThrowError", false);
    runtimeService.startProcessInstanceByKey("process", vars);

    runtimeService.signalEventReceived("newSignal");

    HistoricActivityInstance historicActivity = historyService.createHistoricActivityInstanceQuery()
        .activityId("signalStartEvent").singleResult();

    assertEquals("signalStartEvent", historicActivity.getActivityType());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.startEventTypesForEventSubprocess.bpmn20.xml")
  public void testTimerEventSubprocess() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("shouldThrowError", false);
    runtimeService.startProcessInstanceByKey("process", vars);

    Job timerJob = managementService.createJobQuery().singleResult();
    managementService.executeJob(timerJob.getId());

    HistoricActivityInstance historicActivity = historyService.createHistoricActivityInstanceQuery()
        .activityId("timerStartEvent").singleResult();

    assertEquals("startTimerEvent", historicActivity.getActivityType());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.startEventTypesForEventSubprocess.bpmn20.xml")
  public void testErrorEventSubprocess() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("shouldThrowError", true);
    runtimeService.startProcessInstanceByKey("process", vars);

    HistoricActivityInstance historicActivity = historyService.createHistoricActivityInstanceQuery()
        .activityId("errorStartEvent").singleResult();

    assertEquals("errorStartEvent", historicActivity.getActivityType());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testCaseCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testCaseCallActivity() {
    runtimeService.startProcessInstanceByKey("process");

    String subCaseInstanceId = caseService
        .createCaseInstanceQuery()
        .singleResult()
        .getId();


    HistoricActivityInstance historicCallActivity = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("callActivity")
        .singleResult();

    assertEquals(subCaseInstanceId, historicCallActivity.getCalledCaseInstanceId());
    assertNull(historicCallActivity.getEndTime());

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService.completeCaseExecution(humanTaskId);

    historicCallActivity = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("callActivity")
        .singleResult();

    assertEquals(subCaseInstanceId, historicCallActivity.getCalledCaseInstanceId());
    assertNotNull(historicCallActivity.getEndTime());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testProcessDefinitionKeyProperty() {
    // given
    String key = "oneTaskProcess";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    // when
    HistoricActivityInstance activityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .processInstanceId(processInstanceId)
      .activityId("theTask")
      .singleResult();

    // then
    assertNotNull(activityInstance.getProcessDefinitionKey());
    assertEquals(key, activityInstance.getProcessDefinitionKey());

  }

  @Deployment
  public void testEndParallelJoin() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<HistoricActivityInstance> activityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .processInstanceId(pi.getId())
      .activityId("parallelJoinEnd")
      .list();

    assertThat(activityInstance.size(), is(2));
    assertThat(pi.isEnded(), is(true));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceProperties.bpmn20.xml"})
  public void testAssigneeSavedWhenTaskSaved() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask");

    runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
    HistoricActivityInstance historicActivityInstance = query.singleResult();

    Task task = taskService.createTaskQuery().singleResult();

    // assume
    assertEquals("kermit", historicActivityInstance.getAssignee());

    // when
    task.setAssignee("gonzo");
    taskService.saveTask(task);

    // then
    historicActivityInstance = query.singleResult();
    assertEquals("gonzo", historicActivityInstance.getAssignee());
  }

}
