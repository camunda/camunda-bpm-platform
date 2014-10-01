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
package org.camunda.bpm.engine.test.cmmn.sentry;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class SentryExitCriteriaTest extends SentryTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitTask.cmmn"})
  public void testExitTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    // (1) when
    manualStart(firstHumanTaskId);

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isEnabled());

    assertNull(caseService.getVariable(caseInstanceId, "exit"));

    // (2) when
    complete(firstHumanTaskId);

    // (2) then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitStage.cmmn"})
  public void testExitStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();
    assertTrue(stage.isEnabled());

    manualStart(stageId);

    stage = queryCaseExecutionById(stageId);
    assertTrue(stage.isActive());

    CaseExecution milestone = queryCaseExecutionByActivityId("PI_Milestone_1");
    String milestoneId = milestone.getId();
    assertTrue(milestone.isAvailable());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    // (1) when
    manualStart(firstHumanTaskId);

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
  public void testAndJoin() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isEnabled());

    // (1) when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // (1) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isEnabled());

    assertNull(caseService.getVariable(caseInstanceId, "exit"));

    // (2) when
    manualStart(secondHumanTaskId);
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testAndFork.cmmn"})
  public void testAndFork() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testOrJoin.cmmn"})
  public void testOrJoin() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isEnabled());

    // (1) when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // (1) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);

    Object exitVariable = caseService.getVariable(caseInstanceId, "exit");
    assertNotNull(exitVariable);
    assertTrue((Boolean) exitVariable);

    // (2) when
    manualStart(secondHumanTaskId);
    complete(secondHumanTaskId);

    // (2) then
    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testOrFork.cmmn"})
  public void testOrFork() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 80)
      .complete();

    // then
    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testCycle.cmmn"})
  public void testCycle() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();
    assertTrue(thirdHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertNull(thirdHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testCycleWithStage.cmmn"})
  public void testCycleWithStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    String stageId = stage.getId();

    manualStart(stageId);

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution innerFirstHumanTask = queryCaseExecutionByActivityId("PI_Inner_HumanTask_1");
    String innerFirstHumanTaskId = innerFirstHumanTask.getId();
    assertTrue(innerFirstHumanTask.isEnabled());

    CaseExecution innerSecondHumanTask = queryCaseExecutionByActivityId("PI_Inner_HumanTask_2");
    String innerSecondHumanTaskId = innerSecondHumanTask.getId();
    assertTrue(innerSecondHumanTask.isEnabled());

    CaseExecution innerThirdHumanTask = queryCaseExecutionByActivityId("PI_Inner_HumanTask_3");
    String innerThirdHumanTaskId = innerThirdHumanTask.getId();
    assertTrue(innerThirdHumanTask.isEnabled());

    // when
    suspend(stageId);

    // then
    stage = queryCaseExecutionById(stageId);
    assertNull(stage);

    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    innerFirstHumanTask = queryCaseExecutionById(innerFirstHumanTaskId);
    assertNull(innerFirstHumanTask);

    innerSecondHumanTask = queryCaseExecutionById(innerSecondHumanTaskId);
    assertNull(innerSecondHumanTask);

    innerThirdHumanTask = queryCaseExecutionById(innerThirdHumanTaskId);
    assertNull(innerThirdHumanTask);

    Object variable = caseService.getVariable(caseInstanceId, "parentSuspend");
    assertNotNull(variable);
    assertTrue(Boolean.valueOf((Boolean) variable));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitTaskWithIfPart.cmmn"})
  public void testExitTaskWithIfPartSatisfied() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);
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
  public void testExitTaskWithIfPartNotSatisfied() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();
    assertTrue(secondHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);
    caseService
      .withCaseExecution(firstHumanTaskId)
      .setVariable("value", 99)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitTaskByInstanceCreation.cmmn"})
  public void testExitTaskByInstanceCreation() {
    // given + when
    createCaseInstance();

    // then
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    assertNull(secondHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitCriteriaOnCasePlanModel.cmmn"})
  public void testExitCriteriaOnCasePlanModel() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then
    CaseExecution caseInstance = queryCaseExecutionById(caseInstanceId);
    assertTrue(((CaseExecutionEntity) caseInstance).isTerminated());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitOnParentSuspendInsideStage.cmmn"})
  public void testExitOnParentSuspendInsideStage() {
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
  public void testExitOnParentResumeInsideStage() {
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

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitDisabledTask.cmmn"})
  public void testExitDisabledTask() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isEnabled());

    disable(secondHumanTaskId);

    // when
    manualStart(firstHumanTaskId);

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isActive());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitDisabledTask.cmmn"})
  public void testExitSuspendedTask() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isEnabled());

    manualStart(secondHumanTaskId);
    suspend(secondHumanTaskId);

    // when
    manualStart(firstHumanTaskId);

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isActive());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitAvailableTask.cmmn"})
  public void testExitAvailableTask() {
    // given
    createCaseInstance();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_1");
    String firstHumanTaskId = firstHumanTask.getId();

    assertTrue(firstHumanTask.isEnabled());

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    String secondHumanTaskId = secondHumanTask.getId();

    assertTrue(secondHumanTask.isAvailable());

    CaseExecution thirdHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    String thirdHumanTaskId = thirdHumanTask.getId();

    assertTrue(thirdHumanTask.isEnabled());

    // when
    manualStart(firstHumanTaskId);

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertTrue(firstHumanTask.isActive());

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);

    thirdHumanTask = queryCaseExecutionById(thirdHumanTaskId);
    assertTrue(thirdHumanTask.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryExitCriteriaTest.testExitDisabledTask.cmmn"})
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
