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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class SentryExitCriteriaTest extends CmmnTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitTask.cmmn"})
  @Test
  public void testExitTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isActive());

    assertNull(caseService.getVariable(caseInstanceId, "exit"));

    // (1) when
    complete(firstHumanTaskId);

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitStage.cmmn"})
  @Test
  public void testExitStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();
    assertTrue(stage.isActive());

    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    CaseExecution milestone = queryCaseExecutionByActivityId("PI_Milestone_1");
    String milestoneId = milestone.getId();
    assertTrue(milestone.isAvailable());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    assertNull(caseService.getVariable(caseInstanceId, "exit"));
    assertNull(caseService.getVariable(caseInstanceId, "parentTerminate"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertNull(stage);

    milestone = queryCaseExecutionById(milestoneId);
    assertNull(milestone);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);

    Object parentTerminateVariable = caseService.getVariable(caseInstanceId, "parentTerminate");
    assertNotNull(parentTerminateVariable);
    assertTrue((Boolean) parentTerminateVariable);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testAndJoin.cmmn"})
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
    assertTrue(thirdHumanTask.isActive());

    // (1) when
    complete(firstHumanTaskId);

    // (1) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isActive());

    assertNull(caseService.getVariable(caseInstanceId, "exit"));

    // (2) when
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testAndFork.cmmn"})
  @Test
  public void testAndFork() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isActive());

    // when
    complete(firstHumanTaskId);

    // then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testOrJoin.cmmn"})
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
    assertTrue(thirdHumanTask.isActive());

    // (1) when
    complete(firstHumanTaskId);

    // (1) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);

    // (2) when
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testOrFork.cmmn"})
  @Test
  public void testOrFork() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isActive());

    // when
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 80)
      .complete();

    // then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isActive());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testCycle.cmmn"})
  @Test
  public void testCycle() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isActive());

    // when
    complete(firstHumanTaskId);

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitTaskWithIfPart.cmmn"})
  @Test
  public void testExitTaskWithIfPartSatisfied() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    // when
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 100)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitTaskWithIfPart.cmmn"})
  @Test
  public void testExitTaskWithIfPartNotSatisfied() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isActive());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isActive());

    // when
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 99)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitCriteriaOnCasePlanModel.cmmn"})
  @Test
  public void testExitCriteriaOnCasePlanModel() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isActive());

    // when
    complete(firstHumanTaskId);

    // then
    CaseExecution caseInstance = queryCaseExecutionById(caseInstanceId);
    assertTrue(caseInstance.isTerminated());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitOnParentSuspendInsideStage.cmmn"})
  public void FAILING_testExitOnParentSuspendInsideStage() {
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

    assertTrue(secondHumanTask.isEnabled());

    // when
    suspend(stageId);

    // then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitOnParentResumeInsideStage.cmmn"})
  public void FAILING_testExitOnParentResumeInsideStage() {
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

    assertTrue(secondHumanTask.isEnabled());

    // (1) when
    suspend(stageId);

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(((CaseExecutionEntity)secondHumanTask).isSuspended());

    // (2) when
    resume(stageId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isActive());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isEnabled());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitActiveTask.cmmn"})
  @Test
  public void testExitActiveTask() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isEnabled());

    manualStart(secondHumanTaskId);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

    Task secondTask = taskService
        .createTaskQuery()
        .singleResult();
    assertNotNull(secondTask);

    // when
    manualStart(firstHumanTaskId);

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isActive());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    secondTask = taskService
        .createTaskQuery()
        .taskId(secondTask.getId())
        .singleResult();
    assertNull(secondTask);

  }
}
