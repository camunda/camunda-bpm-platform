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

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricActivityInstanceSequenceCounterTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testSequence.bpmn20.xml"})
  public void testSequence() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);

    // then
    verifyOrder(query, 4, Arrays.asList("theStart", "theService1", "theService2", "theEnd"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testForkSameSequenceLength.bpmn20.xml"})
  public void testFork() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    JobQuery jobQuery = managementService.createJobQuery();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    Job firstJob = jobQuery.activityId("theEnd1").singleResult();
    String firstExecutionId = firstJob.getExecutionId();
    Job secondJob = jobQuery.activityId("theEnd2").singleResult();
    String secondExecutionId = secondJob.getExecutionId();

    managementService.executeJob(firstJob.getId());
    managementService.executeJob(secondJob.getId());

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, 3, Arrays.asList("theStart", "theService", "theEnd2"));

    query.executionId(firstExecutionId);
    verifyOrder(query, 2, Arrays.asList("theService1", "theEnd1"));

    query.executionId(secondExecutionId);
    verifyOrder(query, 2, Arrays.asList("fork", "theService2"));

    query = historyService
      .createHistoricActivityInstanceQuery()
      .orderBySequenceCounter()
      .asc()
      .orderByActivityId()
      .asc();
    verifyOrder(query, 7, Arrays.asList("theStart", "theService", "fork", "theService1", "theService2", "theEnd1", "theEnd2"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testForkAndJoinDifferentSequenceLength.bpmn20.xml"})
  public void testForkAndJoin() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    executeAvailableJobs();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, 5, Arrays.asList("theStart", "theService", "join", "theService4", "theEnd"));

    String firstExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theService1")
        .singleResult()
        .getExecutionId();

    String secondExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theService2")
        .singleResult()
        .getExecutionId();

    query.executionId(firstExecutionId);
    if (query.count() == 1) {

      verifyOrder(query, 1, Arrays.asList("theService1"));

      query.executionId(secondExecutionId);
      verifyOrder(query, 4, Arrays.asList("fork", "theService2", "theService3", "join"));
    }
    else {
      verifyOrder(query, 2, Arrays.asList("theService1", "join"));

      query.executionId(secondExecutionId);
      verifyOrder(query, 3, Arrays.asList("fork", "theService2", "theService3"));
    }

    query = historyService
      .createHistoricActivityInstanceQuery()
      .orderBySequenceCounter()
      .asc()
      .orderByActivityId()
      .asc();
    verifyOrder(query, 10, Arrays.asList("theStart", "theService", "fork", "theService1", "theService2", "join", "theService3", "join", "theService4", "theEnd"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testSequenceInsideSubProcess.bpmn20.xml"})
  public void testSequenceInsideSubProcess() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    executeAvailableJobs();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, 4, Arrays.asList("theStart", "theService1", "theService2", "theEnd"));

    String subProcessExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("subProcess")
        .singleResult()
        .getExecutionId();

    query.executionId(subProcessExecutionId);
    verifyOrder(query, 4, Arrays.asList("subProcess", "innerStart", "innerService", "innerEnd"));

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();
    verifyOrder(query, 8, Arrays.asList("theStart", "theService1", "subProcess", "innerStart", "innerService", "innerEnd", "theService2", "theEnd"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testSequentialMultiInstance.bpmn20.xml"})
  public void testSequentialMultiInstance() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    executeAvailableJobs();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, 4, Arrays.asList("theStart", "theService1", "theService2", "theEnd"));

    String taskExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask")
        .list()
        .get(0)
        .getExecutionId();

    query.executionId(taskExecutionId);
    verifyOrder(query, 2, Arrays.asList("theTask", "theTask"));

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();
    verifyOrder(query, 6, Arrays.asList("theStart", "theService1", "theTask", "theTask", "theService2", "theEnd"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testParallelMultiInstance.bpmn20.xml"})
  public void testParallelMultiInstance() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    executeAvailableJobs();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, 4, Arrays.asList("theStart", "theService1", "theService2", "theEnd"));

    List<HistoricActivityInstance> taskActivityInstances = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask")
        .list();
    for (HistoricActivityInstance activityInstance : taskActivityInstances) {
      query.executionId(activityInstance.getExecutionId());
      verifyOrder(query, 1, Arrays.asList("theTask"));
    }

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();
    verifyOrder(query, 6, Arrays.asList("theStart", "theService1", "theTask", "theTask", "theService2", "theEnd"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testLoop.bpmn20.xml"})
  public void testLoop() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    executeAvailableJobs();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, 10, Arrays.asList("theStart", "theService1", "join", "theScript", "fork", "join", "theScript", "fork", "theService2", "theEnd"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testInterruptingBoundaryEvent.bpmn20.xml"})
  public void testInterruptingBoundaryEvent() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    runtimeService.correlateMessage("newMessage");
    executeAvailableJobs();

    // then
    verifyOrder(query, 6, Arrays.asList("theStart", "theService1", "theTask", "messageBoundary", "theServiceAfterMessage", "theEnd2"));

    query.executionId(processInstanceId);
    verifyOrder(query, 5, Arrays.asList("theStart", "theService1", "messageBoundary", "theServiceAfterMessage", "theEnd2"));

    String taskExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask")
        .singleResult()
        .getExecutionId();

    query.executionId(taskExecutionId);
    verifyOrder(query, 1, Arrays.asList("theTask"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testNonInterruptingBoundaryEvent.bpmn20.xml"})
  public void testNonInterruptingBoundaryEvent() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderBySequenceCounter()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    runtimeService.correlateMessage("newMessage");
    runtimeService.correlateMessage("newMessage");
    executeAvailableJobs();
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
    executeAvailableJobs();

    // then
    verifyOrder(query, 10, Arrays.asList("theStart", "theService1", "theTask", "messageBoundary", "messageBoundary", "theServiceAfterMessage", "theServiceAfterMessage", "theEnd2", "theEnd2", "theEnd1"));

    query.executionId(processInstanceId);
    verifyOrder(query, 3, Arrays.asList("theStart", "theService1", "theEnd1"));

    String taskExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask")
        .singleResult()
        .getExecutionId();

    query.executionId(taskExecutionId);
    verifyOrder(query, 1, Arrays.asList("theTask"));

    List<HistoricActivityInstance> activityInstances = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("messageBoundary")
        .list();
    for (HistoricActivityInstance historicActivityInstance : activityInstances) {
      query.executionId(historicActivityInstance.getExecutionId());
      verifyOrder(query, 3, Arrays.asList("messageBoundary", "theServiceAfterMessage", "theEnd2"));
    }
  }

  protected void verifyOrder(HistoricActivityInstanceQuery query, int countExpected, List<String> expectedActivityInstanceOrder) {
    assertEquals(countExpected, query.count());
    assertEquals(countExpected, expectedActivityInstanceOrder.size());

    List<HistoricActivityInstance> activityInstances = query.list();

    for (int i = 0; i < countExpected; i++) {
      HistoricActivityInstance activityInstance = activityInstances.get(i);
      String currentActivityId = activityInstance.getActivityId();
      String expectedActivityId = expectedActivityInstanceOrder.get(i);
      assertEquals(expectedActivityId, currentActivityId);
    }

  }

}
