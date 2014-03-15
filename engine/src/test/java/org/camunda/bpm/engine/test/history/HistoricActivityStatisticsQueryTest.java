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

import java.util.List;

import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricActivityStatisticsQueryTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
  public void testNoRunningProcessInstances() {
    String processDefinitionId = getProcessDefinitionId();

    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);
    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(0, query.count());
    assertEquals(0, statistics.size());
  }

  @Deployment
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
  public void testMultipleRunningTasks() {
    String processDefinitionId = getProcessDefinitionId();

    startProcesses(5);

    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .orderByActivityId()
        .asc();

    List<HistoricActivityStatistics> statistics = query.list();

    assertEquals(3, query.count());
    assertEquals(3, statistics.size());

    // innerTask
    HistoricActivityStatistics innerTask = statistics.get(0);

    assertEquals("innerTask", innerTask.getId());
    assertEquals(25, innerTask.getInstances());

    // subprocess
    HistoricActivityStatistics subProcess = statistics.get(1);

    assertEquals("subprocess", subProcess.getId());
    assertEquals(25, subProcess.getInstances());

    // task
    HistoricActivityStatistics task = statistics.get(2);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());

    completeProcessInstances();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.calledProcess.bpmn20.xml" })
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

    assertEquals(8, query.count());
    assertEquals(8, statistics.size());

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

    // task
    HistoricActivityStatistics task = statistics.get(7);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());
    assertEquals(0, task.getFinished());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
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

    assertEquals(3, query.count());
    assertEquals(3, statistics.size());

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

    // task
    HistoricActivityStatistics task = statistics.get(2);

    assertEquals("task", task.getId());
    assertEquals(5, task.getInstances());
    assertEquals(0, task.getCompleteScope());

    completeProcessInstances();
  }

  @Deployment(resources="org/camunda/bpm/engine/test/history/HistoricActivityStatisticsQueryTest.testSingleTask.bpmn20.xml")
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

}
