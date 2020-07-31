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

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.junit.Test;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class SentryVariableOnPartExitCriteriaTest extends CmmnTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryVariableOnPartExitCriteriaTest.testExitTaskWithVariableOnPart.cmmn"})
  @Test
  public void testExitTaskWithVariableOnPartSatisfied() {
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
      .setVariable("variable_1", 100)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertNull(secondHumanTask);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryVariableOnPartExitCriteriaTest.testExitTaskWithVariableOnPart.cmmn"})
  @Test
  public void testExitTaskWithVariableOnPartNotSatisfied() {
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
      .setVariable("variable", 100)
      .complete();

    // then
    firstHumanTask = queryCaseExecutionById(firstHumanTaskId);
    assertNull(firstHumanTask);

    secondHumanTask = queryCaseExecutionById(secondHumanTaskId);
    assertTrue(secondHumanTask.isActive());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryVariableOnPartExitCriteriaTest.testExitTaskWithMultipleOnPart.cmmn"})
  @Test
  public void testExitTaskWithMultipleOnPartSatisfied() {
    // given
    createCaseInstance();

    CaseExecution stageExecution;

    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isActive());
    
    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isActive());

    complete(humanTask1.getId());

    stageExecution = queryCaseExecutionByActivityId("Stage_1");
    // Still if part and variable on part conditions are yet to be satisfied for the exit criteria
    assertNotNull(stageExecution);

    caseService.setVariable(stageExecution.getId(), "value", 99);
    stageExecution = queryCaseExecutionByActivityId("Stage_1");
    // Still if part is yet to be satisfied for the exit criteria
    assertNotNull(stageExecution);

    caseService.setVariable(stageExecution.getId(), "value", 101);
    stageExecution = queryCaseExecutionByActivityId("Stage_1");
    // exit criteria satisfied
    assertNull(stageExecution);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryVariableOnPartExitCriteriaTest.testExitTasksOfDifferentScopes.cmmn"})
  @Test
  public void testExitMultipleTasksOfDifferentScopes() {
    // given
    createCaseInstance();

    CaseExecution stageExecution1 = queryCaseExecutionByActivityId("Stage_1");

    caseService.setVariable(stageExecution1.getId(), "value", 101);

    stageExecution1 = queryCaseExecutionByActivityId("Stage_1");
    assertNull(stageExecution1);

    CaseExecution stageExecution2 = queryCaseExecutionByActivityId("Stage_2");
    assertNull(stageExecution2);
    
  }
}
