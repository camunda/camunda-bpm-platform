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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl;
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
public class CaseExecutionCompletionTest {

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
  public void testCompleteActiveTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // when

    // completing task A
    taskA.complete();

    // then
    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // active --complete(A)--> completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(A)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // task A is completed ...
    assertTrue(taskA.isCompleted());
    // ... and the case instance is also completed
    assertTrue(caseInstance.isCompleted());

    // task A is not part of the case instance anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // the case instance has no children
    assertTrue(((CaseExecutionImpl) caseInstance).getCaseExecutions().isEmpty());
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
  public void testManualCompleteActiveTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // when

    // completing task A
    taskA.manualComplete();

    // then
    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // active --complete(A)--> completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(A)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // task A is completed ...
    assertTrue(taskA.isCompleted());
    // ... and the case instance is also completed
    assertTrue(caseInstance.isCompleted());

    // task A is not part of the case instance anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // the case instance has no children
    assertTrue(((CaseExecutionImpl) caseInstance).getCaseExecutions().isEmpty());
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
  public void testCompleteEnabledTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    try {
      // when
      // completing task A
      taskA.complete();
      fail("It should not be possible to complete an enabled task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // task A is still enabled
      assertTrue(taskA.isEnabled());
    }

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
  public void testManualCompleteEnabledTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    try {
      // when
      // completing task A
      taskA.manualComplete();
      fail("It should not be possible to complete an enabled task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // task A is still enabled
      assertTrue(taskA.isEnabled());
    }

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
  public void testCompleteAlreadyCompletedTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    taskA.complete();

    // task A is completed
    assertTrue(taskA.isCompleted());

    try {
      // when
      // complete A
      taskA.complete();
      fail("It should not be possible to complete an already completed task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // task A is still completed
      assertTrue(taskA.isCompleted());
    }

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
  public void testManualCompleteAlreadyCompletedTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    taskA.complete();

    // task A is completed
    assertTrue(taskA.isCompleted());

    try {
      // when
      // complete A
      taskA.manualComplete();
      fail("It should not be possible to complete an already completed task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // task A is still completed
      assertTrue(taskA.isCompleted());
    }

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
  public void testCompleteTerminatedTask() {
    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    taskA.terminate();

    // task A is completed
    assertTrue(taskA.isTerminated());

    try {
      // when
      // complete A
      taskA.complete();
      fail("It should not be possible to complete an already completed task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // task A is still completed
      assertTrue(taskA.isTerminated());
    }
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
  public void testManualCompleteTerminatedTask() {
    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");
    taskA.terminate();

    // task A is completed
    assertTrue(taskA.isTerminated());

    try {
      // when
      // complete A
      taskA.manualComplete();
      fail("It should not be possible to complete an already completed task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // task A is still completed
      assertTrue(taskA.isTerminated());
    }
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
  public void testDisableTaskShouldCompleteCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("disable", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    // when
    // complete A
    taskA.disable();

    // then

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // enabled --disable(A)-->      disabled
    // active  --complete(Case1)--> completed
    expectedStateTransitions.add("enabled --disable(A)--> disabled");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // task A is disabled
    assertTrue(taskA.isDisabled());

    // case instance is completed
    assertTrue(caseInstance.isCompleted());

    assertNull(caseInstance.findCaseExecution("A"));
    assertTrue(((CaseExecutionImpl)caseInstance).getCaseExecutions().isEmpty());

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
  public void testTerminateTaskShouldCompleteCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("terminate", stateTransitionCollector)
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is active
    assertTrue(taskA.isActive());

    // when
    // terminate A
    taskA.terminate();

    // then

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // active --terminate(A)-->    terminated
    // active  --complete(Case1)--> completed
    expectedStateTransitions.add("active --terminate(A)--> terminated");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // task A is disabled
    assertTrue(taskA.isTerminated());

    // case instance is completed
    assertTrue(caseInstance.isCompleted());

    assertNull(caseInstance.findCaseExecution("A"));
    assertTrue(((CaseExecutionImpl)caseInstance).getCaseExecutions().isEmpty());

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
  public void testCompleteActiveCaseInstanceWithEnabledTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    try {
      // when
      // complete caseInstance
      caseInstance.complete();
    } catch (Exception e) {
      // then
      // case instance is still active
      assertTrue(caseInstance.isActive());

      assertNotNull(caseInstance.findCaseExecution("A"));
    }
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
  public void testManualCompleteActiveCaseInstanceWithEnabledTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    // when

    // complete caseInstance (manualCompletion == true)
    caseInstance.manualComplete();

    // then

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // the case instance
    assertTrue(caseInstance.isCompleted());

    // task A is not a child of the case instance anymore
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
  public void testCompleteActiveCaseInstanceWithActiveTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is active
    assertTrue(taskA.isActive());

    try {
      // when
      caseInstance.complete();
      fail("It should not be possible to complete a case instance containing an active task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // the case instance is still active
      assertTrue(caseInstance.isActive());
      assertFalse(caseInstance.isCompleted());
    }
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
  public void testManualCompleteActiveCaseInstanceWithActiveTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    try {
      // when
      caseInstance.manualComplete();
      fail("It should not be possible to complete a case instance containing an active task.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // the case instance is still active
      assertTrue(caseInstance.isActive());
      assertFalse(caseInstance.isCompleted());
    }
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
  public void testCompleteAlreadyCompletedCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    // case instance is already completed
    caseInstance.manualComplete();

    try {
      // when
      caseInstance.complete();
      fail("It should not be possible to complete an already completed case instance.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      // the case instance is still completed
      assertTrue(caseInstance.isCompleted());
    }

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
  public void testManualCompleteAlreadyCompletedCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("A")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution taskA = caseInstance.findCaseExecution("A");

    // task A is enabled
    assertTrue(taskA.isEnabled());

    // case instance is already completed
    caseInstance.manualComplete();

    try {
      // when
      caseInstance.manualComplete();
      fail("It should not be possible to complete an already completed case instance.");
    } catch (CaseIllegalStateTransitionException e) {
      // then

      assertThat(caseInstance.isCompleted()).describedAs("the case instance is still completed").isTrue();
    }

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
  public void testCompleteOnlyTaskA() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // complete task A
    taskA.complete();

    // then ////////////////////////////////////////////////////////////////

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transitions:
    // active --complete(A)--> completed
    expectedStateTransitions.add("active --complete(A)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();

    // task A is completed
    assertTrue(taskA.isCompleted());

    // task B is still active
    assertTrue(taskB.isActive());

    // stage X is still active
    assertTrue(stageX.isActive());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));

    // task B is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("B"));

    // stage X is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("X"));

    // case instance has only one child
    assertEquals(1, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // stage X has two children
    assertEquals(1, ((CaseExecutionImpl) stageX).getCaseExecutions().size());

    // case instance is still active
    assertTrue(caseInstance.isActive());

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
  public void testManualCompleteOnlyTaskA() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // complete task A
    taskA.manualComplete();

    // then ////////////////////////////////////////////////////////////////

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transitions:
    // active --complete(A)--> completed
    expectedStateTransitions.add("active --complete(A)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();

    // task A is completed
    assertTrue(taskA.isCompleted());

    // task B is still active
    assertTrue(taskB.isActive());

    // stage X is still active
    assertTrue(stageX.isActive());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));

    // task B is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("B"));

    // stage X is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("X"));

    // case instance has only one child
    assertEquals(1, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // stage X has two children
    assertEquals(1, ((CaseExecutionImpl) stageX).getCaseExecutions().size());

    // case instance is still active
    assertTrue(caseInstance.isActive());

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
  public void testDisableOnlyTaskA() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // disable task A
    taskA.disable();

    // then ////////////////////////////////////////////////////////////////

    assertTrue(stateTransitionCollector.stateTransitions.isEmpty());

    // task A is disabled
    assertTrue(taskA.isDisabled());

    // task B is still active
    assertTrue(taskB.isActive());

    // stage X is still active
    assertTrue(stageX.isActive());

    // task B is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("A"));

    // task B is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("B"));

    // stage X is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("X"));

    // case instance has only one child
    assertEquals(1, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // stage X has only one child
    assertEquals(2, ((CaseExecutionImpl) stageX).getCaseExecutions().size());

    // case instance is still active
    assertTrue(caseInstance.isActive());

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
  public void testTerminateOnlyTaskA() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // complete task A
    taskA.terminate();

    // then ////////////////////////////////////////////////////////////////

    assertTrue(stateTransitionCollector.stateTransitions.isEmpty());

    // task A is terminated
    assertTrue(taskA.isTerminated());

    // task B is still active
    assertTrue(taskB.isActive());

    // stage X is still active
    assertTrue(stageX.isActive());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));

    // task B is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("B"));

    // stage X is still part of the case instance
    assertNotNull(caseInstance.findCaseExecution("X"));

    // case instance has only one child
    assertEquals(1, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // stage X has only one child
    assertEquals(1, ((CaseExecutionImpl) stageX).getCaseExecutions().size());

    // case instance is still active
    assertTrue(caseInstance.isActive());

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
  public void testCompleteTaskAAndTaskB() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // complete task A
    taskA.complete();
    // complete task B
    taskB.complete();

    // then ////////////////////////////////////////////////////////////////

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transitions:
    // active --complete(A)-->     completed
    // active --complete(B)-->     completed
    // active --complete(X)-->     completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(A)--> completed");
    expectedStateTransitions.add("active --complete(B)--> completed");
    expectedStateTransitions.add("active --complete(X)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();

    // task A is completed
    assertTrue(taskA.isCompleted());

    // task B is completed
    assertTrue(taskB.isCompleted());

    // stage X is completed
    assertTrue(stageX.isCompleted());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // stage X does not contain task B anymore
    assertNull(caseInstance.findCaseExecution("B"));
    // stage X does not contain task X anymore
    assertNull(caseInstance.findCaseExecution("X"));

    // stage X has only one child
    assertEquals(0, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // case instance is completed
    assertTrue(caseInstance.isCompleted());

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
  public void testManualCompleteTaskAAndTaskB() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // complete task A
    taskA.manualComplete();
    // complete task B
    taskB.manualComplete();

    // then ////////////////////////////////////////////////////////////////

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transitions:
    // active --complete(A)-->     completed
    // active --complete(B)-->     completed
    // active --complete(X)-->     completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(A)--> completed");
    expectedStateTransitions.add("active --complete(B)--> completed");
    expectedStateTransitions.add("active --complete(X)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();

    // task A is completed
    assertTrue(taskA.isCompleted());

    // task B is completed
    assertTrue(taskB.isCompleted());

    // stage X is completed
    assertTrue(stageX.isCompleted());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // stage X does not contain task B anymore
    assertNull(caseInstance.findCaseExecution("B"));
    // stage X does not contain task X anymore
    assertNull(caseInstance.findCaseExecution("X"));

    // stage X has only one child
    assertEquals(0, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // case instance is completed
    assertTrue(caseInstance.isCompleted());

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
  public void testDisableTaskAAndTaskB() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
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

    // when ////////////////////////////////////////////////////////////////

    // disable task A
    taskA.disable();
    // disable task B
    taskB.disable();

    // then ////////////////////////////////////////////////////////////////

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transitions:
    // active --complete(X)-->     completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(X)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();

    // task A is disabled
    assertTrue(taskA.isDisabled());

    // task B is disabled
    assertTrue(taskB.isDisabled());

    // stage X is completed
    assertTrue(stageX.isCompleted());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // stage X does not contain task B anymore
    assertNull(caseInstance.findCaseExecution("B"));
    // stage X does not contain task X anymore
    assertNull(caseInstance.findCaseExecution("X"));

    // stage X has only one child
    assertEquals(0, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // case instance is completed
    assertTrue(caseInstance.isCompleted());

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
  public void testTerminateTaskAAndTaskB() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given ///////////////////////////////////////////////////////////////

    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("complete", stateTransitionCollector)
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("complete", stateTransitionCollector)
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

    // when ////////////////////////////////////////////////////////////////

    // terminate task A
    taskA.terminate();
    // terminate task B
    taskB.terminate();

    // then ////////////////////////////////////////////////////////////////

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transitions:
    // active --complete(X)-->     completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(X)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();

    // task A is terminated
    assertTrue(taskA.isTerminated());

    // task B is terminated
    assertTrue(taskB.isTerminated());

    // stage X is completed
    assertTrue(stageX.isCompleted());

    // stage X does not contain task A anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // stage X does not contain task B anymore
    assertNull(caseInstance.findCaseExecution("B"));
    // stage X does not contain task X anymore
    assertNull(caseInstance.findCaseExecution("X"));

    // stage X has only one child
    assertEquals(0, ((CaseExecutionImpl) caseInstance).getCaseExecutions().size());

    // case instance is completed
    assertTrue(caseInstance.isCompleted());

  }

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+---+
   *   |                       |
   *   |                       |
   *   |                       |
   *   |                       |
   *   |                       |
   *   +-----------------------+
   *
   */
  @Test
  public void testAutoCompletionCaseInstanceWithoutChildren() {
    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .buildCaseDefinition();

    // when

    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // then

    assertTrue(caseInstance.isCompleted());

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);
  }

  /**
   *
   *   +-----------------+
   *   | Case1            \
   *   +-------------------+-----------------+
   *   |                                     |
   *   |     +------------------------+      |
   *   |    / X                        \     |
   *   |   +                            +    |
   *   |   |                            |    |
   *   |   +                            +    |
   *   |    \                          /     |
   *   |     +------------------------+      |
   *   |                                     |
   *   +-------------------------------------+
   *
   */
  @Test
  public void testAutoCompletionStageWithoutChildren() {
    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("complete", stateTransitionCollector)
      .createActivity("X")
        .listener("complete", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, TestHelper.defaultManualActivation())
        .behavior(new StageActivityBehavior())
      .endActivity()
      .buildCaseDefinition();

    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();


    CmmnActivityExecution stageX = caseInstance.findCaseExecution("X");

    // when
    stageX.manualStart();

    // then

    assertTrue(caseInstance.isCompleted());
    assertTrue(stageX.isCompleted());

    List<String> expectedStateTransitions = new ArrayList<String>();

    // expected state transition:
    // active --complete(X)-->     completed
    // active --complete(Case1)--> completed
    expectedStateTransitions.add("active --complete(X)--> completed");
    expectedStateTransitions.add("active --complete(Case1)--> completed");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);
  }

}
