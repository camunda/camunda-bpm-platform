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
package org.camunda.bpm.engine.test.cmmn.repetition;

import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class RepetitionRuleTest extends CmmnProcessEngineTestCase {

  private static final String CASE_ID = "case";

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testVariableBasedRule.cmmn")
  public void testVariableBasedRepetitionRuleEvaluatesToTrue() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repeat", true);
    createCaseInstanceByKey("case", variables);

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
    assertEquals(1, query.active().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testVariableBasedRule.cmmn")
  public void testVariableBasedRepetitionRuleEvaluatesToFalse() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repeat", false);
    createCaseInstanceByKey("case", variables);

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(1, query.count());
    assertEquals(1, query.active().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultVariableBasedRepetitionRuleEvaluatesToTrue() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repeat", true);
    createCaseInstanceByKey("case", variables);

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
    assertEquals(1, query.active().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultVariableBasedRepetitionRuleEvaluatesToFalse() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repeat", false);
    createCaseInstanceByKey("case", variables);

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(1, query.count());
    assertEquals(1, query.active().count());
  }

  @Deployment
  public void testRepeatTask() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());

    CaseExecution originInstance = query.active().singleResult();
    assertNotNull(originInstance);

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);
  }

  @Deployment
  public void testRepeatStage() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");

    assertEquals(2, query.count());

    CaseExecution originInstance = query.active().singleResult();
    assertNotNull(originInstance);

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);
  }

  @Deployment
  public void testRepeatMilestone() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    String milestoneId = queryCaseExecutionByActivityId("PI_Milestone_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1");

    assertEquals(1, query.count());
    assertTrue(query.singleResult().isAvailable());
    assertFalse(milestoneId.equals(query.singleResult().getId()));
  }

  @Deployment
  public void testRepeatTaskMultipleTimes() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when (1)
    disable(firstHumanTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());

    CaseExecution originInstance = query.active().singleResult();
    assertNotNull(originInstance);

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);

    // when (2)
    reenable(firstHumanTaskId);
    disable(firstHumanTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(3, query.count());

    // active instances
    assertEquals(2, query.active().count());

    // available instances
    assertEquals(1,  query.available().count());
  }

  @Deployment
  public void testRepeatStageMultipleTimes() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when (1)
    disable(firstHumanTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");

    assertEquals(2, query.count());

    CaseExecution originInstance = query.active().singleResult();
    assertNotNull(originInstance);

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);

    // when (2)
    reenable(firstHumanTaskId);
    disable(firstHumanTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");

    assertEquals(3, query.count());

    // enabled instances
    assertEquals(2, query.active().count());

    // available instances
    assertEquals(1,  query.available().count());
  }

  @Deployment
  public void testRepeatMilestoneMultipleTimes() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    String milestoneId = queryCaseExecutionByActivityId("PI_Milestone_1").getId();

    // when (1)
    disable(firstHumanTaskId);

    // then (2)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1");

    assertEquals(1, query.count());
    assertTrue(query.singleResult().isAvailable());
    assertFalse(milestoneId.equals(query.singleResult().getId()));

    // when (2)
    reenable(firstHumanTaskId);
    disable(firstHumanTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1");

    assertEquals(1, query.count());
    assertTrue(query.singleResult().isAvailable());
    assertFalse(milestoneId.equals(query.singleResult().getId()));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatTaskWithoutEntryCriteria.cmmn")
  public void testRepeatTaskWithoutEntryCriteriaWhenCompleting() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_ID,Variables.createVariables().putValue("repeating", true)).getId();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    CaseExecution activeCaseExecution = query.active().singleResult();
    assertNotNull(activeCaseExecution);

    // when (1)
    complete(activeCaseExecution.getId());

    // then (1)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    activeCaseExecution = query.active().singleResult();
    assertNotNull(activeCaseExecution);

    // when (2)
    caseService.setVariable(caseInstanceId,"repeating",false);
    complete(activeCaseExecution.getId());

    // then (2)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(0, query.count());

    // then (3)
    query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatStageWithoutEntryCriteria.cmmn")
  public void testRepeatStageWithoutEntryCriteriaWhenCompleting() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_ID,Variables.createVariables().putValue("repeating",true)).getId();

    CaseExecutionQuery stageQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");
    assertEquals(1, stageQuery.count());

    CaseExecution activeStageCaseExecution = stageQuery.active().singleResult();
    assertNotNull(activeStageCaseExecution);

    CaseExecution humanTaskCaseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // when (1)
    complete(humanTaskCaseExecution.getId());

    // then (1)
    stageQuery = caseService.createCaseExecutionQuery().activityId("PI_Stage_1");
    assertEquals(1, stageQuery.count());

    activeStageCaseExecution = stageQuery.active().singleResult();
    assertNotNull(activeStageCaseExecution);

    humanTaskCaseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // when (2)
    caseService.setVariable(caseInstanceId,"repeating",false);
    complete(humanTaskCaseExecution.getId());

    // then (3)
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatTaskWithoutEntryCriteria.cmmn")
  public void testRepeatTaskWithoutEntryCriteriaWhenTerminating() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_ID,Variables.createVariables().putValue("repeating",true)).getId();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    CaseExecution activeCaseExecution = query.active().singleResult();
    assertNotNull(activeCaseExecution);

    // when (1)
    terminate(activeCaseExecution.getId());

    // then (1)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    activeCaseExecution = query.active().singleResult();
    assertNotNull(activeCaseExecution);

    // when (2)
    caseService.setVariable(caseInstanceId,"repeating",false);
    terminate(activeCaseExecution.getId());

    // then (2)
    query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatStageWithoutEntryCriteria.cmmn")
  public void testRepeatStageWithoutEntryCriteriaWhenTerminating() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_ID,Variables.createVariables().putValue("repeating",true)).getId();

    CaseExecutionQuery stageQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");
    assertEquals(1, stageQuery.count());

    CaseExecution activeStageCaseExecution = stageQuery.active().singleResult();
    assertNotNull(activeStageCaseExecution);

    CaseExecution humanTaskCaseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // when (1)
    terminate(humanTaskCaseExecution.getId());

    // then (1)
    stageQuery = caseService.createCaseExecutionQuery().activityId("PI_Stage_1");
    assertEquals(1, stageQuery.count());

    activeStageCaseExecution = stageQuery.active().singleResult();
    assertNotNull(activeStageCaseExecution);

    humanTaskCaseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // when (2)
    caseService.setVariable(caseInstanceId,"repeating",false);
    terminate(humanTaskCaseExecution.getId());

    // then (2)
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment
  public void testRepeatTaskWithoutEntryCriteriaOnCustomStandardEvent() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    CaseExecution enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);

    // when (1)
    disable(enabledCaseExecution.getId());

    // then (1)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);

    // when (2)
    disable(enabledCaseExecution.getId());

    // then (2)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(3, query.count());

    enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);

    // when (3)
    complete(caseInstanceId);

    // then (3)
    query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment
  public void testRepeatStageWithoutEntryCriteriaOnCustomStandardEvent() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecutionQuery stageQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");
    assertEquals(1, stageQuery.count());

    CaseExecution enabledStageCaseExecution = stageQuery.enabled().singleResult();
    assertNotNull(enabledStageCaseExecution);

    // when (1)
    disable(enabledStageCaseExecution.getId());

    // then (1)
    stageQuery = caseService.createCaseExecutionQuery().activityId("PI_Stage_1");
    assertEquals(2, stageQuery.count());

    enabledStageCaseExecution = stageQuery.enabled().singleResult();
    assertNotNull(enabledStageCaseExecution);

    // when (2)
    disable(enabledStageCaseExecution.getId());

    // then (2)
    stageQuery = caseService.createCaseExecutionQuery().activityId("PI_Stage_1");
    assertEquals(3, stageQuery.count());

    enabledStageCaseExecution = stageQuery.enabled().singleResult();
    assertNotNull(enabledStageCaseExecution);

    // when (3)
    complete(caseInstanceId);

    // then (3)
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment
  public void testNonRepeatableTaskDependsOnRepeatableTask() {
    // given
    createCaseInstance();

    CaseExecutionQuery availableQuery = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .available();

    // fire three times entry criteria of repeatable task
    // -> three enabled tasks
    // -> one available task
    fireEntryCriteria(availableQuery.singleResult().getId());
    fireEntryCriteria(availableQuery.singleResult().getId());
    fireEntryCriteria(availableQuery.singleResult().getId());

    // get any enabled task
    CaseExecutionQuery enabledQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .active();

    String enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isAvailable());

    // when
    complete(enabledTaskId);

    // then
    // there is only one instance of PI_HumanTask_2
    secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isActive());
  }

  @Deployment
  public void testRepeatableTaskDependsOnAnotherRepeatableTask() {
    // given
    createCaseInstance();

    CaseExecutionQuery availableQuery = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .available();

    // fire three times entry criteria of repeatable task
    // -> three enabled tasks
    // -> one available task
    fireEntryCriteria(availableQuery.singleResult().getId());
    fireEntryCriteria(availableQuery.singleResult().getId());
    fireEntryCriteria(availableQuery.singleResult().getId());

    // get any enabled task
    CaseExecutionQuery activeQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .active();

    String activeTaskId = activeQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    // when (1)
    complete(activeTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
    assertEquals(1, query.active().count());
    assertEquals(1, query.available().count());

    // when (2)
    // get another enabled task
    activeTaskId = activeQuery
        .listPage(0, 1)
        .get(0)
        .getId();
    complete(activeTaskId);

    // then (2)
    query = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_2");
    assertEquals(3, query.count());
    assertEquals(2, query.active().count());
    assertEquals(1, query.available().count());
  }

  @Deployment
  public void testLimitedRepetitions() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repetition", 0);
    createCaseInstanceByKey("case", variables);

    CaseExecutionQuery availableQuery = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .available();

    // fire three times entry criteria of repeatable task
    // -> three enabled tasks
    // -> one available task
    fireEntryCriteria(availableQuery.singleResult().getId());
    fireEntryCriteria(availableQuery.singleResult().getId());
    fireEntryCriteria(availableQuery.singleResult().getId());

    // get any enabled task
    CaseExecutionQuery activeQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .active();

    String activeTaskId = activeQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    // when (1)
    complete(activeTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
    assertEquals(1, query.active().count());
    assertEquals(1, query.available().count());

    // when (2)
    activeTaskId = activeQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    complete(activeTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(3, query.count());
    assertEquals(2, query.active().count());
    assertEquals(1, query.available().count());

    // when (3)
    activeTaskId = activeQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    complete(activeTaskId);

    // then (3)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(3, query.count());
    assertEquals(3, query.active().count());
    assertEquals(0, query.available().count());
  }

  @Deployment
  public void testLimitedSequentialRepetitions() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repetition", 0);
    createCaseInstanceByKey("case", variables);

    CaseExecutionQuery activeQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .active();
    String enabledCaseExecutionId = activeQuery.singleResult().getId();

    // when (1)
    complete(enabledCaseExecutionId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());
    assertEquals(1, query.active().count());

    // when (2)
    enabledCaseExecutionId = activeQuery.singleResult().getId();
    complete(enabledCaseExecutionId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());
    assertEquals(1, query.active().count());

    // when (3)
    enabledCaseExecutionId = activeQuery.singleResult().getId();
    complete(enabledCaseExecutionId);

    // then (3)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(0, query.count());
  }

  @Deployment
  public void testLimitedParallelRepetitions() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repetition", 0);
    createCaseInstanceByKey("case", variables);

    // when (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");

    // then (1)
    assertEquals(3, query.count());

    // when (2)
    // complete any task
    String caseExecutionId = query.listPage(0, 1).get(0).getId();
    complete(caseExecutionId);

    // then (2)
    assertEquals(2, query.count());
  }

  @Deployment
  public void testAutoCompleteStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    // then
    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertNull(stage);

    CaseInstance caseInstance = (CaseInstance) queryCaseExecutionById(caseInstanceId);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment
  public void testAutoCompleteStageWithoutEntryCriteria() {
    // given
    VariableMap variables = Variables.createVariables().putValue("manualActivation", false);
    String caseInstanceId = createCaseInstanceByKey("case", variables).getId();

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    assertEquals(1, query.active().count());
    String activeTaskId = query.singleResult().getId();

    // when (2)
    // completing active task
    complete(activeTaskId);

    // then (2)
    // the stage should be completed
    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertNull(stage);

    CaseInstance caseInstance = (CaseInstance) queryCaseExecutionById(caseInstanceId);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment
  public void testAutoCompleteStageAutoActivationRepeatableTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    String stageId = queryCaseExecutionByActivityId("PI_Stage_1").getId();

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    assertEquals(1, query.active().count());
    String activeTaskId = query.singleResult().getId();

    // when (2)
    // completing active task
    complete(activeTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    assertEquals(1, query.active().count());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertNotNull(stage);
    assertTrue(stage.isActive());

    CaseInstance caseInstance = (CaseInstance) queryCaseExecutionById(caseInstanceId);
    assertTrue(caseInstance.isActive());
  }

  @Deployment
  public void testAutoCompleteStageRequiredRepeatableTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    assertEquals(1, query.active().count());
    String activeTaskId = query.singleResult().getId();

    // when (2)
    complete(activeTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());
    assertEquals(1, query.active().count());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertNotNull(stage);
    assertTrue(stage.isActive());

    CaseInstance caseInstance = (CaseInstance) queryCaseExecutionById(caseInstanceId);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatTask.cmmn")
  public void testShouldNotRepeatTaskAfterCompletion() {
    // given
    createCaseInstance();
    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when (1)
    complete(humanTask1);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
    assertEquals(1, query.active().count());

    // when (2)
    String humanTask2 = query.active().singleResult().getId();
    complete(humanTask2);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(1, query.count());
    assertEquals(1, query.available().count());
  }

  @Deployment
  public void testIgnoreRepeatOnStandardEvent() {
    // given
    createCaseInstance();

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    complete(humanTask1);

    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());

    // when
    String humanTask2 = query.enabled().singleResult().getId();
    disable(humanTask2);

    // then
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
  }

  @Deployment
  public void testDefaultValueWithoutCondition() {
    createCaseInstanceByKey("case");
    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
    assertEquals(1, query.active().count());
  }

  @Deployment
  public void testDefaultValueWithEmptyCondition() {
    createCaseInstanceByKey("case");
    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
    assertEquals(1, query.active().count());
  }

  // helper ////////////////////////////////////////////////////////

  protected void fireEntryCriteria(final String caseExecutionId) {
    executeHelperCaseCommand(new HelperCaseCommand() {
      public void execute() {
        getExecution(caseExecutionId).fireEntryCriteria();
      }
    });
  }

}
