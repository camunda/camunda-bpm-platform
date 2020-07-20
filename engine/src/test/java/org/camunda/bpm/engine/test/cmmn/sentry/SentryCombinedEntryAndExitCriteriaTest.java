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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.junit.Ignore;

/**
 * @author Roman Smirnov
 *
 */
@Ignore
public class SentryCombinedEntryAndExitCriteriaTest extends CmmnTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryCombinedEntryAndExitCriteriaTest.testParentResumeInsideStage.cmmn"})
  public void FAILING_testParentResumeInsideStage() {
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

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();

    assertTrue(thirdHumanTask.isAvailable());

    // (1) when
    suspend(stageId);

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(((CaseExecutionEntity)secondHumanTask).isSuspended());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(((CaseExecutionEntity)thirdHumanTask).isSuspended());

    // (2) when
    resume(stageId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isEnabled());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryCombinedEntryAndExitCriteriaTest.testParentSuspendInsideStage.cmmn"})
  public void FAILING_testParentSuspendInsideStage() {
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

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();

    assertTrue(thirdHumanTask.isAvailable());

    // when
    suspend(stageId);

    // then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(((CaseExecutionEntity)thirdHumanTask).isSuspended());
    assertEquals(CaseExecutionState.ENABLED, ((CaseExecutionEntity) thirdHumanTask).getPreviousState());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryCombinedEntryAndExitCriteriaTest.testParentResumeInsideStageDifferentPlanItemOrder.cmmn"})
  public void FAILING_testParentResumeInsideStageDifferentPlanItemOrder() {
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

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();

    assertTrue(thirdHumanTask.isAvailable());

    // (1) when
    suspend(stageId);

    // (1) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(((CaseExecutionEntity)secondHumanTask).isSuspended());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(((CaseExecutionEntity)thirdHumanTask).isSuspended());

    // (2) when
    resume(stageId);

    // (2) then
    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isEnabled());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryCombinedEntryAndExitCriteriaTest.testParentSuspendInsideStageDifferentPlanItemOrder.cmmn"})
  public void FAILING_testParentSuspendInsideStageDifferentPlanItemOrder() {
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

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();

    assertTrue(thirdHumanTask.isAvailable());

    // when
    suspend(stageId);

    // then
    stage = queryCaseExecutionById(stageId);
    assertTrue(((CaseExecutionEntity)stage).isSuspended());

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(((CaseExecutionEntity)firstHumanTask).isSuspended());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(((CaseExecutionEntity)thirdHumanTask).isSuspended());
    assertEquals(CaseExecutionState.ENABLED, ((CaseExecutionEntity) thirdHumanTask).getPreviousState());

  }
}
