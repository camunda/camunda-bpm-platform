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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricActivityInstanceSequenceCounterTest extends PluggableProcessEngineTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testSequence.bpmn20.xml"})
  @Test
  public void testSequence() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyOrder(query, "theStart", "theService1", "theService2", "theEnd");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testForkSameSequenceLengthWithoutWaitStates.bpmn20.xml"})
  @Test
  public void testFork() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService", "fork", "theService2", "theEnd2");

    String firstExecutionId = historyService.createHistoricActivityInstanceQuery().activityId("theService1").singleResult().getExecutionId();
    query.executionId(firstExecutionId);
    verifyOrder(query, "theService1", "theEnd1");

    query = historyService
      .createHistoricActivityInstanceQuery()
      .orderPartiallyByOccurrence()
      .asc();
    verifyOrder(query, "theStart", "theService", "fork", "theService1", "theEnd1", "theService2", "theEnd2");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testForkAndJoinDifferentSequenceLength.bpmn20.xml"})
  @Test
  public void testForkAndJoin() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService", "fork", "join", "theService4", "theEnd");

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
    verifyOrder(query, "theService1", "join");

    query.executionId(secondExecutionId);
    verifyOrder(query, "theService2", "theService3");

    query = historyService
      .createHistoricActivityInstanceQuery()
      .orderPartiallyByOccurrence()
      .asc()
      .orderByActivityId()
      .asc();
    verifyOrder(query, "theStart", "theService", "fork", "theService1", "theService2", "join", "theService3", "join", "theService4", "theEnd");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testSequenceInsideSubProcess.bpmn20.xml"})
  @Test
  public void testSequenceInsideSubProcess() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService1", "theService2", "theEnd");

    String subProcessExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("subProcess")
        .singleResult()
        .getExecutionId();

    query.executionId(subProcessExecutionId);
    verifyOrder(query, "subProcess", "innerStart", "innerService", "innerEnd");

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();
    verifyOrder(query, "theStart", "theService1", "subProcess", "innerStart", "innerService", "innerEnd", "theService2", "theEnd");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testSequentialMultiInstance.bpmn20.xml"})
  @Test
  public void testSequentialMultiInstance() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService1", "theService3", "theEnd");

    String taskExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theService2")
        .list()
        .get(0)
        .getExecutionId();

    query.executionId(taskExecutionId);
    verifyOrder(query, "theService2#multiInstanceBody", "theService2", "theService2");

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();
    verifyOrder(query, "theStart", "theService1", "theService2#multiInstanceBody", "theService2", "theService2", "theService3", "theEnd");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testParallelMultiInstance.bpmn20.xml"})
  @Test
  public void testParallelMultiInstance() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService1", "theService3", "theEnd");

    List<HistoricActivityInstance> taskActivityInstances = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theService2")
        .list();
    for (HistoricActivityInstance activityInstance : taskActivityInstances) {
      query.executionId(activityInstance.getExecutionId());
      verifyOrder(query, "theService2");
    }

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();
    verifyOrder(query, "theStart", "theService1", "theService2#multiInstanceBody", "theService2", "theService2", "theService3", "theEnd");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testLoop.bpmn20.xml"})
  @Test
  public void testLoop() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService1", "join", "theScript", "fork", "join", "theScript", "fork", "theService2", "theEnd");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testInterruptingBoundaryEvent.bpmn20.xml"})
  @Test
  public void testInterruptingBoundaryEvent() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    runtimeService.correlateMessage("newMessage");

    // then
    verifyOrder(query, "theStart", "theService1", "theTask", "messageBoundary", "theServiceAfterMessage", "theEnd2");

    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService1", "messageBoundary", "theServiceAfterMessage", "theEnd2");

    String taskExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask")
        .singleResult()
        .getExecutionId();

    query.executionId(taskExecutionId);
    verifyOrder(query, "theTask");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testNonInterruptingBoundaryEvent.bpmn20.xml"})
  @Test
  public void testNonInterruptingBoundaryEvent() {
    // given
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    runtimeService.correlateMessage("newMessage");
    runtimeService.correlateMessage("newMessage");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    // then
    query.executionId(processInstanceId);
    verifyOrder(query, "theStart", "theService1", "theEnd1");

    String taskExecutionId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("theTask")
        .singleResult()
        .getExecutionId();

    query.executionId(taskExecutionId);
    verifyOrder(query, "theTask");

    List<HistoricActivityInstance> activityInstances = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("messageBoundary")
        .list();
    for (HistoricActivityInstance historicActivityInstance : activityInstances) {
      query.executionId(historicActivityInstance.getExecutionId());
      verifyOrder(query, "messageBoundary", "theServiceAfterMessage", "theEnd2");
    }

    query = historyService
        .createHistoricActivityInstanceQuery()
        .orderPartiallyByOccurrence()
        .asc()
        .orderByActivityId()
        .asc();

    verifyOrder(query, "theStart", "theService1", "messageBoundary", "theTask", "theServiceAfterMessage", "theEnd2", "messageBoundary", "theServiceAfterMessage", "theEnd2", "theEnd1");
  }

  protected void verifyOrder(HistoricActivityInstanceQuery query, String... expectedOrder) {
    assertEquals(expectedOrder.length, query.count());

    List<HistoricActivityInstance> activityInstances = query.list();

    for (int i = 0; i < expectedOrder.length; i++) {
      HistoricActivityInstance activityInstance = activityInstances.get(i);
      String currentActivityId = activityInstance.getActivityId();
      String expectedActivityId = expectedOrder[i];
      assertEquals(expectedActivityId, currentActivityId);
    }

  }

}
