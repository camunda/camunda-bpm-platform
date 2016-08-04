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
package org.camunda.bpm.engine.test.cmmn.stage;

import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class AutoCompleteTest extends CmmnProcessEngineTestCase {

  protected static final String CASE_DEFINITION_KEY = "case";

  @Deployment
  public void testCasePlanModel() {
    // given
    // a deployed process

    // when
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    // then
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    // humanTask1 and humanTask2 are not available
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();
    assertNull(query.activityId("PI_HumanTask_1").singleResult());
    assertNull(query.activityId("PI_HumanTask_2").singleResult());
  }

  @Deployment
  public void testStage() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1");

    String stageId = query.singleResult().getId();

    // when
    caseService.manuallyStartCaseExecution(stageId);

    // then

    // the instance is still active (contains
    // a further human task)
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    // humanTask1 is still available
    assertNotNull(query.activityId("PI_HumanTask_1").singleResult());

    // stage, humanTask2, humanTask3 are not available
    assertNull(query.activityId("PI_Stage_1").singleResult());
    assertNull(query.activityId("PI_HumanTask_2").singleResult());
    assertNull(query.activityId("PI_HumanTask_3").singleResult());
  }

  @Deployment
  public void testManualActivationDisabled() {
    // given
    // a deployed case definition

    // when (1)
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    // then (1)
    CaseInstanceQuery instanceQuery = caseService
      .createCaseInstanceQuery()
      .caseInstanceId(caseInstanceId);

    CaseInstance caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();

    String humanTask2Id = executionQuery
      .activityId("PI_HumanTask_2")
      .singleResult()
      .getId();

    // when (2)
    caseService.completeCaseExecution(humanTask2Id);

    // then (2)
    caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    // humanTask1 and humanTask2 are not available
    assertNull(executionQuery.activityId("PI_HumanTask_1").singleResult());
    assertNull(executionQuery.activityId("PI_HumanTask_2").singleResult());
  }

  @Deployment
  public void testManualActivationDisabledInsideStage() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();

    String stageId = executionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // then (1)
    CaseExecution stage = executionQuery
        .activityId("PI_Stage_1")
        .singleResult();
    assertNotNull(stage);
    assertTrue(stage.isActive());

    String humanTask2Id = executionQuery
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    // when (2)
    complete(humanTask2Id);

    // then (2)
    // the instance is still active (contains
    // a further human task)
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    // humanTask1 is still available
    assertNotNull(executionQuery.activityId("PI_HumanTask_1").singleResult());

    // stage, humanTask2, humanTask3 are not available
    assertNull(executionQuery.activityId("PI_Stage_1").singleResult());
    assertNull(executionQuery.activityId("PI_HumanTask_2").singleResult());
    assertNull(executionQuery.activityId("PI_HumanTask_3").singleResult());
  }

  @Deployment
  public void testNested() {
    // given
    // a deployed case definition

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();

    // when
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    // then
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    // stage, humanTask1, humanTask2, humanTask3 are not available
    assertNull(executionQuery.activityId("PI_Stage_1").singleResult());
    assertNull(executionQuery.activityId("PI_HumanTask_1").singleResult());
    assertNull(executionQuery.activityId("PI_HumanTask_2").singleResult());
    assertNull(executionQuery.activityId("PI_HumanTask_3").singleResult());
  }

  @Deployment
  public void testRequiredEnabled() {
    // given
    // a deployed case definition

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();
    CaseInstanceQuery instanceQuery = caseService.createCaseInstanceQuery();

    // when (1)
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    // then (1)
    CaseInstance caseInstance = instanceQuery
        .caseInstanceId(caseInstanceId)
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    String humanTask1Id = executionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();
    manualStart(humanTask1Id);

    // when (2)
    complete(humanTask1Id);

    // then (2)
    caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    String humanTask2Id = executionQuery
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();
    manualStart(humanTask2Id);

    // when (3)
    complete(humanTask2Id);

    // then (3)
    caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment
  public void testRequiredEnabledInsideStage() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();

    String humanTask3Id = executionQuery
        .activityId("PI_HumanTask_3")
        .singleResult()
        .getId();

    // when (1)
    complete(humanTask3Id);

    // then (1)
    CaseExecution stage = executionQuery
        .activityId("PI_Stage_1")
        .singleResult();
    assertNotNull(stage);
    assertTrue(stage.isActive());

    String humanTask2Id = executionQuery
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    // when (2)
    complete(humanTask2Id);

    // then (2)
    assertNull(executionQuery.activityId("PI_Stage_1").singleResult());

    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .caseInstanceId(caseInstanceId)
      .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment
  public void testEntryCriteriaAndManualActivationDisabled() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();

    String humanTask1Id = executionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when (1)
    complete(humanTask1Id);

    // then (1)
    CaseInstanceQuery instanceQuery = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId);

    CaseInstance caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    String humanTask2Id = executionQuery
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    // when (2)
    complete(humanTask2Id);

    // then (2)
    caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment
  public void testExitCriteriaAndRequiredEnabled() {
    // given
    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();

    String humanTask1Id = executionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    CaseExecution humanTask2 = executionQuery
      .activityId("PI_HumanTask_2")
      .singleResult();

    manualStart(humanTask2.getId());
 
    // when
    complete(humanTask1Id);

    // then
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/stage/AutoCompleteTest.testRequiredEnabled.cmmn"})
  public void testTerminate() {
    // given
    // a deployed case definition

    String caseInstanceId = createCaseInstanceByKey(CASE_DEFINITION_KEY).getId();

    CaseExecutionQuery executionQuery = caseService.createCaseExecutionQuery();
    CaseInstanceQuery instanceQuery = caseService.createCaseInstanceQuery().caseInstanceId(caseInstanceId);

    String humanTask2Id = executionQuery
      .activityId("PI_HumanTask_2")
      .singleResult()
      .getId();
    manualStart(humanTask2Id);

    // when
    terminate(humanTask2Id);

    // then
    CaseInstance caseInstance = instanceQuery.singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

}
