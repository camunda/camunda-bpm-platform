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

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testVariableBasedRule.cmmn")
  public void testRepetitionRuleEvaluatesToTrue() {
    VariableMap variables = Variables.createVariables().putValue("repeat", true);
    createCaseInstanceByKey("case", variables);

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_2");

    assertNotNull(taskExecution);
    assertTrue(taskExecution.isRepeatable());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testVariableBasedRule.cmmn")
  public void testRepetitionRuleEvaluatesToFalse() {
    VariableMap variables = Variables.createVariables().putValue("repeat", false);
    createCaseInstanceByKey("case", variables);

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_2");

    assertNotNull(taskExecution);
    assertFalse(taskExecution.isRepeatable());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultRepetitionRuleEvaluatesToTrue() {
    VariableMap variables = Variables.createVariables().putValue("repeat", true);
    createCaseInstanceByKey("case", variables);

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_2");

    assertNotNull(taskExecution);
    assertTrue(taskExecution.isRepeatable());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultRepetitionRuleEvaluatesToFalse() {
    VariableMap variables = Variables.createVariables().putValue("repeat", false);
    createCaseInstanceByKey("case", variables);

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_2");

    assertNotNull(taskExecution);
    assertFalse(taskExecution.isRepeatable());
  }

  @Deployment
  public void testRepeatTask() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(2, query.count());

    CaseExecution originInstance = query.enabled().singleResult();
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
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");

    assertEquals(2, query.count());

    CaseExecution originInstance = query.enabled().singleResult();
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
    manualStart(firstHumanTaskId);
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

    CaseExecution originInstance = query.enabled().singleResult();
    assertNotNull(originInstance);

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);

    reenable(firstHumanTaskId);

    // when (2)
    disable(firstHumanTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    assertEquals(3, query.count());

    // enabled instances
    assertEquals(2, query.enabled().count());

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

    CaseExecution originInstance = query.enabled().singleResult();
    assertNotNull(originInstance);

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);

    reenable(firstHumanTaskId);

    // when (2)
    disable(firstHumanTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");

    assertEquals(3, query.count());

    // enabled instances
    assertEquals(2, query.enabled().count());

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

    reenable(firstHumanTaskId);

    // when (2)
    disable(firstHumanTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1");

    assertEquals(1, query.count());
    assertTrue(query.singleResult().isAvailable());
    assertFalse(milestoneId.equals(query.singleResult().getId()));
  }

  @Deployment
  public void testRepetitionCriterionTask() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    CaseExecution enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);
    CaseExecution availableCaseExecution = query.available().singleResult();
    assertNotNull(availableCaseExecution);

    // when (1)
    manualStart(enabledCaseExecution.getId());
    complete(enabledCaseExecution.getId());

    // then (1)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);
    assertEquals(availableCaseExecution.getId(), enabledCaseExecution.getId());

    availableCaseExecution = query.available().singleResult();
    assertNotNull(availableCaseExecution);

    // when (2)
    manualStart(enabledCaseExecution.getId());
    complete(enabledCaseExecution.getId());

    // then (2)
    query = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);
    assertEquals(availableCaseExecution.getId(), enabledCaseExecution.getId());

    availableCaseExecution = query.available().singleResult();
    assertNotNull(availableCaseExecution);

    // when (3)
    complete(caseInstanceId);

    // then (3)
    query = caseService.createCaseExecutionQuery();
    assertEquals(1, query.count());
    assertEquals(caseInstanceId, query.singleResult().getId());
  }

  @Deployment
  public void testRepetitionCriterionStage() {
    // given
    String caseInstanceId = createCaseInstance().getId();

    CaseExecutionQuery stageQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");
    assertEquals(2, stageQuery.count());

    CaseExecution enabledStageCaseExecution = stageQuery.enabled().singleResult();
    assertNotNull(enabledStageCaseExecution);
    CaseExecution availableStageCaseExecution = stageQuery.available().singleResult();
    assertNotNull(availableStageCaseExecution);

    // start enabled stage
    manualStart(enabledStageCaseExecution.getId());

    CaseExecution humanTaskCaseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // when (1)
    manualStart(humanTaskCaseExecution.getId());
    complete(humanTaskCaseExecution.getId());

    // then (1)
    stageQuery = caseService.createCaseExecutionQuery().activityId("PI_Stage_1");
    assertEquals(2, stageQuery.count());

    enabledStageCaseExecution = stageQuery.enabled().singleResult();
    assertNotNull(enabledStageCaseExecution);
    assertEquals(availableStageCaseExecution.getId(), enabledStageCaseExecution.getId());

    availableStageCaseExecution = stageQuery.available().singleResult();
    assertNotNull(availableStageCaseExecution);

    // start enabled stage
    manualStart(enabledStageCaseExecution.getId());

    humanTaskCaseExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    // when (2)
    manualStart(humanTaskCaseExecution.getId());
    complete(humanTaskCaseExecution.getId());

    // then (2)
    stageQuery = caseService.createCaseExecutionQuery().activityId("PI_Stage_1");
    assertEquals(2, stageQuery.count());

    enabledStageCaseExecution = stageQuery.enabled().singleResult();
    assertNotNull(enabledStageCaseExecution);
    assertEquals(availableStageCaseExecution.getId(), enabledStageCaseExecution.getId());

    availableStageCaseExecution = stageQuery.available().singleResult();
    assertNotNull(availableStageCaseExecution);

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
        .enabled();

    String enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isAvailable());

    // when
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then
    // there is only one instance of PI_HumanTask_2
    secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
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
    CaseExecutionQuery enabledQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .enabled();

    String enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    // when (1)
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
    assertEquals(1, query.enabled().count());
    assertEquals(1, query.available().count());

    // when (2)
    // get another enabled task
    enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (2)
    query = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_2");
    assertEquals(3, query.count());
    assertEquals(2, query.enabled().count());
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
    CaseExecutionQuery enabledQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .enabled();

    String enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    // when (1)
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
    assertEquals(1, query.enabled().count());
    assertEquals(1, query.available().count());

    // when (2)
    enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(3, query.count());
    assertEquals(2, query.enabled().count());
    assertEquals(1, query.available().count());

    // when (3)
    enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (3)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(3, query.count());
    assertEquals(3, query.enabled().count());
    assertEquals(0, query.available().count());
  }

  @Deployment
  public void testLimitedSequentialRepetitions() {
    // given
    VariableMap variables = Variables.createVariables().putValue("repetition", 0);
    createCaseInstanceByKey("case", variables);

    CaseExecutionQuery enabledQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .enabled();
    String enabledCaseExecutionId = enabledQuery.singleResult().getId();

    // when (1)
    manualStart(enabledCaseExecutionId);
    complete(enabledCaseExecutionId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());
    assertEquals(1, query.enabled().count());
    assertEquals(1, query.available().count());

    // when (2)
    enabledCaseExecutionId = enabledQuery.singleResult().getId();
    manualStart(enabledCaseExecutionId);
    complete(enabledCaseExecutionId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());
    assertEquals(1, query.enabled().count());
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
    manualStart(caseExecutionId);
    complete(caseExecutionId);

    // then (2)
    assertEquals(2, query.count());
  }

  @Deployment
  public void testEntryCriterionAndRepetitionCriterion() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when (1)
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());

    CaseExecution enabledCaseExecution = query.enabled().singleResult();
    assertNotNull(enabledCaseExecution);

    CaseExecution availableCaseExecution = query.available().singleResult();
    assertNotNull(availableCaseExecution);

    // when (2)
    manualStart(enabledCaseExecution.getId());
    complete(enabledCaseExecution.getId());

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, query.count());
    assertEquals(1, query.enabled().count());
    assertEquals(1, query.available().count());
  }

  @Deployment
  public void testIgnoreEntryCriterion() {
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
        .enabled();

    String enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();

    // when (1)
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (1)
    // "PI_HumanTask_2" should perform transition "enable",
    // because the entryCriterion is fulfilled.
    // -> there are two instance of "PI_HumanTask_2":
    //    - one instance is enabled
    //    - one instance is available
    CaseExecutionQuery secondHumanTaskQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, secondHumanTaskQuery.count());

    assertEquals(1, secondHumanTaskQuery.enabled().count());
    String enabledSecondHumanTaskId = secondHumanTaskQuery.singleResult().getId();

    assertEquals(1, secondHumanTaskQuery.available().count());
    String availableSecondHumanTaskId = secondHumanTaskQuery.singleResult().getId();

    // when (2)
    enabledTaskId = enabledQuery
        .listPage(0, 1)
        .get(0)
        .getId();
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (2)
    // the defined entryCriterion on "PI_HumanTask_2" will be ignored
    secondHumanTaskQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(2, secondHumanTaskQuery.count());

    assertEquals(1, secondHumanTaskQuery.enabled().count());
    assertEquals(enabledSecondHumanTaskId, secondHumanTaskQuery.singleResult().getId());

    assertEquals(1, secondHumanTaskQuery.available().count());
    assertEquals(availableSecondHumanTaskId, secondHumanTaskQuery.singleResult().getId());
  }

  @Deployment
  public void testRepetitionCriteria() {
    // given
    createCaseInstance();

    CaseExecutionQuery firstHumanTaskQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, firstHumanTaskQuery.count());

    CaseExecutionQuery secondHumanTaskQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");
    assertEquals(1, secondHumanTaskQuery.count());
    String secondHumanTaskId = secondHumanTaskQuery.singleResult().getId();

    // when (1)
    manualStart(secondHumanTaskId);
    complete(secondHumanTaskId);

    // then (1)
    assertEquals(3, firstHumanTaskQuery.count());
    assertEquals(1, firstHumanTaskQuery.available().count());
    assertEquals(2, firstHumanTaskQuery.enabled().count());

    String enabledFirstHumanTaskId = firstHumanTaskQuery
      .enabled()
      .listPage(0, 1)
      .get(0)
      .getId();

    // when (2)
    manualStart(enabledFirstHumanTaskId);
    complete(enabledFirstHumanTaskId);

    // then (2)
    firstHumanTaskQuery = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");

    assertEquals(3, firstHumanTaskQuery.count());
    assertEquals(1, firstHumanTaskQuery.available().count());
    assertEquals(2, firstHumanTaskQuery.enabled().count());
  }

  @Deployment
  public void testAutoCompleteStage() {
    // given
    VariableMap variables = Variables.createVariables().putValue("manualActivation", false);
    String caseInstanceId = createCaseInstanceByKey("case", variables).getId();

    String stageId = queryCaseExecutionByActivityId("PI_Stage_1").getId();

    // when (1)
    manualStart(stageId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    assertEquals(1, query.available().count());

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

    // when (1)
    manualStart(stageId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    assertEquals(1, query.available().count());

    assertEquals(1, query.active().count());
    String activeTaskId = query.singleResult().getId();

    // when (2)
    // completing active task
    complete(activeTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    assertEquals(1, query.available().count());

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

    String stageId = queryCaseExecutionByActivityId("PI_Stage_1").getId();

    // when (1)
    manualStart(stageId);

    // then (1)
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    assertEquals(1, query.available().count());

    assertEquals(1, query.enabled().count());
    String enabledTaskId = query.singleResult().getId();

    // when (2)
    manualStart(enabledTaskId);
    complete(enabledTaskId);

    // then (2)
    query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
    assertEquals(1, query.enabled().count());

    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertNotNull(stage);
    assertTrue(stage.isActive());

    CaseInstance caseInstance = (CaseInstance) queryCaseExecutionById(caseInstanceId);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatTask.cmmn"})
  public void testRepetitionProperty() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2");

    // when (1)
    CaseExecution secondHumanTask = query.singleResult();

    // then (1)
    assertFalse(secondHumanTask.isRepetition());

    // when (2)
    manualStart(firstHumanTaskId);
    complete(firstHumanTaskId);

    // then
    assertEquals(2, query.count());

    CaseExecution originInstance = query.enabled().singleResult();
    assertNotNull(originInstance);
    assertFalse(originInstance.isRepetition());

    CaseExecution repetitionInstance = query.available().singleResult();
    assertNotNull(repetitionInstance);
    assertTrue(repetitionInstance.isRepetition());
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
