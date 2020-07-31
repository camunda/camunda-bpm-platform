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
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 *
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricActivityStatisticsQueryTest extends PluggableProcessEngineTest {

  private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testNoRunningProcessInstances() {
    String processDefinitionId = getProcessDefinitionId();

    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(0, query.count());
    assertEquals(0, statistics.size());
  }

  @Deployment
  @Test
  public void testSingleTask() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    HistoricActivityStatistics statistic = statistics.get(0);

    assertEquals("task", statistic.getId());
    assertEquals(5, statistic.getInstances());

    completeProcessInstances();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testFinishedProcessInstances() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    completeProcessInstances();

    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(0, query.count());
    assertEquals(0, statistics.size());
  }

  @Deployment
  @Test
  public void testMultipleRunningTasks() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(4, query.count());
    assertEquals(4, statistics.size());

    // innerTask
    HistoricActivityStatistics innerTask = statistics.get(0);

    assertEquals("innerTask", innerTask.getId());
    assertEquals(25, innerTask.getInstances());

    // subprocess
    HistoricActivityStatistics subProcess = statistics.get(1);

    assertEquals("subprocess", subProcess.getId());
    assertEquals(25, subProcess.getInstances());

    // subprocess multi instance body
    HistoricActivityStatistics subProcessMiBody = statistics.get(2);

    assertEquals("subprocess#multiInstanceBody", subProcessMiBody.getId());
    assertEquals(5, subProcessMiBody.getInstances());

    // task
    HistoricActivityStatistics task = statistics.get(3);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());

    completeProcessInstances();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.calledProcess.bpmn20.xml" })
  @Test
  public void testMultipleProcessDefinitions() {
    String processId = getProcessDefinitionId();
    String calledProcessId = getProcessDefinitionIdByKey("calledProcess");

    startProcesses(5);

    startProcessesByKey(10, "calledProcess");

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processId)
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    // callActivity
    HistoricActivityStatistics calledActivity = statistics.get(0);

    assertEquals("callActivity", calledActivity.getId());
    assertEquals(5, calledActivity.getInstances());

    query = historyService
        .createHistoricActivityStatisticsQuery(calledProcessId)
        .orderByActivityId()
        .asc();

    statistics = query.list();

    assertEquals(2, query.count());
    assertEquals(2, statistics.size());

    // task1
    HistoricActivityStatistics task1 = statistics.get(0);

    assertEquals("task1", task1.getId());
    assertEquals(15, task1.getInstances());

    // task2
    HistoricActivityStatistics task2 = statistics.get(1);

    assertEquals("task2", task2.getId());
    assertEquals(15, task2.getInstances());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByFinished() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(2, query.count());
    assertEquals(2, statistics.size());

    // start
    HistoricActivityStatistics start = statistics.get(0);

    assertEquals("start", start.getId());
    assertEquals(0, start.getInstances());
    assertEquals(5, start.getFinished());

    // task
    HistoricActivityStatistics task = statistics.get(1);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());
    assertEquals(0, task.getFinished());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByFinishedAfterFinishingSomeInstances() {
    String processDefinitionId = getProcessDefinitionId();

    // start five instances
    startProcesses(5);

    // complete two task, so that two process instances are finished
    List<Task> tasks = taskService.createTaskQuery().list();
    for (int i = 0; i < 2; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(3, query.count());
    assertEquals(3, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(2, end.getFinished());

    // start
    HistoricActivityStatistics start = statistics.get(1);

    assertEquals("start", start.getId());
    assertEquals(0, start.getInstances());
    assertEquals(5, start.getFinished());

    // task
    HistoricActivityStatistics task = statistics.get(2);

    assertEquals("task", task.getId());
    assertEquals(3, task.getInstances());
    assertEquals(2, task.getFinished());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testMultipleRunningTasks.bpmn20.xml")
  @Test
  public void testQueryByFinishedMultipleRunningTasks() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("innerTask").list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(9, query.count());
    assertEquals(9, statistics.size());

    // end1
    HistoricActivityStatistics end1 = statistics.get(0);

    assertEquals("end1", end1.getId());
    assertEquals(0, end1.getInstances());
    assertEquals(5, end1.getFinished());

    // gtw
    HistoricActivityStatistics gtw = statistics.get(1);

    assertEquals("gtw", gtw.getId());
    assertEquals(0, gtw.getInstances());
    assertEquals(5, gtw.getFinished());

    // innerEnd
    HistoricActivityStatistics innerEnd = statistics.get(2);

    assertEquals("innerEnd", innerEnd.getId());
    assertEquals(0, innerEnd.getInstances());
    assertEquals(25, innerEnd.getFinished());

    // innerStart
    HistoricActivityStatistics innerStart = statistics.get(3);

    assertEquals("innerStart", innerStart.getId());
    assertEquals(0, innerStart.getInstances());
    assertEquals(25, innerStart.getFinished());

    // innerTask
    HistoricActivityStatistics innerTask = statistics.get(4);

    assertEquals("innerTask", innerTask.getId());
    assertEquals(0, innerTask.getInstances());
    assertEquals(25, innerTask.getFinished());

    // innerStart
    HistoricActivityStatistics start = statistics.get(5);

    assertEquals("start", start.getId());
    assertEquals(0, start.getInstances());
    assertEquals(5, start.getFinished());

    // subprocess
    HistoricActivityStatistics subProcess = statistics.get(6);

    assertEquals("subprocess", subProcess.getId());
    assertEquals(0, subProcess.getInstances());
    assertEquals(25, subProcess.getFinished());

    // subprocess - multi-instance body
    HistoricActivityStatistics subProcessMiBody = statistics.get(7);

    assertEquals("subprocess#multiInstanceBody", subProcessMiBody.getId());
    assertEquals(0, subProcessMiBody.getInstances());
    assertEquals(5, subProcessMiBody.getFinished());

    // task
    HistoricActivityStatistics task = statistics.get(8);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());
    assertEquals(0, task.getFinished());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCompleteScope() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    completeProcessInstances();

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCompleteScope();
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(5, end.getCompleteScope());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCompleteScopeAfterFinishingSomeInstances() {
    String processDefinitionId = getProcessDefinitionId();

    // start five instances
    startProcesses(5);

    // complete two task, so that two process instances are finished
    List<Task> tasks = taskService.createTaskQuery().list();
    for (int i = 0; i < 2; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCompleteScope()
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(2, query.count());
    assertEquals(2, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(2, end.getCompleteScope());

    // task
    HistoricActivityStatistics task = statistics.get(1);

    assertEquals("task", task.getId());
    assertEquals(3, task.getInstances());
    assertEquals(0, task.getCompleteScope());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testMultipleRunningTasks.bpmn20.xml")
  @Test
  public void testQueryByCompleteScopeMultipleRunningTasks() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("innerTask").list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCompleteScope()
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(4, query.count());
    assertEquals(4, statistics.size());

    // end1
    HistoricActivityStatistics end1 = statistics.get(0);

    assertEquals("end1", end1.getId());
    assertEquals(0, end1.getInstances());
    assertEquals(5, end1.getCompleteScope());

    // innerEnd
    HistoricActivityStatistics innerEnd = statistics.get(1);

    assertEquals("innerEnd", innerEnd.getId());
    assertEquals(0, innerEnd.getInstances());
    assertEquals(25, innerEnd.getCompleteScope());

    // subprocess (completes the multi-instances body scope, see BPMN spec)
    HistoricActivityStatistics subprocess = statistics.get(2);

    assertEquals("subprocess", subprocess.getId());
    assertEquals(0, subprocess.getInstances());
    assertEquals(25, subprocess.getCompleteScope());

    // task
    HistoricActivityStatistics task = statistics.get(3);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());
    assertEquals(0, task.getCompleteScope());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCanceled() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    cancelProcessInstances();

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    // task
    HistoricActivityStatistics task = statistics.get(0);

    assertEquals("task", task.getId());
    assertEquals(0, task.getInstances());
    assertEquals(5, task.getCanceled());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCanceledAfterCancelingSomeInstances() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(3);

    // cancel running process instances
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance processInstance : processInstances) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    }

    startProcesses(2);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    // task
    HistoricActivityStatistics task = statistics.get(0);

    assertEquals("task", task.getId());
    assertEquals(2, task.getInstances());
    assertEquals(3, task.getCanceled());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCanceledAndFinished() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);

    // cancel running process instances
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance processInstance : processInstances) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    }

    startProcesses(2);

    // complete running tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    startProcesses(2);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled()
        .includeFinished()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(3, query.count());
    assertEquals(3, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(0, end.getCanceled());
    assertEquals(2, end.getFinished());

    // start
    HistoricActivityStatistics start = statistics.get(1);

    assertEquals("start", start.getId());
    assertEquals(0, start.getInstances());
    assertEquals(0, start.getCanceled());
    assertEquals(6, start.getFinished());

    // task
    HistoricActivityStatistics task = statistics.get(2);

    assertEquals("task", task.getId());
    assertEquals(2, task.getInstances());
    assertEquals(2, task.getCanceled());
    assertEquals(4, task.getFinished());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCanceledAndFinishedByPeriods() throws ParseException {
    try {

      //start two process instances
      ClockUtil.setCurrentTime(sdf.parse("15.01.2016 12:00:00"));
      startProcesses(2);

      // cancel running process instances
      ClockUtil.setCurrentTime(sdf.parse("15.02.2016 12:00:00"));
      List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
      for (ProcessInstance processInstance : processInstances) {
        runtimeService.deleteProcessInstance(processInstance.getId(), "test");
      }

      //start two process instances
      ClockUtil.setCurrentTime(sdf.parse("01.02.2016 12:00:00"));
      startProcesses(2);

      // complete running tasks
      ClockUtil.setCurrentTime(sdf.parse("25.02.2016 12:00:00"));
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.complete(task.getId());
      }

      //starte two more process instances
      ClockUtil.setCurrentTime(sdf.parse("15.03.2016 12:00:00"));
      startProcesses(2);

      //NOW
      ClockUtil.setCurrentTime(sdf.parse("25.03.2016 12:00:00"));

      String processDefinitionId = getProcessDefinitionId();
      //check January by started dates
      HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .startedAfter(sdf.parse("01.01.2016 00:00:00")).startedBefore(sdf.parse("31.01.2016 23:59:59")).orderByActivityId().asc();
      List<HistoricActivityStatistics> statistics = query.list();

      assertEquals(2, query.count());
      assertEquals(2, statistics.size());

      // start
      assertActivityStatistics(statistics.get(0), "start", 0, 0, 2);

      // task
      assertActivityStatistics(statistics.get(1), "task", 0, 2, 2);

      //check January by finished dates
      query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .finishedAfter(sdf.parse("01.01.2016 00:00:00")).finishedBefore(sdf.parse("31.01.2016 23:59:59")).orderByActivityId().asc();
      statistics = query.list();

      assertEquals(0, query.count());
      assertEquals(0, statistics.size());

      //check February by started dates
      query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .startedAfter(sdf.parse("01.02.2016 00:00:00")).startedBefore(sdf.parse("28.02.2016 23:59:59")).orderByActivityId().asc();
      statistics = query.list();

      assertEquals(3, query.count());
      assertEquals(3, statistics.size());

      // end
      assertActivityStatistics(statistics.get(0), "end", 0, 0, 2);

      // start
      assertActivityStatistics(statistics.get(1), "start", 0, 0, 2);

      // task
      assertActivityStatistics(statistics.get(2), "task", 0, 0, 2);

      //check February by finished dates
      query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .finishedAfter(sdf.parse("01.02.2016 00:00:00")).finishedBefore(sdf.parse("28.02.2016 23:59:59")).orderByActivityId().asc();
      statistics = query.list();

      assertEquals(3, query.count());
      assertEquals(3, statistics.size());

      // end
      assertActivityStatistics(statistics.get(0), "end", 0, 0, 2);

      // start
      assertActivityStatistics(statistics.get(1), "start", 0, 0, 4);

      // task
      assertActivityStatistics(statistics.get(2), "task", 0, 2, 4);

      //check March by started dates
      query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .startedAfter(sdf.parse("01.03.2016 00:00:00")).orderByActivityId().asc();
      statistics = query.list();

      assertEquals(2, query.count());
      assertEquals(2, statistics.size());

      // start
      assertActivityStatistics(statistics.get(0), "start", 0, 0, 2);

      // task
      assertActivityStatistics(statistics.get(1), "task", 2, 0, 0);

      //check March by finished dates
      query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .finishedAfter(sdf.parse("01.03.2016 00:00:00")).orderByActivityId().asc();
      statistics = query.list();

      assertEquals(0, query.count());
      assertEquals(0, statistics.size());

      //check whole period by started date
      query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId).includeCanceled().includeFinished()
        .startedAfter(sdf.parse("01.01.2016 00:00:00")).orderByActivityId().asc();
      statistics = query.list();

      assertEquals(3, query.count());
      assertEquals(3, statistics.size());

      // end
      assertActivityStatistics(statistics.get(0), "end", 0, 0, 2);

      // start
      assertActivityStatistics(statistics.get(1), "start", 0, 0, 6);

      // task
      assertActivityStatistics(statistics.get(2), "task", 2, 2, 4);

    } finally {
      ClockUtil.reset();
    }

  }

  protected void assertActivityStatistics(HistoricActivityStatistics activity, String activityName, long instances, long canceled, long finished) {
    assertEquals(activityName, activity.getId());
    assertEquals(instances, activity.getInstances());
    assertEquals(canceled, activity.getCanceled());
    assertEquals(finished, activity.getFinished());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByCanceledAndCompleteScope() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);

    // cancel running process instances
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance processInstance : processInstances) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    }

    startProcesses(2);

    // complete running tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    startProcesses(2);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled()
        .includeCompleteScope()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(2, query.count());
    assertEquals(2, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(0, end.getCanceled());
    assertEquals(2, end.getCompleteScope());

    // task
    HistoricActivityStatistics task = statistics.get(1);

    assertEquals("task", task.getId());
    assertEquals(2, task.getInstances());
    assertEquals(2, task.getCanceled());
    assertEquals(0, task.getCompleteScope());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByFinishedAndCompleteScope() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);

    // cancel running process instances
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance processInstance : processInstances) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    }

    startProcesses(2);

    // complete running tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    startProcesses(2);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCompleteScope()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(3, query.count());
    assertEquals(3, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(2, end.getFinished());
    assertEquals(2, end.getCompleteScope());

    // start
    HistoricActivityStatistics start = statistics.get(1);

    assertEquals("start", start.getId());
    assertEquals(0, start.getInstances());
    assertEquals(6, start.getFinished());
    assertEquals(0, start.getCompleteScope());

    // task
    HistoricActivityStatistics task = statistics.get(2);

    assertEquals("task", task.getId());
    assertEquals(2, task.getInstances());
    assertEquals(4, task.getFinished());
    assertEquals(0, task.getCompleteScope());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByFinishedAndCompleteScopeAndCanceled() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);

    // cancel running process instances
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance processInstance : processInstances) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    }

    startProcesses(2);

    // complete running tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    startProcesses(2);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCompleteScope()
        .includeCanceled()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(3, query.count());
    assertEquals(3, statistics.size());

    // end
    HistoricActivityStatistics end = statistics.get(0);

    assertEquals("end", end.getId());
    assertEquals(0, end.getInstances());
    assertEquals(0, end.getCanceled());
    assertEquals(2, end.getFinished());
    assertEquals(2, end.getCompleteScope());

    // start
    HistoricActivityStatistics start = statistics.get(1);

    assertEquals("start", start.getId());
    assertEquals(0, start.getInstances());
    assertEquals(0, start.getCanceled());
    assertEquals(6, start.getFinished());
    assertEquals(0, start.getCompleteScope());

    // task
    HistoricActivityStatistics task = statistics.get(2);

    assertEquals("task", task.getId());
    assertEquals(2, task.getInstances());
    assertEquals(2, task.getCanceled());
    assertEquals(4, task.getFinished());
    assertEquals(0, task.getCompleteScope());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryByProcessInstanceIds() {
    // given
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(3);
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String cancelledInstance = processInstances.get(0).getId();
    String completedInstance = processInstances.get(1).getId();

    runtimeService.deleteProcessInstance(cancelledInstance, "test");
    Task task = taskService.createTaskQuery().processInstanceId(completedInstance).singleResult();
    taskService.complete(task.getId());

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .processInstanceIdIn(cancelledInstance, completedInstance) // excluding the third running instance
        .includeFinished()
        .includeCompleteScope()
        .includeCanceled()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    // end
    HistoricActivityStatistics endStats = statistics.get(0);

    assertEquals("end", endStats.getId());
    assertEquals(0, endStats.getInstances());
    assertEquals(0, endStats.getCanceled());
    assertEquals(1, endStats.getFinished());
    assertEquals(1, endStats.getCompleteScope());

    // start
    HistoricActivityStatistics startStats = statistics.get(1);

    assertEquals("start", startStats.getId());
    assertEquals(0, startStats.getInstances());
    assertEquals(0, startStats.getCanceled());
    assertEquals(2, startStats.getFinished());
    assertEquals(0, startStats.getCompleteScope());

    // task
    HistoricActivityStatistics taskStats = statistics.get(2);

    assertEquals("task", taskStats.getId());
    assertEquals(0, taskStats.getInstances());
    assertEquals(1, taskStats.getCanceled());
    assertEquals(2, taskStats.getFinished());
    assertEquals(0, taskStats.getCompleteScope());
  }

  @Test
  public void testCheckProcessInstanceIdsForNull() {
    // given
    HistoricActivityStatisticsQuery query = historyService
    .createHistoricActivityStatisticsQuery("foo");

    // when 1
    try {
      query.processInstanceIdIn((String[]) null);
      fail("exception expected");
    } catch (NullValueException e) {
      // then 1
      testRule.assertTextPresent("processInstanceIds is null", e.getMessage());
    }

    // when 2
    try {
      query.processInstanceIdIn((String) null);
      fail("exception expected");
    } catch (NullValueException e) {
      // then 2
      testRule.assertTextPresent("processInstanceIds contains null value", e.getMessage());
    }
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testSorting() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId);

    assertEquals(1, query.orderByActivityId().asc().list().size());
    assertEquals(1, query.orderByActivityId().desc().list().size());

    assertEquals(1, query.orderByActivityId().asc().count());
    assertEquals(1, query.orderByActivityId().desc().count());
  }

  @Deployment(resources= {"org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testAnotherSingleTask.bpmn20.xml"})
  @Test
  public void testDifferentProcessesWithSameActivityId() {
    String processDefinitionId = getProcessDefinitionId();
    String anotherProcessDefinitionId = getProcessDefinitionIdByKey("anotherProcess");

    startProcesses(5);

    startProcessesByKey(10, "anotherProcess");

    // first processDefinition
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId);

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    HistoricActivityStatistics task = statistics.get(0);
    assertEquals(5, task.getInstances());

    // second processDefinition
    query = historyService
        .createHistoricActivityStatisticsQuery(anotherProcessDefinitionId);

    statistics = query.list();

    assertEquals(1, query.count());
    assertEquals(1, statistics.size());

    task = statistics.get(0);
    assertEquals(10, task.getInstances());

  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidents() {
    // given
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(4);
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String processInstanceWithResolvedIncidents = processInstances.get(1).getId();
    String processInstanceWithDeletedIncident = processInstances.get(2).getId();
    String processInstanceWithOpenIncident = processInstances.get(3).getId();

    Incident resolvedIncident = createIncident(processInstanceWithResolvedIncidents);
    runtimeService.resolveIncident(resolvedIncident.getId());
    resolvedIncident = createIncident(processInstanceWithResolvedIncidents);
    runtimeService.resolveIncident(resolvedIncident.getId());

    createIncident(processInstanceWithDeletedIncident);
    runtimeService.deleteProcessInstance(processInstances.get(2).getId(), "test");

    createIncident(processInstanceWithOpenIncident);

    // when
    final HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCanceled()
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(2, statistics.size());

    // start
    assertActivityStatistics(statistics.get(0), "start", 0, 0, 4, 0, 0, 0);

    // task
    assertActivityStatistics(statistics.get(1), "task", 3, 1, 1, 1, 2, 1);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidentsDeletedOnlyAndProcessInstanceIds() {
    // given
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(3);
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String processInstanceWithResolvedIncident = processInstances.get(1).getId();
    String processInstanceWithDeletedIncident = processInstances.get(2).getId();

    runtimeService.resolveIncident(createIncident(processInstanceWithResolvedIncident).getId());

    createIncident(processInstanceWithDeletedIncident);
    createIncident(processInstanceWithDeletedIncident);
    createIncident(processInstanceWithDeletedIncident);
    runtimeService.deleteProcessInstance(processInstanceWithDeletedIncident, "test");

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .processInstanceIdIn(processInstanceWithDeletedIncident)
        .includeFinished()
        .includeCompleteScope()
        .includeCanceled()
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(2, statistics.size());

    // start
    assertActivityStatistics(statistics.get(0), "start", 0, 0, 1, 0, 0, 0);

    // task
    assertActivityStatistics(statistics.get(1), "task", 0, 1, 1, 0, 0, 3);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidentsWhenNoIncidents() {
    // given
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();

    runtimeService.deleteProcessInstance(processInstances.get(0).getId(), null);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCompleteScope()
        .includeCanceled()
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(2, statistics.size());

    // start
    assertActivityStatistics(statistics.get(0), "start", 0, 0, 2, 0, 0, 0);

    // task
    assertActivityStatistics(statistics.get(1), "task", 1, 1, 1, 0, 0, 0);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testMultipleRunningTasks.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidentsMultipleRunningTasksDeletedOnly() {
    // given
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String cancelledInstanceWithIncident = processInstances.get(0).getId();

     List<Execution> executions = runtimeService.createExecutionQuery()
        .processInstanceId(cancelledInstanceWithIncident)
        .activityId("innerTask").active().list();
    runtimeService.createIncident("foo1", executions.get(0).getId(), ((ExecutionEntity) executions.get(0)).getActivityId(), "bar1");
    runtimeService.createIncident("foo2", executions.get(1).getId(), ((ExecutionEntity) executions.get(1)).getActivityId(), "bar2");
    runtimeService.deleteProcessInstance(cancelledInstanceWithIncident, "test");

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCompleteScope()
        .includeCanceled()
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(7, statistics.size());

    assertActivityStatistics(statistics.get(0), "gtw", 0, 0, 2, 0, 0, 0);
    assertActivityStatistics(statistics.get(1), "innerStart", 0, 0, 10, 0, 0, 0);
    assertActivityStatistics(statistics.get(2), "innerTask", 5, 5, 5, 0, 0, 2);
    assertActivityStatistics(statistics.get(3), "start", 0, 0, 2, 0, 0, 0);
    assertActivityStatistics(statistics.get(4), "subprocess", 5, 5, 5, 0, 0, 0);
    assertActivityStatistics(statistics.get(5), "subprocess#multiInstanceBody", 1, 1, 1, 0, 0, 0);
    assertActivityStatistics(statistics.get(6), "task", 1, 1, 1, 0, 0, 0);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testMultipleRunningTasks.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidentsMultipleRunningTasksOpenOnly() {
    // given
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(2);
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String cancelledInstanceWithIncident = processInstances.get(0).getId();

     List<Execution> executions = runtimeService.createExecutionQuery()
        .processInstanceId(cancelledInstanceWithIncident)
        .activityId("innerTask").active().list();
    runtimeService.createIncident("foo1", executions.get(0).getId(), ((ExecutionEntity) executions.get(0)).getActivityId(), "bar1");
    runtimeService.createIncident("foo1", executions.get(0).getId(), ((ExecutionEntity) executions.get(0)).getActivityId(), "bar1");
    runtimeService.createIncident("foo2", executions.get(1).getId(), ((ExecutionEntity) executions.get(1)).getActivityId(), "bar2");

    executions = runtimeService.createExecutionQuery()
        .processInstanceId(cancelledInstanceWithIncident)
        .activityId("task").active().list();
    runtimeService.createIncident("foo", executions.get(0).getId(), ((ExecutionEntity) executions.get(0)).getActivityId(), "bar");

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(4, statistics.size());

    assertActivityStatistics(statistics.get(0), "innerTask", 10, 0, 0, 3, 0, 0);
    assertActivityStatistics(statistics.get(1), "subprocess", 10, 0, 0, 0, 0, 0);
    assertActivityStatistics(statistics.get(2), "subprocess#multiInstanceBody", 2, 0, 0, 0, 0, 0);
    assertActivityStatistics(statistics.get(3), "task", 2, 0, 0, 1, 0, 0);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryCancelledIncludeIncidentsDeletedOnly() throws ParseException {
    try {
      // given
      String processDefinitionId = getProcessDefinitionId();

      // start two instances with one incident and cancel them
      ClockUtil.setCurrentTime(sdf.parse("5.10.2019 12:00:00"));
      startProcessesByKey(2, "process");
      List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
      String firstInstance = processInstances.get(0).getId();
      createIncident(firstInstance);
      cancelProcessInstances();

      // start another two instances with two incidents and cancel them
      ClockUtil.setCurrentTime(sdf.parse("20.10.2019 12:00:00"));
      startProcessesByKey(2, "process");
      processInstances = runtimeService.createProcessInstanceQuery().list();
      String thirdInstance = processInstances.get(0).getId();
      String fourthInstance = processInstances.get(1).getId();
      createIncident(thirdInstance);
      createIncident(fourthInstance);
      cancelProcessInstances();

      // when
      final HistoricActivityStatisticsQuery query = historyService
          .createHistoricActivityStatisticsQuery(processDefinitionId)
          .startedAfter(sdf.parse("01.10.2019 12:00:00"))
          .startedBefore(sdf.parse("10.10.2019 12:00:00"))
          .includeFinished()
          .includeCompleteScope()
          .includeCanceled()
          .includeIncidents()
          .orderByActivityId()
          .asc();
      List<HistoricActivityStatistics> statistics = query.list();

      // then results only from the first two instances
      assertEquals(2, statistics.size());
      assertActivityStatistics(statistics.get(0), "start", 0, 0, 2, 0, 0, 0);
      assertActivityStatistics(statistics.get(1), "task", 0, 2, 2, 0, 0, 1);
    } finally {
      ClockUtil.reset();
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  @Test
  public void testQueryCompletedIncludeIncidentsDeletedOnly() throws ParseException {
    try {
      // given
      String processDefinitionId = getProcessDefinitionId();

      // start two instances with one incident and complete them
      ClockUtil.setCurrentTime(sdf.parse("5.10.2019 12:00:00"));
      startProcessesByKey(2, "process");
      List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
      String firstInstance = processInstances.get(0).getId();
      createIncident(firstInstance);
      completeProcessInstances();

      // start another two instances with two incidents and complete them
      ClockUtil.setCurrentTime(sdf.parse("20.10.2019 12:00:00"));
      startProcessesByKey(2, "process");
      processInstances = runtimeService.createProcessInstanceQuery().list();
      String thirdInstance = processInstances.get(0).getId();
      createIncident(thirdInstance);
      completeProcessInstances();

      // when
      final HistoricActivityStatisticsQuery query = historyService
          .createHistoricActivityStatisticsQuery(processDefinitionId)
          .finishedAfter(sdf.parse("10.10.2019 12:00:00"))
          .finishedBefore(sdf.parse("30.10.2019 12:00:00"))
          .includeFinished()
          .includeCompleteScope()
          .includeCanceled()
          .includeIncidents()
          .orderByActivityId()
          .asc();
      List<HistoricActivityStatistics> statistics = query.list();

      // then results only from the second two instances
      assertEquals(3, statistics.size());
      assertActivityStatistics(statistics.get(0), "end", 0, 0, 2, 0, 0, 0);
      assertActivityStatistics(statistics.get(1), "start", 0, 0, 2, 0, 0, 0);
      assertActivityStatistics(statistics.get(2), "task", 0, 0, 2, 0, 0, 1);
    } finally {
      ClockUtil.reset();
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidentsWhenNoHistoricActivityInstanceDeletedOnly() {
    // given
    startProcessesByKey(3, "failingProcess");

    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      managementService.setJobRetries(job.getId(), 0);
    }

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String cancelledInstance1 = processInstances.get(1).getId();
    String cancelledInstance2 = processInstances.get(2).getId();
    String processDefinitionId = processInstances.get(0).getProcessDefinitionId();

    runtimeService.deleteProcessInstances(java.util.Arrays.asList(cancelledInstance1, cancelledInstance2), "test", false, false);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(2, statistics.size());

    assertActivityStatistics(statistics.get(0), "serviceTask", 0, 0, 0, 1, 0, 2);
    assertActivityStatistics(statistics.get(1), "start", 0, 0, 3, 0, 0, 0);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources="org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml")
  @Test
  public void testQueryIncludeIncidentsWhenNoHistoricActivityInstanceWithoutFilters() {
    // given
    startProcessesByKey(3, "failingProcess");

    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      managementService.setJobRetries(job.getId(), 0);
    }

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String cancelledInstance1 = processInstances.get(1).getId();
    String cancelledInstance2 = processInstances.get(2).getId();
    String processDefinitionId = processInstances.get(0).getProcessDefinitionId();

    runtimeService.deleteProcessInstances(java.util.Arrays.asList(cancelledInstance1, cancelledInstance2), "test", false, false);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .orderByActivityId()
        .asc();
    List<HistoricActivityStatistics> statistics = query.list();

    // then
    assertEquals(1, statistics.size());

    assertActivityStatistics(statistics.get(0), "serviceTask", 0, 0, 0, 1, 0, 2);
  }

  protected void completeProcessInstances() {
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
  }

  protected void cancelProcessInstances() {
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance pi : processInstances) {
      runtimeService.deleteProcessInstance(pi.getId(), "test");
    }
  }

  protected void startProcesses(int numberOfInstances) {
    startProcessesByKey(numberOfInstances, "process");
  }

  protected void startProcessesByKey(int numberOfInstances, String key) {
    for (int i = 0; i < numberOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(key);
    }
  }

  protected String getProcessDefinitionIdByKey(String key) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).singleResult().getId();
  }

  protected String getProcessDefinitionId() {
    return getProcessDefinitionIdByKey("process");
  }

  protected Incident createIncident(String instanceId) {
    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(instanceId).active().singleResult();
    return runtimeService.createIncident("foo", execution.getId(), execution.getActivityId(), "exec" + execution.getId());
  }

  protected void assertActivityStatistics(HistoricActivityStatistics activity, String activityName, int instances, int canceled, int finished, int openIncidents, int resolvedIncidents, int deletedIncidents) {
    assertActivityStatistics(activity, activityName, instances, canceled, finished);
    assertEquals(openIncidents, activity.getOpenIncidents());
    assertEquals(resolvedIncidents, activity.getResolvedIncidents());
    assertEquals(deletedIncidents, activity.getDeletedIncidents());
  }

}
