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
package org.camunda.bpm.engine.test.cmmn.operation;

import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CaseDefinitionBuilder;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionTerminationTest {

  @Test
  public void testTerminateCaseInstance() {

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .createActivity("X")
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .behavior(new TaskWaitState())
        .endActivity()
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // a case execution associated with Stage X
    CmmnActivityExecution stageX = caseInstance.findCaseExecution("X");

    // a case execution associated with Task A
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // a case execution associated with Task B
    CmmnActivityExecution taskB = caseInstance.findCaseExecution("B");

    // when

    caseInstance.terminate();

    // then
    assertTrue(caseInstance.isTerminated());
    assertTrue(stageX.isTerminated());
    assertTrue(taskA.isTerminated());
    assertTrue(taskB.isTerminated());
  }

  @Test
  public void testTerminateStage() {

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .createActivity("X")
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .behavior(new TaskWaitState())
        .endActivity()
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // a case execution associated with Stage X
    CmmnActivityExecution stageX = caseInstance.findCaseExecution("X");

    // a case execution associated with Task A
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // a case execution associated with Task B
    CmmnActivityExecution taskB = caseInstance.findCaseExecution("B");

    // when

    stageX.terminate();

    // then
    assertTrue(caseInstance.isCompleted());
    assertTrue(stageX.isTerminated());
    assertTrue(taskA.isTerminated());
    assertTrue(taskB.isTerminated());
  }

  @Test
  public void testTerminateTask() {

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .createActivity("X")
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .behavior(new TaskWaitState())
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .endActivity()
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // a case execution associated with Stage X
    CmmnActivityExecution stageX = caseInstance.findCaseExecution("X");

    // a case execution associated with Task A
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // a case execution associated with Task B
    CmmnActivityExecution taskB = caseInstance.findCaseExecution("B");

    // when

    taskA.terminate();

    // then
    assertTrue(caseInstance.isActive());
    assertTrue(stageX.isActive());
    assertTrue(taskA.isTerminated());
    assertTrue(taskB.isEnabled());
  }

}
