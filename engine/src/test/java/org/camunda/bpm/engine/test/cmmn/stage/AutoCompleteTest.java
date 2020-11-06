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
package org.camunda.bpm.engine.test.cmmn.stage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class AutoCompleteTest extends CmmnTest {

  protected static final String CASE_DEFINITION_KEY = "case";

  @Deployment
  @Test
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
  @Test
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
  @Test
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
  @Test
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
  @Test
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
  @Test
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
  @Test
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
  @Test
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
  @Test
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
  @Test
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

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/cmmn/stage/AutoCompleteTest.testProcessTasksOnStage.cmmn",
    "org/camunda/bpm/engine/test/cmmn/stage/AutoCompleteTest.testProcessTasksOnStage.bpmn"
  })
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testProcessTasksOnStage() {
    // given

    // when
    createCaseInstanceByKey(CASE_DEFINITION_KEY);

    List<HistoricCaseActivityInstance> historicCaseActivityInstances =
      historyService.createHistoricCaseActivityInstanceQuery()
      .caseActivityType("processTask")
      .list();

    // then
    assertThat(historicCaseActivityInstances).hasSize(2);
  }

}
