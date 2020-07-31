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
package org.camunda.bpm.engine.test.cmmn.sentry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnSentryPart;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class SentryEntryCriteriaTest extends CmmnTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceEnableTask.cmmn"})
  @Test
  public void testSequenceEnableTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    // (1) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "start"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isEnabled());

    Object enableVariable = caseService.getVariable(caseInstanceId, "enable");
    assertNotNull(enableVariable);
    assertTrue((Boolean) enableVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceAutoStartTask.cmmn"})
  @Test
  public void testSequenceAutoStartTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    // (1) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "enable"));
    assertNull(caseService.getVariable(caseInstanceId, "start"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

    assertNull(caseService.getVariable(caseInstanceId, "enable"));
    Object startVariable = caseService.getVariable(caseInstanceId, "start");
    assertNotNull(startVariable);
    assertTrue((Boolean) startVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceEnableStage.cmmn"})
  @Test
  public void testSequenceEnableStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();
    assertTrue(stage.isAvailable());

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "enable"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isEnabled());

    Object enableVariable = caseService.getVariable(caseInstanceId, "enable");
    assertNotNull(enableVariable);
    assertTrue((Boolean) enableVariable);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceAutoStartStage.cmmn"})
  @Test
  public void testSequenceAutoStartStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();
    assertTrue(stage.isAvailable());

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "enable"));
    assertNull(caseService.getVariable(caseInstanceId, "start"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    assertNull(caseService.getVariable(caseInstanceId, "enable"));
    Object startVariable = caseService.getVariable(caseInstanceId, "start");
    assertNotNull(startVariable);
    assertTrue((Boolean) startVariable);

    CaseExecutionQuery query = caseService
      .createCaseExecutionQuery()
      .enabled();

    assertEquals(2, query.count());

    for (CaseExecution child : query.list()) {
      assertEquals(stageId, child.getParentId());
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceOccurMilestone.cmmn"})
  @Test
  public void testSequenceOccurMilestone() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution milestone = queryCaseExecutionByActivityId("PI_Milestone_1");
    String milestoneId = milestone.getId();
    assertTrue(milestone.isAvailable());

    // (1) then
    milestone = queryCaseExecutionById(milestoneId);
    assertTrue(milestone.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "occur"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    milestone = queryCaseExecutionById(milestoneId);
    assertNull(milestone);

    Object occurVariable = caseService.getVariable(caseInstanceId, "occur");
    assertNotNull(occurVariable);
    assertTrue((Boolean) occurVariable);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequence.cmmn"})
  @Test
  public void testSequence() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    // (1) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isAvailable());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "start"));

    // (2) when (complete first human task) /////////////////////////////////////////////
    complete(firstHumanTaskId);

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isAvailable());

    Object enableVariable = caseService.getVariable(caseInstanceId, "start");
    assertNotNull(enableVariable);
    assertTrue((Boolean) enableVariable);

    // reset variable
    caseService
      .withCaseExecution(caseInstanceId)
      .removeVariable("start")
      .execute();

    // (3) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "start"));

    // (4) when (complete second human task) //////////////////////////////////////////
    complete(secondHumanTaskId);

    // (4) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

    enableVariable = caseService.getVariable(caseInstanceId, "start");
    assertNotNull(enableVariable);
    assertTrue((Boolean) enableVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceWithIfPart.cmmn"})
  @Test
  public void testSequenceWithIfPartNotSatisfied() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    // when
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 99)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isAvailable());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceWithIfPart.cmmn"})
  @Test
  public void testSequenceWithIfPartSatisfied() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    // when
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 100)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testAndFork.cmmn"})
  @Test
  public void testAndFork() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    CaseSentryPartQueryImpl query = createCaseSentryPartQuery();
    CmmnSentryPart part = query.singleResult();
    assertFalse(part.isSatisfied());

    // when
    complete(firstHumanTaskId);

    // then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

    part = query.singleResult();
    assertNotNull(part);
    assertFalse(part.isSatisfied());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testAndJoin.cmmn"})
  @Test
  public void testAndJoin() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    // (1) when
    complete(firstHumanTaskId);

    // (1) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isAvailable());

    assertNull(caseService.getVariable(caseInstanceId, "start"));

    // (2) when
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

    Object startVariable = caseService.getVariable(caseInstanceId, "start");
    assertNotNull(startVariable);
    assertTrue((Boolean) startVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testSequenceCombinedWithAndJoin.cmmn"})
  @Test
  public void testSequenceCombinedWithAndJoin() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    // (1) when
    complete(firstHumanTaskId);

    // (1) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isAvailable()); // still available

    // (2) when
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testOrFork.cmmn"})
  @Test
  public void testOrFork() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    // when
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 80)
      .complete();

    // then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isAvailable());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testOrJoin.cmmn"})
  @Test
  public void testOrJoin() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    // (1) when
    complete(firstHumanTaskId);

    // (1) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

    Object startVariable = caseService.getVariable(caseInstanceId, "start");
    assertNotNull(startVariable);
    assertTrue((Boolean) startVariable);

    // (2) when
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive()); // is still active
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testCycle.cmmn"})
  @Test
  public void testCycle() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isAvailable());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isAvailable());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isAvailable());

    try {
      // (1) when
      manualStart(firstHumanTaskId);
      fail("It should not be possible to start the first human task manually.");
    } catch (NotAllowedException e) {
    }

    try {
      // (2) when
      manualStart(secondHumanTaskId);
      fail("It should not be possible to start the second human task manually.");
    } catch (NotAllowedException e) {
    }

    try {
      // (3) when
      manualStart(thirdHumanTaskId);
      fail("It should not be possible to third the second human task manually.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testEnableByInstanceCreation.cmmn"})
  @Test
  public void testEnableByInstanceCreation() {
    // given + when
    createCaseInstance();

    // then
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    assertTrue(secondHumanTask.isActive());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testEnableOnParentSuspendInsideStage.cmmn"})
  public void FAILING_testEnableOnParentSuspendInsideStage() {
    // given
    createCaseInstance();

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();

    manualStart(stageId);

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isAvailable());

    // (1) when
    suspend(stageId);

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(((CaseExecutionEntity)secondHumanTask).isSuspended());
    assertEquals(CaseExecutionState.ENABLED, ((CaseExecutionEntity)secondHumanTask).getPreviousState());

    // (2) when
    resume(stageId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isEnabled());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testEnableOnParentResumeInsideStage.cmmn"})
  public void FAILING_testEnableOnParentResumeInsideStage() {
    // given
    createCaseInstance();

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();

    manualStart(stageId);

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isAvailable());

    // (1) when
    suspend(stageId);

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(((CaseExecutionEntity)secondHumanTask).isSuspended());
    assertEquals(CaseExecutionState.AVAILABLE, ((CaseExecutionEntity)secondHumanTask).getPreviousState());

    // (2) when
    resume(stageId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isEnabled());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isEnabled());
  }

  /**
   * Please note that suspension and/or resuming is currently
   * not supported by the public API. Furthermore the given
   * test is not a very useful use case in that just a milestone
   * will be suspended.
   */
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testResume.cmmn"})
  public void FAILING_testResume() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution milestone = queryCaseExecutionByActivityId("PI_Milestone_1");
    String milestoneId = milestone.getId();

    assertTrue(milestone.isAvailable());

    suspend(milestoneId);

    // (1) when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // (1) then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    milestone = queryCaseExecutionById(milestoneId);
    assertTrue(((CaseExecutionEntity) milestone).isSuspended());

    // (2) when
    resume(milestoneId);

    // (2)
    milestone = queryCaseExecutionById(milestoneId);
    assertNull(milestone);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testFireAlsoNotAffectedSentries.cmmn"})
  @Test
  public void testFireAlsoNotAffectedSentries() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isAvailable());

    CaseExecution milestone = queryCaseExecutionByActivityId("PI_Milestone_1");
    String milestoneId = milestone.getId();

    assertTrue(milestone.isAvailable());

    caseService
      .withCaseExecution(caseInstanceId)
      .setVariable("value", 99)
      .execute();

    // (1) when
    complete(firstHumanTaskId);

    // (1) then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isAvailable());

    // (2) when
    caseService
      .withCaseExecution(caseInstanceId)
      .setVariable("value", 101)
      .execute();

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

    milestone = queryCaseExecutionById(milestoneId);
    // milestone occurs when the sentry was evaluated successfully after value is set to 101
    assertNull(milestone);
  }

  @Deployment
  @Test
  public void testCaseFileItemOnPart() {
    createCaseInstance().getId();

    CaseExecution humanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // sentry has been ignored
    assertTrue(humanTask.isActive());
  }

  @Deployment
  @Test
  public void testReusableStage() {
    // given
    createCaseInstance();

    String firstStageId = queryCaseExecutionByActivityId("PI_Stage_1").getId();
    String secondStageId = queryCaseExecutionByActivityId("PI_Stage_2").getId();

    List<CaseExecution> humanTasks = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .active()
      .list();
    assertEquals(2, humanTasks.size());

    String humanTaskInsideFirstStageId = null;
    if (((CaseExecutionEntity) humanTasks.get(0)).getParentId().equals(firstStageId)) {
      humanTaskInsideFirstStageId = humanTasks.get(0).getId();
    }
    else {
      humanTaskInsideFirstStageId = humanTasks.get(1).getId();
    }

    // when
    complete(humanTaskInsideFirstStageId);

    // then
    CaseExecution secondHumanTaskInsideFirstStage = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_2")
      .active()
      .singleResult();
    assertEquals(firstStageId, ((CaseExecutionEntity) secondHumanTaskInsideFirstStage).getParentId());

    // PI_HumanTask_1 in PI_Stage_2 is enabled
    CaseExecution firstHumanTaskInsideSecondStage = queryCaseExecutionByActivityId("PI_HumanTask_1");
    assertNotNull(firstHumanTaskInsideSecondStage);
    assertTrue(firstHumanTaskInsideSecondStage.isActive());
    assertEquals(secondStageId, ((CaseExecutionEntity) firstHumanTaskInsideSecondStage).getParentId());

    // PI_HumanTask_2 in PI_Stage_2 is available
    CaseExecution secondHumanTaskInsideSecondStage = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2")
        .available()
        .singleResult();
    assertNotNull(secondHumanTaskInsideSecondStage);
    assertTrue(secondHumanTaskInsideSecondStage.isAvailable());
    assertEquals(secondStageId, ((CaseExecutionEntity) secondHumanTaskInsideSecondStage).getParentId());
  }

  /**
   * CAM-3226
   */
  @Deployment
  @Test
  public void testSentryShouldNotBeEvaluatedAfterStageComplete() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    // when
    CaseExecution stageExecution = caseService.createCaseExecutionQuery().activityId("PI_Stage_1").singleResult();
    assertNotNull(stageExecution);

    // .. there is a local stage variable
    caseService.setVariableLocal(stageExecution.getId(), "value", 99);

    // .. and the stage is activated (such that the tasks are instantiated)
    caseService.manuallyStartCaseExecution(stageExecution.getId());

    CaseExecution task1Execution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(task1Execution);

    // then
    // .. completing the stage should be successful; evaluating Sentry_1 should not fail
    caseService.completeCaseExecution(task1Execution.getId());
    stageExecution = caseService.createCaseExecutionQuery().activityId("PI_Stage_1").singleResult();
    assertNull(stageExecution);

    // .. and the case plan model should have completed
    CaseExecution casePlanModelExecution = caseService.createCaseExecutionQuery().caseExecutionId(caseInstanceId).singleResult();
    assertNotNull(casePlanModelExecution);
    assertFalse(casePlanModelExecution.isActive());

    caseService.closeCaseInstance(caseInstanceId);
  }

  @Deployment
  @Test
  public void testIfPartOnCaseInstanceCreate() {

    // when
    createCaseInstanceByKey("case", Variables.putValue("value", 101));

    // then
    CaseExecution caseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");
    assertTrue(caseExecution.isActive());

  }

  @Deployment
  @Test
  public void testIfPartOnCaseInstanceCreateWithSentry() {

    // when
    createCaseInstanceByKey("case", Variables.putValue("myVar", 101));

    // then
    CaseExecution caseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");
    assertTrue(caseExecution.isActive());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.testShouldNotTriggerCompletionTwice.cmmn",
      "org/camunda/bpm/engine/test/cmmn/sentry/SentryEntryCriteriaTest.noop.bpmn20.xml"
  })
  @Test
  public void testShouldNotTriggerCompletionTwice() {
    // when
    CaseInstance ci = caseService.createCaseInstanceByKey("case");

    // then
    assertTrue(ci.isCompleted());
  }

}
