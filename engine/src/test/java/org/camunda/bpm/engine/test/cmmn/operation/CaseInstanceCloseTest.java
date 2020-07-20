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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
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
public class CaseInstanceCloseTest {

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+---+
   *   |                       |
   *   |     +-------+         |
   *   |     |   A   |         |
   *   |     +-------+         |
   *   |                       |
   *   +-----------------------+
   *
   */
  @Test
  public void testCloseCompletedCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("close", stateTransitionCollector)
      .createActivity("A")
        .behavior(new TaskWaitState())
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // disable task A -> completes case instance
    taskA.disable();

    assertTrue(caseInstance.isCompleted());

    // when

    // close case
    caseInstance.close();

    // then
    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // completed --close(Case1)--> closed
    expectedStateTransitions.add("completed --close(Case1)--> closed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    assertTrue(caseInstance.isClosed());
  }

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+---+
   *   |                       |
   *   |     +-------+         |
   *   |     |   A   |         |
   *   |     +-------+         |
   *   |                       |
   *   +-----------------------+
   *
   */
  @Test
  public void testCloseTerminatedCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("close", stateTransitionCollector)
      .createActivity("A")
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    caseInstance.terminate();
    assertTrue(caseInstance.isTerminated());

    // when

    // close case
    caseInstance.close();

    // then
    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // terminated --close(Case1)--> closed
    expectedStateTransitions.add("terminated --close(Case1)--> closed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    assertTrue(caseInstance.isClosed());
  }

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+---+
   *   |                       |
   *   |     +-------+         |
   *   |     |   A   |         |
   *   |     +-------+         |
   *   |                       |
   *   +-----------------------+
   *
   */
  @Test
  public void testCloseSuspendedCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("close", stateTransitionCollector)
      .createActivity("A")
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    caseInstance.suspend();
    assertTrue(caseInstance.isSuspended());

    // when

    // close case
    caseInstance.close();

    // then
    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // suspended --close(Case1)--> closed
    expectedStateTransitions.add("suspended --close(Case1)--> closed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    assertTrue(caseInstance.isClosed());

    assertNull(caseInstance.findCaseExecution("A"));
  }


  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+---+
   *   |                       |
   *   |     +-------+         |
   *   |     |   A   |         |
   *   |     +-------+         |
   *   |                       |
   *   +-----------------------+
   *
   */
  @Test
  public void testCloseActiveCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("close", stateTransitionCollector)
      .createActivity("A")
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    assertTrue(caseInstance.isActive());

    try {
      // when
      caseInstance.close();
    } catch (CaseIllegalStateTransitionException e) {

    }

    // then
    assertTrue(stateTransitionCollector.stateTransitions.isEmpty());

    assertTrue(caseInstance.isActive());

    assertNotNull(caseInstance.findCaseExecution("A"));
  }

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+---+
   *   |                       |
   *   |     +-------+         |
   *   |     |   A   |         |
   *   |     +-------+         |
   *   |                       |
   *   +-----------------------+
   *
   */
  @Test
  public void testCloseTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("close", stateTransitionCollector)
      .createActivity("A")
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    try {
      // when
      taskA.close();
      fail("It should not be possible to close a task.");
    } catch (CaseIllegalStateTransitionException e) {

    }

    // then
    assertTrue(stateTransitionCollector.stateTransitions.isEmpty());

    assertTrue(caseInstance.isActive());
    assertNotNull(caseInstance.findCaseExecution("A"));
  }

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+-----------------+
   *   |                                     |
   *   |     +------------------------+      |
   *   |    / X                        \     |
   *   |   +    +-------+  +-------+    +    |
   *   |   |    |   A   |  |   B   |    |    |
   *   |   +    +-------+  +-------+    +    |
   *   |    \                          /     |
   *   |     +------------------------+      |
   *   |                                     |
   *   +-------------------------------------+
   *
   */
  @Test
  public void testCloseStage() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("close", stateTransitionCollector)
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

    CmmnActivityExecution stageX = caseInstance.findCaseExecution("X");

    try {
      // when
      stageX.close();
      fail("It should not be possible to close a stage.");
    } catch (CaseIllegalStateTransitionException e) {

    }

    // then
    assertTrue(stateTransitionCollector.stateTransitions.isEmpty());

    assertTrue(caseInstance.isActive());
    assertNotNull(caseInstance.findCaseExecution("X"));
  }
}
