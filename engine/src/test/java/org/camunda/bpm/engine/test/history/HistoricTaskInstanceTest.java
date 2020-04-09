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
package org.camunda.bpm.engine.test.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;


/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricTaskInstanceTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testHistoricTaskInstance() throws Exception {
    String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    // Set priority to non-default value
    Task runtimeTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    runtimeTask.setPriority(1234);

    // Set due-date
    Date dueDate = sdf.parse("01/02/2003 04:05:06");
    runtimeTask.setDueDate(dueDate);
    taskService.saveTask(runtimeTask);

    String taskId = runtimeTask.getId();
    String taskDefinitionKey = runtimeTask.getTaskDefinitionKey();

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(taskId, historicTaskInstance.getId());
    assertEquals(1234, historicTaskInstance.getPriority());
    assertEquals("Clean up", historicTaskInstance.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", historicTaskInstance.getDescription());
    assertEquals(dueDate, historicTaskInstance.getDueDate());
    assertEquals("kermit", historicTaskInstance.getAssignee());
    assertEquals(taskDefinitionKey, historicTaskInstance.getTaskDefinitionKey());
    assertNull(historicTaskInstance.getEndTime());
    assertNull(historicTaskInstance.getDurationInMillis());

    assertNull(historicTaskInstance.getCaseDefinitionId());
    assertNull(historicTaskInstance.getCaseInstanceId());
    assertNull(historicTaskInstance.getCaseExecutionId());

    // the activity instance id is set
    assertEquals(((TaskEntity)runtimeTask).getExecution().getActivityInstanceId(), historicTaskInstance.getActivityInstanceId());

    runtimeService.setVariable(processInstanceId, "deadline", "yesterday");

    // move clock by 1 second
    Date now = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(new Date(now.getTime() + 1000));

    taskService.complete(taskId);

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());

    historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(taskId, historicTaskInstance.getId());
    assertEquals(1234, historicTaskInstance.getPriority());
    assertEquals("Clean up", historicTaskInstance.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", historicTaskInstance.getDescription());
    assertEquals(dueDate, historicTaskInstance.getDueDate());
    assertEquals("kermit", historicTaskInstance.getAssignee());
    assertEquals(TaskEntity.DELETE_REASON_COMPLETED, historicTaskInstance.getDeleteReason());
    assertEquals(taskDefinitionKey, historicTaskInstance.getTaskDefinitionKey());
    assertNotNull(historicTaskInstance.getEndTime());
    assertNotNull(historicTaskInstance.getDurationInMillis());
    assertTrue(historicTaskInstance.getDurationInMillis() >= 1000);
    assertTrue(((HistoricTaskInstanceEntity)historicTaskInstance).getDurationRaw() >= 1000);

    assertNull(historicTaskInstance.getCaseDefinitionId());
    assertNull(historicTaskInstance.getCaseInstanceId());
    assertNull(historicTaskInstance.getCaseExecutionId());

    historyService.deleteHistoricTaskInstance(taskId);

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
  }

  @Test
  public void testDeleteHistoricTaskInstance() {
    // deleting unexisting historic task instance should be silently ignored
    historyService.deleteHistoricTaskInstance("unexistingId");
  }

  @Deployment
  @Test
  public void testHistoricTaskInstanceQuery() throws Exception {
    // First instance is finished
    ProcessInstance finishedInstance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");

    // Set priority to non-default value
    Task task = taskService.createTaskQuery().processInstanceId(finishedInstance.getId()).singleResult();
    task.setPriority(1234);
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 04:05:06");
    task.setDueDate(dueDate);

    taskService.saveTask(task);

    // Complete the task
    String taskId = task.getId();
    taskService.complete(taskId);

    // Task id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskId("unexistingtaskid").count());

    // Name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskName("Clean_up").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskName("unexistingname").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskNameLike("Clean\\_u%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskNameLike("%lean\\_up").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskNameLike("%lean\\_u%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskNameLike("%unexistingname%").count());


    // Description
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescription("Historic task_description").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDescription("unexistingdescription").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%task\\_description").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("Historic task%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%task%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%unexistingdescripton%").count());

    // Execution id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().executionId(finishedInstance.getId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().executionId("unexistingexecution").count());

    // Process instance id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processInstanceId(finishedInstance.getId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processInstanceId("unexistingid").count());

    // Process definition id
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionId(finishedInstance.getProcessDefinitionId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionId("unexistingdefinitionid").count());

    // Process definition name
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionName("Historic task query test process").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionName("unexistingdefinitionname").count());

    // Process definition key
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processDefinitionKey("HistoricTaskQueryTest").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processDefinitionKey("unexistingdefinitionkey").count());


    // Assignee
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssignee("ker_mit").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskAssignee("johndoe").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%er\\_mit").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("ker\\_mi%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%er\\_mi%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%johndoe%").count());

    // Delete reason
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDeleteReason(TaskEntity.DELETE_REASON_COMPLETED).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDeleteReason("deleted").count());

    // Task definition ID
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("task").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("unexistingkey").count());

    // Task priority
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskPriority(1234).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskPriority(5678).count());


    // Due date
    Calendar anHourAgo = Calendar.getInstance();
    anHourAgo.setTime(dueDate);
    anHourAgo.add(Calendar.HOUR, -1);

    Calendar anHourLater = Calendar.getInstance();
    anHourLater.setTime(dueDate);
    anHourLater.add(Calendar.HOUR, 1);

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueDate(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueDate(anHourLater.getTime()).count());

    // Due date before
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourLater.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourAgo.getTime()).count());

    // Due date after
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueAfter(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueAfter(anHourLater.getTime()).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).taskDueBefore(anHourLater.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).taskDueBefore(anHourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).taskDueAfter(anHourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueDate(dueDate).taskDueAfter(anHourLater.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskDueBefore(anHourAgo.getTime()).taskDueAfter(anHourLater.getTime()).count());

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // Start/end dates
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().finishedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().finishedBefore(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().finishedAfter(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().startedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().startedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().startedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().startedAfter(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().startedAfter(hourFromNow.getTime()).startedBefore(hourAgo.getTime()).count());

    // Finished and Unfinished - Add anther other instance that has a running task (unfinished)
    runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().finished().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().unfinished().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().unfinished().finished().count());
  }

  @Deployment
  @Test
  public void testHistoricTaskInstanceQueryByProcessVariableValue() throws Exception {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("hallo", "steffen");

      String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest", variables).getId();

      Task runtimeTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
      String taskId = runtimeTask.getId();

      HistoricTaskInstance historicTaskInstance = historyService
          .createHistoricTaskInstanceQuery()
          .processVariableValueEquals("hallo", "steffen")
          .singleResult();

      assertNotNull(historicTaskInstance);
      assertEquals(taskId, historicTaskInstance.getId());

      taskService.complete(taskId);
      assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).count());

      historyService.deleteHistoricTaskInstance(taskId);
      assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
    }
  }

  @Test
  public void testHistoricTaskInstanceAssignment() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    // task exists & has no assignee:
    HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertNull(hti.getAssignee());

    // assign task to jonny:
    taskService.setAssignee(task.getId(), "jonny");

    // should be reflected in history
    hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals("jonny", hti.getAssignee());
    assertNull(hti.getOwner());

    taskService.deleteTask(task.getId());
    historyService.deleteHistoricTaskInstance(hti.getId());
  }

  @Deployment
  @Test
  public void testHistoricTaskInstanceAssignmentListener() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("assignee", "jonny");
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    HistoricActivityInstance hai = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult();
    assertEquals("jonny", hai.getAssignee());

    HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals("jonny", hti.getAssignee());
    assertNull(hti.getOwner());

  }

  @Test
  public void testHistoricTaskInstanceOwner() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    // task exists & has no owner:
    HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertNull(hti.getOwner());

    // set owner to jonny:
    taskService.setOwner(task.getId(), "jonny");

    // should be reflected in history
    hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals("jonny", hti.getOwner());

    taskService.deleteTask(task.getId());
    historyService.deleteHistoricTaskInstance(hti.getId());
  }

  @Test
  public void testHistoricTaskInstancePriority() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    // task exists & has normal priority:
    HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(Task.PRIORITY_NORMAL, hti.getPriority());

    // set priority to maximum value:
    taskService.setPriority(task.getId(), Task.PRIORITY_MAXIMUM);

    // should be reflected in history
    hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    assertEquals(Task.PRIORITY_MAXIMUM, hti.getPriority());

    taskService.deleteTask(task.getId());
    historyService.deleteHistoricTaskInstance(hti.getId());
  }

  @Deployment
  @Test
  public void testHistoricTaskInstanceQueryProcessFinished() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoTaskHistoricTaskQueryTest");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Running task on running process should be available
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processUnfinished().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processFinished().count());

    // Finished and running task on running process should be available
    taskService.complete(task.getId());
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processUnfinished().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processFinished().count());

    // 2 finished tasks are found for finished process after completing last task of process
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processUnfinished().count());
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processFinished().count());

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processUnfinished().processFinished().count());
  }

  @Deployment
  @Test
  public void testHistoricTaskInstanceQuerySorting() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("HistoricTaskQueryTest");

    String taskId = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult().getId();
    taskService.complete(taskId);

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByExecutionId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskName().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDueDate().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskFollowUpDate().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByCaseDefinitionId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByCaseInstanceId().asc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByCaseExecutionId().asc().count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByExecutionId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskName().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskDueDate().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByTaskFollowUpDate().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByCaseDefinitionId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByCaseInstanceId().desc().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().orderByCaseExecutionId().desc().count());
  }

  @Test
  public void testInvalidSorting() {
    try {
      historyService.createHistoricTaskInstanceQuery().asc();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricTaskInstanceQuery().desc();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricTaskInstanceTest.testHistoricTaskInstance.bpmn20.xml"})
  @Test
  public void testHistoricTaskInstanceQueryByFollowUpDate() throws Exception {
    Calendar otherDate = Calendar.getInstance();

    runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");

    // do not find any task instances with follow up date
    assertEquals(0, taskService.createTaskQuery().followUpDate(otherDate.getTime()).count());

    Task task = taskService.createTaskQuery().singleResult();

    // set follow-up date on task
    Date followUpDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setFollowUpDate(followUpDate);
    taskService.saveTask(task);

    // test that follow-up date was written to historic database
    assertEquals(followUpDate, historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult().getFollowUpDate());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskFollowUpDate(followUpDate).count());

    otherDate.setTime(followUpDate);

    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskFollowUpDate(otherDate.getTime()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskFollowUpBefore(otherDate.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskFollowUpAfter(otherDate.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskFollowUpAfter(otherDate.getTime()).taskFollowUpDate(followUpDate).count());

    otherDate.add(Calendar.YEAR, -2);
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskFollowUpAfter(otherDate.getTime()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskFollowUpBefore(otherDate.getTime()).count());
    assertEquals(followUpDate, historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult().getFollowUpDate());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskFollowUpBefore(otherDate.getTime()).taskFollowUpDate(followUpDate).count());

    taskService.complete(task.getId());

    assertEquals(followUpDate, historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult().getFollowUpDate());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskFollowUpDate(followUpDate).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricTaskInstanceTest.testHistoricTaskInstance.bpmn20.xml"})
  @Test
  public void testHistoricTaskInstanceQueryByActivityInstanceId() throws Exception {
    runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");

    String activityInstanceId = historyService.createHistoricActivityInstanceQuery()
        .activityId("task")
        .singleResult()
        .getId();

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
        .activityInstanceIdIn(activityInstanceId);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricTaskInstanceTest.testHistoricTaskInstance.bpmn20.xml"})
  @Test
  public void testHistoricTaskInstanceQueryByActivityInstanceIds() throws Exception {
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");

    String activityInstanceId1 = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(pi1.getId())
        .activityId("task")
        .singleResult()
        .getId();

    String activityInstanceId2 = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(pi2.getId())
        .activityId("task")
        .singleResult()
        .getId();

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
        .activityInstanceIdIn(activityInstanceId1, activityInstanceId2);

    assertEquals(2, query.count());
    assertEquals(2, query.list().size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricTaskInstanceTest.testHistoricTaskInstance.bpmn20.xml"})
  @Test
  public void testHistoricTaskInstanceQueryByInvalidActivityInstanceId() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.activityInstanceIdIn("invalid");
    assertEquals(0, query.count());

    try {
      query.activityInstanceIdIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      query.activityInstanceIdIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      String[] values = { "a", null, "b" };
      query.activityInstanceIdIn(values);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseDefinitionId() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // then
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseDefinitionId(caseDefinitionId);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    HistoricTaskInstance task = query.singleResult();
    assertNotNull(task);

    assertEquals(caseDefinitionId, task.getCaseDefinitionId());
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    assertEquals(humanTaskId, task.getCaseExecutionId());
  }

  @Test
  public void testQueryByInvalidCaseDefinitionId() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseDefinitionId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

    query.caseDefinitionId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseDefinitionKey() {
    // given
    String key = "oneTaskCase";

    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(key)
        .singleResult()
        .getId();

    String caseInstanceId = caseService
        .withCaseDefinitionByKey(key)
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // then
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseDefinitionKey(key);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    HistoricTaskInstance task = query.singleResult();
    assertNotNull(task);

    assertEquals(caseDefinitionId, task.getCaseDefinitionId());
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    assertEquals(humanTaskId, task.getCaseExecutionId());
  }

  @Test
  public void testQueryByInvalidCaseDefinitionKey() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseDefinitionKey("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

    query.caseDefinitionKey(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseDefinitionName() {
    // given
    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .singleResult();

    String caseDefinitionName = caseDefinition.getName();
    String caseDefinitionId = caseDefinition.getId();

    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // then
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseDefinitionName(caseDefinitionName);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    HistoricTaskInstance task = query.singleResult();
    assertNotNull(task);

    assertEquals(caseDefinitionId, task.getCaseDefinitionId());
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    assertEquals(humanTaskId, task.getCaseExecutionId());
  }

  @Test
  public void testQueryByInvalidCaseDefinitionName() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseDefinitionName("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

    query.caseDefinitionName(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseInstanceId() {
    // given
    String key = "oneTaskCase";

    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(key)
        .singleResult()
        .getId();

    String caseInstanceId = caseService
        .withCaseDefinitionByKey(key)
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // then
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseInstanceId(caseInstanceId);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    HistoricTaskInstance task = query.singleResult();
    assertNotNull(task);

    assertEquals(caseDefinitionId, task.getCaseDefinitionId());
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    assertEquals(humanTaskId, task.getCaseExecutionId());
  }



  @Deployment(resources=
    {
      "org/camunda/bpm/engine/test/history/HistoricTaskInstanceTest.testQueryByCaseInstanceIdHierarchy.cmmn",
      "org/camunda/bpm/engine/test/history/HistoricTaskInstanceTest.testQueryByCaseInstanceIdHierarchy.bpmn20.xml"
    })
  @Test
  public void testQueryByCaseInstanceIdHierarchy() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    caseService
        .createCaseExecutionQuery()
        .activityId("PI_ProcessTask_1")
        .singleResult()
        .getId();

    // then
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseInstanceId(caseInstanceId);

    assertEquals(2, query.count());
    assertEquals(2, query.list().size());

    for (HistoricTaskInstance task : query.list()) {
      assertEquals(caseInstanceId, task.getCaseInstanceId());

      assertNull(task.getCaseDefinitionId());
      assertNull(task.getCaseExecutionId());

      taskService.complete(task.getId());
    }

    assertEquals(3, query.count());
    assertEquals(3, query.list().size());

    for (HistoricTaskInstance task : query.list()) {
      assertEquals(caseInstanceId, task.getCaseInstanceId());

      assertNull(task.getCaseDefinitionId());
      assertNull(task.getCaseExecutionId());
    }

  }

  @Test
  public void testQueryByInvalidCaseInstanceId() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseExecutionId() {
    // given
    String key = "oneTaskCase";

    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(key)
        .singleResult()
        .getId();

    String caseInstanceId = caseService
        .withCaseDefinitionByKey(key)
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // then
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseExecutionId(humanTaskId);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    HistoricTaskInstance task = query.singleResult();
    assertNotNull(task);

    assertEquals(caseDefinitionId, task.getCaseDefinitionId());
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    assertEquals(humanTaskId, task.getCaseExecutionId());
  }

  @Test
  public void testQueryByInvalidCaseExecutionId() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.caseExecutionId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

    query.caseExecutionId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());

  }

  @Test
  public void testHistoricTaskInstanceCaseInstanceId() {
    Task task = taskService.newTask();
    task.setCaseInstanceId("aCaseInstanceId");
    taskService.saveTask(task);

    HistoricTaskInstance hti = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(task.getId())
        .singleResult();

    assertEquals("aCaseInstanceId", hti.getCaseInstanceId());

    task.setCaseInstanceId("anotherCaseInstanceId");
    taskService.saveTask(task);

    hti = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(task.getId())
        .singleResult();

    assertEquals("anotherCaseInstanceId", hti.getCaseInstanceId());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessDefinitionKeyProperty() {
    // given
    String key = "oneTaskProcess";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    // when
    HistoricTaskInstance task = historyService
        .createHistoricTaskInstanceQuery()
        .processInstanceId(processInstanceId)
        .taskDefinitionKey("theTask")
        .singleResult();

    // then
    assertNotNull(task.getProcessDefinitionKey());
    assertEquals(key, task.getProcessDefinitionKey());

    assertNull(task.getCaseDefinitionKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  @Test
  public void testCaseDefinitionKeyProperty() {
    // given
    String key = "oneTaskCase";
    String caseInstanceId = caseService.createCaseInstanceByKey(key).getId();

    // when
    HistoricTaskInstance task = historyService
        .createHistoricTaskInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .taskDefinitionKey("PI_HumanTask_1")
        .singleResult();

    // then
    assertNotNull(task.getCaseDefinitionKey());
    assertEquals(key, task.getCaseDefinitionKey());

    assertNull(task.getProcessDefinitionKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByTaskDefinitionKey() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    HistoricTaskInstanceQuery query1 = historyService
        .createHistoricTaskInstanceQuery()
        .taskDefinitionKey("theTask");

    HistoricTaskInstanceQuery query2 = historyService
        .createHistoricTaskInstanceQuery()
        .taskDefinitionKeyIn("theTask");

    // then
    assertEquals(1, query1.count());
    assertEquals(1, query2.count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  @Test
  public void testQueryByTaskDefinitionKeys() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    caseService.createCaseInstanceByKey("oneTaskCase");

    // when
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskDefinitionKeyIn("theTask", "PI_HumanTask_1");

    // then
    assertEquals(2, query.count());
  }

  @Test
  public void testQueryByInvalidTaskDefinitionKeys() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.taskDefinitionKeyIn("invalid");
    assertEquals(0, query.count());

    try {
      query.taskDefinitionKeyIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (NotValidException e) {}

    try {
      query.taskDefinitionKeyIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (NotValidException e) {}

    try {
      String[] values = { "a", null, "b" };
      query.taskDefinitionKeyIn(values);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (NotValidException e) {}

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testQueryByProcessInstanceBusinessKey() {
    // given
    ProcessInstance piBusinessKey1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertEquals(1, query.processInstanceBusinessKey(piBusinessKey1.getBusinessKey()).count());
    assertEquals(0, query.processInstanceBusinessKey("unexistingBusinessKey").count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testQueryByProcessInstanceBusinessKeyIn() {
    // given
    String businessKey1 = "BUSINESS-KEY-1";
    String businessKey2 = "BUSINESS-KEY-2";
    String businessKey3 = "BUSINESS-KEY-3";
    String unexistingBusinessKey = "unexistingBusinessKey";

    runtimeService.startProcessInstanceByKey("oneTaskProcess", businessKey1);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", businessKey2);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", businessKey3);

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertEquals(3, query.processInstanceBusinessKeyIn(businessKey1, businessKey2, businessKey3).list().size());
    assertEquals(1, query.processInstanceBusinessKeyIn(businessKey2, unexistingBusinessKey).count());
  }

  @Test
  public void testQueryByInvalidProcessInstanceBusinessKeyIn() {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    query.processInstanceBusinessKeyIn("invalid");
    assertEquals(0, query.count());

    try {
      query.processInstanceBusinessKeyIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      query.processInstanceBusinessKeyIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      String[] values = { "a", null, "b" };
      query.processInstanceBusinessKeyIn(values);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testQueryByProcessInstanceBusinessKeyLike() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertEquals(1, query.processInstanceBusinessKeyLike("BUSINESS-KEY-1").list().size());
    assertEquals(1, query.processInstanceBusinessKeyLike("BUSINESS-KEY%").count());
    assertEquals(1, query.processInstanceBusinessKeyLike("%KEY-1").count());
    assertEquals(1, query.processInstanceBusinessKeyLike("%KEY%").count());
    assertEquals(0, query.processInstanceBusinessKeyLike("BUZINESS-KEY%").count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testQueryByProcessInstanceBusinessKeyAndArray() {
    // given
    String businessKey1 = "BUSINESS-KEY-1";
    String businessKey2 = "BUSINESS-KEY-2";
    String businessKey3 = "BUSINESS-KEY-3";
    String unexistingBusinessKey = "unexistingBusinessKey";

    runtimeService.startProcessInstanceByKey("oneTaskProcess", businessKey1);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", businessKey2);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", businessKey3);

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertEquals(0, query.processInstanceBusinessKeyIn(businessKey1, businessKey2).processInstanceBusinessKey(unexistingBusinessKey).count());
    assertEquals(1, query.processInstanceBusinessKeyIn(businessKey2, businessKey3).processInstanceBusinessKey(businessKey2).count());
  }
}
