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
package org.camunda.bpm.engine.test.cmmn.milestone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class MilestoneTest extends PluggableProcessEngineTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/milestone/MilestoneTest.testWithoutEntryCriterias.cmmn"})
  @Test
  public void testWithoutEntryCriterias() {
    // given

    // when
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    // then
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();

    assertTrue(caseInstance.isCompleted());

    Object occurVariable = caseService.getVariable(caseInstanceId, "occur");
    assertNotNull(occurVariable);
    assertTrue((Boolean) occurVariable);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/milestone/MilestoneTest.testWithEntryCriteria.cmmn"})
  @Test
  public void testWithEntryCriteria() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    CaseExecution milestone = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    assertTrue(milestone.isAvailable());

    // then
    assertNull(caseService.getVariable(caseInstanceId, "occur"));

    milestone = caseService
        .createCaseExecutionQuery()
        .available()
        .singleResult();

    assertTrue(milestone.isAvailable());

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    Object occurVariable = caseService.getVariable(caseInstanceId, "occur");
    assertNotNull(occurVariable);
    assertTrue((Boolean) occurVariable);

    milestone = caseService
        .createCaseExecutionQuery()
        .available()
        .singleResult();

    assertNull(milestone);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();

    assertTrue(caseInstance.isCompleted());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/milestone/MilestoneTest.testWithMultipleEntryCriterias.cmmn"})
  @Test
  public void testWithMultipleEntryCriterias() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    CaseExecution milestone = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    assertTrue(milestone.isAvailable());

    // then
    assertNull(caseService.getVariable(caseInstanceId, "occur"));

    milestone = caseService
        .createCaseExecutionQuery()
        .available()
        .singleResult();

    assertTrue(milestone.isAvailable());

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    Object occurVariable = caseService.getVariable(caseInstanceId, "occur");
    assertNotNull(occurVariable);
    assertTrue((Boolean) occurVariable);

    milestone = caseService
        .createCaseExecutionQuery()
        .available()
        .singleResult();

    assertNull(milestone);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();

    assertTrue(caseInstance.isActive());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/milestone/MilestoneTest.testWithEntryCriteria.cmmn"})
  @Test
  public void testActivityType() {
    // given
    caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    // when
    CaseExecution milestone = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult();

    // then
    assertEquals("milestone", milestone.getActivityType());
  }

}
