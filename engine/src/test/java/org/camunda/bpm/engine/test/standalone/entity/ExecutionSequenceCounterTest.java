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
package org.camunda.bpm.engine.test.standalone.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.standalone.entity.ExecutionOrderListener.ActivitySequenceCounterMap;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ExecutionSequenceCounterTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() {
    ExecutionOrderListener.clearActivityExecutionOrder();
  }

  @Deployment
  @Test
  public void testSequence() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "theService2", "theEnd");
  }

  @Deployment
  @Test
  public void testForkSameSequenceLengthWithoutWaitStates() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService", "fork", "theService1", "theEnd1", "theService2", "theEnd2");
  }

  @Deployment
  @Test
  public void testForkSameSequenceLengthWithAsyncEndEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(5, order.size());

    long lastSequenceCounter = 0;

    ActivitySequenceCounterMap theStartElement = order.get(0);
    assertEquals("theStart", theStartElement.getActivityId());
    assertTrue(theStartElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theStartElement.getSequenceCounter();

    ActivitySequenceCounterMap theForkElement = order.get(1);
    assertEquals("theService", theForkElement.getActivityId());
    assertTrue(theForkElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theForkElement.getSequenceCounter();

    ActivitySequenceCounterMap theServiceElement = order.get(2);
    assertEquals("fork", theServiceElement.getActivityId());
    assertTrue(theServiceElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theServiceElement.getSequenceCounter();

    ActivitySequenceCounterMap theService1Element = order.get(3);
    assertEquals("theService1", theService1Element.getActivityId());
    assertTrue(theService1Element.getSequenceCounter() > lastSequenceCounter);

    ActivitySequenceCounterMap theService2Element = order.get(4);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > lastSequenceCounter);

    // when (2)
    String jobId = jobQuery.activityId("theEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(6, order.size());

    ActivitySequenceCounterMap theEnd1Element = order.get(5);
    assertEquals("theEnd1", theEnd1Element.getActivityId());
    assertTrue(theEnd1Element.getSequenceCounter() > theService1Element.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    testRule.assertProcessEnded(processInstanceId);

    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(7, order.size());

    ActivitySequenceCounterMap theEnd2Element = order.get(6);
    assertEquals("theEnd2", theEnd2Element.getActivityId());
    assertTrue(theEnd2Element.getSequenceCounter() > theService2Element.getSequenceCounter());
  }

  @Deployment
  @Test
  public void testForkDifferentSequenceLengthWithoutWaitStates() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService", "fork", "theService1", "theEnd1", "theService2", "theService3", "theEnd2");

  }

  @Deployment
  @Test
  public void testForkDifferentSequenceLengthWithAsyncEndEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(6, order.size());

    long lastSequenceCounter = 0;

    ActivitySequenceCounterMap theStartElement = order.get(0);
    assertEquals("theStart", theStartElement.getActivityId());
    assertTrue(theStartElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theStartElement.getSequenceCounter();

    ActivitySequenceCounterMap theForkElement = order.get(1);
    assertEquals("theService", theForkElement.getActivityId());
    assertTrue(theForkElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theForkElement.getSequenceCounter();

    ActivitySequenceCounterMap theServiceElement = order.get(2);
    assertEquals("fork", theServiceElement.getActivityId());
    assertTrue(theServiceElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theServiceElement.getSequenceCounter();

    ActivitySequenceCounterMap theService1Element = order.get(3);
    assertEquals("theService1", theService1Element.getActivityId());
    assertTrue(theService1Element.getSequenceCounter() > lastSequenceCounter);

    ActivitySequenceCounterMap theService2Element = order.get(4);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > lastSequenceCounter);

    ActivitySequenceCounterMap theService3Element = order.get(5);
    assertEquals("theService3", theService3Element.getActivityId());
    assertTrue(theService3Element.getSequenceCounter() > theService2Element.getSequenceCounter() );

    // when (2)
    String jobId = jobQuery.activityId("theEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(7, order.size());

    ActivitySequenceCounterMap theEnd1Element = order.get(6);
    assertEquals("theEnd1", theEnd1Element.getActivityId());
    assertTrue(theEnd1Element.getSequenceCounter() > theService1Element.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    testRule.assertProcessEnded(processInstanceId);

    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(8, order.size());

    ActivitySequenceCounterMap theEnd2Element = order.get(7);
    assertEquals("theEnd2", theEnd2Element.getActivityId());
    assertTrue(theEnd2Element.getSequenceCounter() > theService3Element.getSequenceCounter());
  }

  @Deployment
  @Test
  public void testForkReplaceBy() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(2, order.size());

    ActivitySequenceCounterMap theService1Element = order.get(0);
    assertEquals("theService1", theService1Element.getActivityId());

    ActivitySequenceCounterMap theService3Element = order.get(1);
    assertEquals("theService3", theService3Element.getActivityId());

    assertTrue(theService1Element.getSequenceCounter() == theService3Element.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("theService4").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(5, order.size());

    ActivitySequenceCounterMap theService4Element = order.get(2);
    assertEquals("theService4", theService4Element.getActivityId());
    assertTrue(theService4Element.getSequenceCounter() > theService3Element.getSequenceCounter());

    ActivitySequenceCounterMap theService5Element = order.get(3);
    assertEquals("theService5", theService5Element.getActivityId());
    assertTrue(theService5Element.getSequenceCounter() > theService4Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd2Element = order.get(4);
    assertEquals("theEnd2", theEnd2Element.getActivityId());
    assertTrue(theEnd2Element.getSequenceCounter() > theService5Element.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theService2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(7, order.size());

    ActivitySequenceCounterMap theService2Element = order.get(5);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > theService1Element.getSequenceCounter());
    assertTrue(theService2Element.getSequenceCounter() > theEnd2Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd1Element = order.get(6);
    assertEquals("theEnd1", theEnd1Element.getActivityId());
    assertTrue(theEnd1Element.getSequenceCounter() > theService2Element.getSequenceCounter());

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/standalone/entity/ExecutionSequenceCounterTest.testForkReplaceBy.bpmn20.xml"})
  @Test
  public void testForkReplaceByAnotherExecutionOrder() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(2, order.size());

    ActivitySequenceCounterMap theService1Element = order.get(0);
    assertEquals("theService1", theService1Element.getActivityId());

    ActivitySequenceCounterMap theService3Element = order.get(1);
    assertEquals("theService3", theService3Element.getActivityId());

    assertTrue(theService1Element.getSequenceCounter() == theService3Element.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("theService2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(4, order.size());

    ActivitySequenceCounterMap theService2Element = order.get(2);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > theService1Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd1Element = order.get(3);
    assertEquals("theEnd1", theEnd1Element.getActivityId());
    assertTrue(theEnd1Element.getSequenceCounter() > theService2Element.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theService4").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(7, order.size());

    ActivitySequenceCounterMap theService4Element = order.get(4);
    assertEquals("theService4", theService4Element.getActivityId());
    assertTrue(theService4Element.getSequenceCounter() > theService3Element.getSequenceCounter());
    assertTrue(theService4Element.getSequenceCounter() > theEnd1Element.getSequenceCounter());

    ActivitySequenceCounterMap theService5Element = order.get(5);
    assertEquals("theService5", theService5Element.getActivityId());
    assertTrue(theService5Element.getSequenceCounter() > theService4Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd2Element = order.get(6);
    assertEquals("theEnd2", theEnd2Element.getActivityId());
    assertTrue(theEnd2Element.getSequenceCounter() > theService5Element.getSequenceCounter());

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testForkReplaceByThreeBranches() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(3, order.size());

    ActivitySequenceCounterMap theService1Element = order.get(0);
    assertEquals("theService1", theService1Element.getActivityId());

    ActivitySequenceCounterMap theService3Element = order.get(1);
    assertEquals("theService3", theService3Element.getActivityId());

    ActivitySequenceCounterMap theService6Element = order.get(2);
    assertEquals("theService6", theService6Element.getActivityId());

    assertTrue(theService1Element.getSequenceCounter() == theService3Element.getSequenceCounter());
    assertTrue(theService3Element.getSequenceCounter() == theService6Element.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("theService2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(5, order.size());

    ActivitySequenceCounterMap theService2Element = order.get(3);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > theService1Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd1Element = order.get(4);
    assertEquals("theEnd1", theEnd1Element.getActivityId());
    assertTrue(theEnd1Element.getSequenceCounter() > theService2Element.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theService4").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(8, order.size());

    ActivitySequenceCounterMap theService4Element = order.get(5);
    assertEquals("theService4", theService4Element.getActivityId());
    assertTrue(theService4Element.getSequenceCounter() > theService3Element.getSequenceCounter());

    ActivitySequenceCounterMap theService5Element = order.get(6);
    assertEquals("theService5", theService5Element.getActivityId());
    assertTrue(theService5Element.getSequenceCounter() > theService4Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd2Element = order.get(7);
    assertEquals("theEnd2", theEnd2Element.getActivityId());
    assertTrue(theEnd2Element.getSequenceCounter() > theService5Element.getSequenceCounter());

    // when (4)
    jobId = jobQuery.activityId("theService7").singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(12, order.size());

    ActivitySequenceCounterMap theService7Element = order.get(8);
    assertEquals("theService7", theService7Element.getActivityId());
    assertTrue(theService7Element.getSequenceCounter() > theService6Element.getSequenceCounter());
    assertTrue(theService7Element.getSequenceCounter() > theEnd2Element.getSequenceCounter());

    ActivitySequenceCounterMap theService8Element = order.get(9);
    assertEquals("theService8", theService8Element.getActivityId());
    assertTrue(theService8Element.getSequenceCounter() > theService7Element.getSequenceCounter());

    ActivitySequenceCounterMap theService9Element = order.get(10);
    assertEquals("theService9", theService9Element.getActivityId());
    assertTrue(theService9Element.getSequenceCounter() > theService8Element.getSequenceCounter());

    ActivitySequenceCounterMap theEnd3Element = order.get(11);
    assertEquals("theEnd3", theEnd3Element.getActivityId());
    assertTrue(theEnd3Element.getSequenceCounter() > theService9Element.getSequenceCounter());

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testForkAndJoinSameSequenceLength() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(9, order.size());

    long lastSequenceCounter = 0;

    ActivitySequenceCounterMap theStartElement = order.get(0);
    assertEquals("theStart", theStartElement.getActivityId());
    assertTrue(theStartElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theStartElement.getSequenceCounter();

    ActivitySequenceCounterMap theForkElement = order.get(1);
    assertEquals("theService", theForkElement.getActivityId());
    assertTrue(theForkElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theForkElement.getSequenceCounter();

    ActivitySequenceCounterMap theServiceElement = order.get(2);
    assertEquals("fork", theServiceElement.getActivityId());
    assertTrue(theServiceElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theServiceElement.getSequenceCounter();

    ActivitySequenceCounterMap theService1Element = order.get(3);
    assertEquals("theService1", theService1Element.getActivityId());
    assertTrue(theService1Element.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theService1Element.getSequenceCounter();

    ActivitySequenceCounterMap theJoin1Element = order.get(4);
    assertEquals("join", theJoin1Element.getActivityId());
    assertTrue(theJoin1Element.getSequenceCounter() > lastSequenceCounter);

    lastSequenceCounter = theForkElement.getSequenceCounter();

    ActivitySequenceCounterMap theService2Element = order.get(5);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theService2Element.getSequenceCounter();

    ActivitySequenceCounterMap theJoin2Element = order.get(6);
    assertEquals("join", theJoin2Element.getActivityId());
    assertTrue(theJoin2Element.getSequenceCounter() > lastSequenceCounter);

    ActivitySequenceCounterMap theService3Element = order.get(7);
    assertEquals("theService3", theService3Element.getActivityId());
    assertTrue(theService3Element.getSequenceCounter() > theJoin1Element.getSequenceCounter());
    assertTrue(theService3Element.getSequenceCounter() > theJoin2Element.getSequenceCounter());
    lastSequenceCounter = theService3Element.getSequenceCounter();

    ActivitySequenceCounterMap theEndElement = order.get(8);
    assertEquals("theEnd", theEndElement.getActivityId());
    assertTrue(theEndElement.getSequenceCounter() > lastSequenceCounter);
  }

  @Deployment
  @Test
  public void testForkAndJoinDifferentSequenceLength() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(10, order.size());

    long lastSequenceCounter = 0;

    ActivitySequenceCounterMap theStartElement = order.get(0);
    assertEquals("theStart", theStartElement.getActivityId());
    assertTrue(theStartElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theStartElement.getSequenceCounter();

    ActivitySequenceCounterMap theForkElement = order.get(1);
    assertEquals("theService", theForkElement.getActivityId());
    assertTrue(theForkElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theForkElement.getSequenceCounter();

    ActivitySequenceCounterMap theServiceElement = order.get(2);
    assertEquals("fork", theServiceElement.getActivityId());
    assertTrue(theServiceElement.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theServiceElement.getSequenceCounter();

    ActivitySequenceCounterMap theService1Element = order.get(3);
    assertEquals("theService1", theService1Element.getActivityId());
    assertTrue(theService1Element.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theService1Element.getSequenceCounter();

    ActivitySequenceCounterMap theJoin1Element = order.get(4);
    assertEquals("join", theJoin1Element.getActivityId());
    assertTrue(theJoin1Element.getSequenceCounter() > lastSequenceCounter);

    lastSequenceCounter = theForkElement.getSequenceCounter();

    ActivitySequenceCounterMap theService2Element = order.get(5);
    assertEquals("theService2", theService2Element.getActivityId());
    assertTrue(theService2Element.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theService2Element.getSequenceCounter();

    ActivitySequenceCounterMap theService3Element = order.get(6);
    assertEquals("theService3", theService3Element.getActivityId());
    assertTrue(theService3Element.getSequenceCounter() > lastSequenceCounter);
    lastSequenceCounter = theService3Element.getSequenceCounter();

    ActivitySequenceCounterMap theJoin2Element = order.get(7);
    assertEquals("join", theJoin2Element.getActivityId());
    assertTrue(theJoin2Element.getSequenceCounter() > lastSequenceCounter);

    assertFalse(theJoin1Element.getSequenceCounter() == theJoin2Element.getSequenceCounter());

    ActivitySequenceCounterMap theService4Element = order.get(8);
    assertEquals("theService4", theService4Element.getActivityId());
    assertTrue(theService4Element.getSequenceCounter() > theJoin1Element.getSequenceCounter());
    assertTrue(theService4Element.getSequenceCounter() > theJoin2Element.getSequenceCounter());
    lastSequenceCounter = theService4Element.getSequenceCounter();

    ActivitySequenceCounterMap theEndElement = order.get(9);
    assertEquals("theEnd", theEndElement.getActivityId());
    assertTrue(theEndElement.getSequenceCounter() > lastSequenceCounter);
  }

  @Deployment
  @Test
  public void testForkAndJoinThreeBranchesDifferentSequenceLength() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(4, order.size());

    ActivitySequenceCounterMap theJoin1Element = order.get(0);
    assertEquals("join", theJoin1Element.getActivityId());

    ActivitySequenceCounterMap theJoin2Element = order.get(1);
    assertEquals("join", theJoin2Element.getActivityId());

    ActivitySequenceCounterMap theJoin3Element = order.get(2);
    assertEquals("join", theJoin3Element.getActivityId());

    assertFalse(theJoin1Element.getSequenceCounter() == theJoin2Element.getSequenceCounter());
    assertFalse(theJoin2Element.getSequenceCounter() == theJoin3Element.getSequenceCounter());
    assertFalse(theJoin3Element.getSequenceCounter() == theJoin1Element.getSequenceCounter());

    ActivitySequenceCounterMap theService7Element = order.get(3);
    assertEquals("theService7", theService7Element.getActivityId());
    assertTrue(theService7Element.getSequenceCounter() > theJoin1Element.getSequenceCounter());
    assertTrue(theService7Element.getSequenceCounter() > theJoin2Element.getSequenceCounter());
    assertTrue(theService7Element.getSequenceCounter() > theJoin3Element.getSequenceCounter());
  }

  @Deployment
  @Test
  public void testSequenceInsideSubProcess() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "subProcess", "innerStart", "innerService", "innerEnd", "theService2", "theEnd");
  }

  @Deployment
  @Test
  public void testForkSameSequenceLengthInsideSubProcess() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(3, order.size());

    ActivitySequenceCounterMap innerEnd1Element = order.get(0);
    assertEquals("innerEnd1", innerEnd1Element.getActivityId());

    ActivitySequenceCounterMap innerEnd2Element = order.get(1);
    assertEquals("innerEnd2", innerEnd2Element.getActivityId());

    ActivitySequenceCounterMap theService1Element = order.get(2);
    assertEquals("theService1", theService1Element.getActivityId());

    assertTrue(theService1Element.getSequenceCounter() > innerEnd1Element.getSequenceCounter());
    assertTrue(theService1Element.getSequenceCounter() > innerEnd2Element.getSequenceCounter());
  }

  @Deployment
  @Test
  public void testForkDifferentSequenceLengthInsideSubProcess() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(3, order.size());

    ActivitySequenceCounterMap innerEnd1Element = order.get(0);
    assertEquals("innerEnd1", innerEnd1Element.getActivityId());

    ActivitySequenceCounterMap innerEnd2Element = order.get(1);
    assertEquals("innerEnd2", innerEnd2Element.getActivityId());

    ActivitySequenceCounterMap theService1Element = order.get(2);
    assertEquals("theService1", theService1Element.getActivityId());

    assertTrue(theService1Element.getSequenceCounter() > innerEnd1Element.getSequenceCounter());
    assertTrue(theService1Element.getSequenceCounter() > innerEnd2Element.getSequenceCounter());
  }

  @Deployment
  @Test
  public void testSequentialMultiInstance() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "theService2", "theService2", "theService3", "theEnd");
  }

  @Deployment
  @Test
  public void testParallelMultiInstance() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(6, order.size());

    ActivitySequenceCounterMap theStartElement = order.get(0);
    assertEquals("theStart", theStartElement.getActivityId());

    ActivitySequenceCounterMap theService1Element = order.get(1);
    assertEquals("theService1", theService1Element.getActivityId());
    assertTrue(theService1Element.getSequenceCounter() > theStartElement.getSequenceCounter());

    ActivitySequenceCounterMap theService21Element = order.get(2);
    assertEquals("theService2", theService21Element.getActivityId());
    assertTrue(theService21Element.getSequenceCounter() > theService1Element.getSequenceCounter());

    ActivitySequenceCounterMap theService22Element = order.get(3);
    assertEquals("theService2", theService22Element.getActivityId());
    assertTrue(theService22Element.getSequenceCounter() > theService1Element.getSequenceCounter());

    ActivitySequenceCounterMap theService3Element = order.get(4);
    assertEquals("theService3", theService3Element.getActivityId());
    assertTrue(theService3Element.getSequenceCounter() > theService21Element.getSequenceCounter());
    assertTrue(theService3Element.getSequenceCounter() > theService22Element.getSequenceCounter());

    ActivitySequenceCounterMap theEndElement = order.get(5);
    assertEquals("theEnd", theEndElement.getActivityId());
    assertTrue(theEndElement.getSequenceCounter() > theService3Element.getSequenceCounter());
  }

  @Deployment
  @Test
  public void testLoop() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when

    // then
    testRule.assertProcessEnded(processInstanceId);

    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "join", "theScript", "fork", "join", "theScript", "fork", "theService2", "theEnd");
  }

  @Deployment
  @Test
  public void testInterruptingBoundaryEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "theTask");

    // when (2)
    runtimeService.correlateMessage("newMessage");

    // then (2)
    testRule.assertProcessEnded(processInstanceId);

    order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "theTask", "messageBoundary", "theServiceAfterMessage", "theEnd2");
  }

  @Deployment
  @Test
  public void testNonInterruptingBoundaryEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)

    // then (1)
    List<ActivitySequenceCounterMap> order = ExecutionOrderListener.getActivityExecutionOrder();
    verifyOrder(order, "theStart", "theService1", "theTask");

    // when (2)
    runtimeService.correlateMessage("newMessage");

    // then (2)
    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(6, order.size());

    ActivitySequenceCounterMap theService1Element = order.get(1);
    assertEquals("theService1", theService1Element.getActivityId());

    ActivitySequenceCounterMap theTaskElement = order.get(2);
    assertEquals("theTask", theTaskElement.getActivityId());

    ActivitySequenceCounterMap messageBoundaryElement = order.get(3);
    assertEquals("messageBoundary", messageBoundaryElement.getActivityId());
    assertTrue(messageBoundaryElement.getSequenceCounter() > theService1Element.getSequenceCounter());
    assertFalse(messageBoundaryElement.getSequenceCounter() > theTaskElement.getSequenceCounter());

    ActivitySequenceCounterMap theServiceAfterMessageElement = order.get(4);
    assertEquals("theServiceAfterMessage", theServiceAfterMessageElement.getActivityId());
    assertTrue(theServiceAfterMessageElement.getSequenceCounter() > messageBoundaryElement.getSequenceCounter());

    ActivitySequenceCounterMap theEnd2Element = order.get(5);
    assertEquals("theEnd2", theEnd2Element.getActivityId());
    assertTrue(theEnd2Element.getSequenceCounter() > theServiceAfterMessageElement.getSequenceCounter());

    // when (3)
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    // then (3)
    testRule.assertProcessEnded(processInstanceId);

    order = ExecutionOrderListener.getActivityExecutionOrder();
    assertEquals(7, order.size());

    ActivitySequenceCounterMap theEnd1Element = order.get(6);
    assertEquals("theEnd1", theEnd1Element.getActivityId());
    assertTrue(theEnd1Element.getSequenceCounter() > theEnd2Element.getSequenceCounter());
  }

  protected void verifyOrder(List<ActivitySequenceCounterMap> actualOrder, String... expectedOrder) {
    assertEquals(expectedOrder.length, actualOrder.size());

    long lastActualSequenceCounter = 0;
    for (int i = 0; i < expectedOrder.length; i++) {
      ActivitySequenceCounterMap actual = actualOrder.get(i);

      String actualActivityId = actual.getActivityId();
      String expectedActivityId = expectedOrder[i];
      assertEquals(actualActivityId, expectedActivityId);

      long actualSequenceCounter = actual.getSequenceCounter();
      assertTrue(actualSequenceCounter > lastActualSequenceCounter);

      lastActualSequenceCounter = actualSequenceCounter;
    }
  }

}
