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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Marcel Wieczorek
 */
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

    if (processEngineConfiguration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId("nonExistingProcessInstanceId").list().size());

    if (processEngineConfiguration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId("nonExistingProcessDefinitionId").list().size());

    if (processEngineConfiguration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().unfinished().list().size());

    if (processEngineConfiguration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().finished().list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().finished().list().size());
    }

    if (processEngineConfiguration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
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

    // TODO: Discuss if boundary events will occur in the log!
    // assertEquals(1,
    // historyService.createHistoricActivityInstanceQuery().activityId("boundaryEvent").list().size());

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
    if (processEngineConfiguration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
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
  
    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finished().count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedBefore(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("theTask").finishedAfter(hourFromNow.getTime()).count());
  }

}
