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
package org.camunda.bpm.engine.test.cmmn.operation;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CaseDefinitionBuilder;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.test.PvmTestCase;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceTest extends PvmTestCase {

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
  public void testCaseInstanceWithOneTask() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("create", stateTransitionCollector)
      .createActivity("A")
        .listener("create", stateTransitionCollector)
        .listener("enable", stateTransitionCollector)
        .listener("manualStart", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .buildCaseDefinition();

    // create a new case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // expected state transitions after creation of a case instance:
    // ()        --create(Case1)--> active
    // ()        --create(A)-->     available
    // available --enable(A)-->     enabled
    List<String> expectedStateTransitions = new ArrayList<String>();
    expectedStateTransitions.add("() --create(Case1)--> active");
    expectedStateTransitions.add("() --create(A)--> available");
    expectedStateTransitions.add("available --enable(A)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // case instance is active
    assertTrue(caseInstance.isActive());

    CaseExecutionImpl instance = (CaseExecutionImpl) caseInstance;

    // case instance has one child plan item
    List<CaseExecutionImpl> childPlanItems = instance.getCaseExecutions();
    assertEquals(1, childPlanItems.size());

    CaseExecutionImpl planItemA = childPlanItems.get(0);

    // the child plan item is enabled
    assertTrue(planItemA.isEnabled());

    // the parent of the child plan item is the case instance
    assertEquals(caseInstance, planItemA.getParent());

    // manual start of A
    planItemA.manualStart();

    // expected state transition after manual start of A:
    // enabled --enable(A)--> active
    expectedStateTransitions.add("enabled --manualStart(A)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    assertTrue(planItemA.isActive());
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
  public void testCaseInstanceWithOneState() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("create", stateTransitionCollector)
      .createActivity("X")
        .listener("create", stateTransitionCollector)
        .listener("enable", stateTransitionCollector)
        .listener("manualStart", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
        .behavior(new StageActivityBehavior())
        .createActivity("A")
          .listener("create", stateTransitionCollector)
          .listener("enable", stateTransitionCollector)
          .listener("manualStart", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B")
          .listener("create", stateTransitionCollector)
          .listener("enable", stateTransitionCollector)
          .listener("manualStart", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
      .endActivity()
      .buildCaseDefinition();

    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // expected state transitions after the creation of a case instance:
    // ()        --create(Case1)--> active
    // ()        --create(X)-->     available
    // available --enable(X)-->     enabled
    List<String> expectedStateTransitions = initAndAssertExpectedTransitions(stateTransitionCollector);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    CaseExecutionImpl planItemX = assertCaseXState(caseInstance);
    List<CaseExecutionImpl> childPlanItems;


    // manual start of x
    planItemX.manualStart();

    // X should be active
    assertTrue(planItemX.isActive());

    // expected state transitions after a manual start of X:
    // enabled   --manualStart(X)--> active
    // ()        --create(A)-->      available
    // available --enable(A)-->      enabled
    // ()        --create(B)-->      available
    // available --enable(B)-->      enabled
    expectedStateTransitions.add("enabled --manualStart(X)--> active");
    expectedStateTransitions.add("() --create(A)--> available");
    expectedStateTransitions.add("available --enable(A)--> enabled");
    expectedStateTransitions.add("() --create(B)--> available");
    expectedStateTransitions.add("available --enable(B)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // X should have two chil plan items
    childPlanItems = planItemX.getCaseExecutions();
    assertEquals(2, childPlanItems.size());

    for (CmmnExecution childPlanItem : childPlanItems) {
      // both children should be enabled
      assertTrue(childPlanItem.isEnabled());

      // manual start of a child
      childPlanItem.manualStart();

      // the child should be active
      assertTrue(childPlanItem.isActive());

      // X should be the parent of both children
      assertEquals(planItemX, childPlanItem.getParent());
    }

    // expected state transitions after the manual starts of A and B:
    // enabled   --manualStart(A)--> active
    // enabled   --manualStart(B)--> active
    expectedStateTransitions.add("enabled --manualStart(A)--> active");
    expectedStateTransitions.add("enabled --manualStart(B)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

  }

  protected CaseExecutionImpl assertCaseXState(CmmnCaseInstance caseInstance) {

    // case instance is active
    assertTrue(caseInstance.isActive());

    CaseExecutionImpl instance = (CaseExecutionImpl) caseInstance;

    // case instance has one child plan item
    List<CaseExecutionImpl> childPlanItems = instance.getCaseExecutions();
    assertEquals(1, childPlanItems.size());

    CaseExecutionImpl planItemX = childPlanItems.get(0);

    // the case instance should be the parent of X
    assertEquals(caseInstance, planItemX.getParent());

    // X should be enabled
    assertTrue(planItemX.isEnabled());

    // before activation (ie. manual start) X should not have any children
    assertTrue(planItemX.getCaseExecutions().isEmpty());
    return planItemX;
  }

  @Test
  public void testCaseInstanceWithOneStateWithoutManualStartOfChildren() {
    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
        .listener("create", stateTransitionCollector)
          .createActivity("X")
            .listener("create", stateTransitionCollector)
            .listener("enable", stateTransitionCollector)
            .listener("manualStart", stateTransitionCollector)
            .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
            .behavior(new StageActivityBehavior())
          .createActivity("A")
            .listener("create", stateTransitionCollector)
            .listener("start", stateTransitionCollector)
            .behavior(new TaskWaitState())
          .endActivity()
          .createActivity("B")
            .listener("create", stateTransitionCollector)
            .listener("start", stateTransitionCollector)
            .behavior(new TaskWaitState())
          .endActivity()
        .endActivity()
        .buildCaseDefinition();

    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();
    List<String> expectedStateTransitions = initAndAssertExpectedTransitions(stateTransitionCollector);
    emptyCollector(stateTransitionCollector, expectedStateTransitions);


    // clear lists
    CaseExecutionImpl planItemX = assertCaseXState(caseInstance);

    // manual start of x
    planItemX.manualStart();

    // X should be active
    assertTrue(planItemX.isActive());

    // expected state transitions after a manual start of X:
    expectedStateTransitions.add("enabled --manualStart(X)--> active");
    expectedStateTransitions.add("() --create(A)--> available");
    expectedStateTransitions.add("available --start(A)--> active");
    expectedStateTransitions.add("() --create(B)--> available");
    expectedStateTransitions.add("available --start(B)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // X should have two chil plan items
    List<CaseExecutionImpl> childPlanItems;
    childPlanItems = planItemX.getCaseExecutions();
    assertEquals(2, childPlanItems.size());

    for (CmmnExecution childPlanItem : childPlanItems) {
      // both children should be active
      assertTrue(childPlanItem.isActive());

      // X should be the parent of both children
      assertEquals(planItemX, childPlanItem.getParent());
    }
  }

  protected void emptyCollector(CaseExecutionStateTransitionCollector stateTransitionCollector, List<String> expectedStateTransitions) {
    // clear lists
    expectedStateTransitions.clear();
    stateTransitionCollector.stateTransitions.clear();
  }

  protected List<String> initAndAssertExpectedTransitions(CaseExecutionStateTransitionCollector stateTransitionCollector) {
    // expected state transitions after the creation of a case instance:
    // ()        --create(Case1)--> active
    // ()        --create(X)-->     available
    // available --enable(X)-->     enabled
    List<String> expectedStateTransitions = new ArrayList<String>();
    expectedStateTransitions.add("() --create(Case1)--> active");
    expectedStateTransitions.add("() --create(X)--> available");
    expectedStateTransitions.add("available --enable(X)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);
    return expectedStateTransitions;
  }


  /**
  *
  *   +-----------------+
  *   | Case1            \
  *   +-------------------+-------------------+
  *   |                                       |
  *   |  +-------+                            |
  *   |  |  A1   |                            |
  *   |  +-------+                            |
  *   |                                       |
  *   |    +------------------------+         |
  *   |   / X1                       \        |
  *   |  +    +-------+  +-------+    +       |
  *   |  |    |  A2   |  |  B1   |    |       |
  *   |  +    +-------+  +-------+    +       |
  *   |   \                          /        |
  *   |    +------------------------+         |
  *   |                                       |
  *   |    +-----------------------------+    |
  *   |   / Y                             \   |
  *   |  +    +-------+                    +  |
  *   |  |    |   C   |                    |  |
  *   |  |    +-------+                    |  |
  *   |  |                                 |  |
  *   |  |   +------------------------+    |  |
  *   |  |  / X2                       \   |  |
  *   |  | +    +-------+  +-------+    +  |  |
  *   |  | |    |  A3   |  |  B2   |    |  |  |
  *   |  | +    +-------+  +-------+    +  |  |
  *   |  |  \                          /   |  |
  *   |  +   +------------------------+    +  |
  *   |   \                               /   |
  *   |    +-----------------------------+    |
  *   |                                       |
  *   +---------------------------------------+
  *
  */
  @Test
  public void testStartComplexCaseInstance() {

    CaseExecutionStateTransitionCollector stateTransitionCollector = new CaseExecutionStateTransitionCollector();

    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .listener("create", stateTransitionCollector)
      .createActivity("A1")
        .listener("create", stateTransitionCollector)
        .listener("enable", stateTransitionCollector)
        .listener("manualStart", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
        .behavior(new TaskWaitState())
      .endActivity()
      .createActivity("X1")
        .listener("create", stateTransitionCollector)
        .listener("enable", stateTransitionCollector)
        .listener("manualStart", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
        .behavior(new StageActivityBehavior())
        .createActivity("A2")
          .listener("create", stateTransitionCollector)
          .listener("enable", stateTransitionCollector)
          .listener("manualStart", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("B1")
          .listener("create", stateTransitionCollector)
          .listener("enable", stateTransitionCollector)
          .listener("manualStart", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
      .endActivity()
      .createActivity("Y")
        .listener("create", stateTransitionCollector)
        .listener("enable", stateTransitionCollector)
        .listener("manualStart", stateTransitionCollector)
        .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
        .behavior(new StageActivityBehavior())
        .createActivity("C")
          .listener("create", stateTransitionCollector)
          .listener("enable", stateTransitionCollector)
          .listener("manualStart", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
          .behavior(new TaskWaitState())
        .endActivity()
        .createActivity("X2")
          .listener("create", stateTransitionCollector)
          .listener("enable", stateTransitionCollector)
          .listener("manualStart", stateTransitionCollector)
          .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
          .behavior(new StageActivityBehavior())
          .createActivity("A3")
            .listener("create", stateTransitionCollector)
            .listener("enable", stateTransitionCollector)
            .listener("manualStart", stateTransitionCollector)
            .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
            .behavior(new TaskWaitState())
          .endActivity()
          .createActivity("B2")
            .listener("create", stateTransitionCollector)
            .listener("enable", stateTransitionCollector)
            .listener("manualStart", stateTransitionCollector)
            .property(ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE, defaultManualActivation())
            .behavior(new TaskWaitState())
          .endActivity()
        .endActivity()
      .endActivity()
      .buildCaseDefinition();

    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // expected state transitions after the creation of a case instance:
    // ()        --create(Case1)--> active
    // ()        --create(A1)-->    available
    // available --enable(A1)-->    enabled
    // ()        --create(X1)-->    available
    // available --enable(X1)-->    enabled
    // ()        --create(Y)-->     available
    // available --enable(Y)-->     enabled
    List<String> expectedStateTransitions = new ArrayList<String>();
    expectedStateTransitions.add("() --create(Case1)--> active");
    expectedStateTransitions.add("() --create(A1)--> available");
    expectedStateTransitions.add("available --enable(A1)--> enabled");
    expectedStateTransitions.add("() --create(X1)--> available");
    expectedStateTransitions.add("available --enable(X1)--> enabled");
    expectedStateTransitions.add("() --create(Y)--> available");
    expectedStateTransitions.add("available --enable(Y)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    CaseExecutionImpl instance = (CaseExecutionImpl) caseInstance;

    // the case instance should be active
    assertTrue(instance.isActive());

    // the case instance should have three child plan items (A1, X1, Y)
    List<CaseExecutionImpl> childPlanItems = instance.getCaseExecutions();
    assertEquals(3, childPlanItems.size());

    // handle plan item A1 //////////////////////////////////////////////////

    CaseExecutionImpl planItemA1 = (CaseExecutionImpl) instance.findCaseExecution("A1");

    // case instance should be the parent of A1
    assertEquals(caseInstance, planItemA1.getParent());

    // A1 should be enabled
    assertTrue(planItemA1.isEnabled());

    // manual start of A1
    planItemA1.manualStart();

    // A1 should be active
    assertTrue(planItemA1.isActive());

    // expected state transitions:
    // enabled --manualStart(A1)--> active
    expectedStateTransitions.add("enabled --manualStart(A1)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item X1 ///////////////////////////////////////////////////

    CaseExecutionImpl planItemX1 = (CaseExecutionImpl) instance.findCaseExecution("X1");

    // case instance should be the parent of X1
    assertEquals(caseInstance, planItemX1.getParent());

    // X1 should be enabled
    assertTrue(planItemX1.isEnabled());

    // manual start of X1
    planItemX1.manualStart();

    // X1 should be active
    assertTrue(planItemX1.isActive());

    // X1 should have two children
    childPlanItems = planItemX1.getCaseExecutions();
    assertEquals(2, childPlanItems.size());

    // expected state transitions after manual start of X1:
    // enabled   --manualStart(X1)--> active
    // ()        --create(A2)-->      available
    // available --enable(A2)-->      enabled
    // ()        --create(B1)-->      available
    // available --enable(B1)-->      enabled
    expectedStateTransitions.add("enabled --manualStart(X1)--> active");
    expectedStateTransitions.add("() --create(A2)--> available");
    expectedStateTransitions.add("available --enable(A2)--> enabled");
    expectedStateTransitions.add("() --create(B1)--> available");
    expectedStateTransitions.add("available --enable(B1)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item A2 ////////////////////////////////////////////////

    CaseExecutionImpl planItemA2 = (CaseExecutionImpl) instance.findCaseExecution("A2");

    // X1 should be the parent of A2
    assertEquals(planItemX1, planItemA2.getParent());

    // A2 should be enabled
    assertTrue(planItemA2.isEnabled());

    // manual start of A2
    planItemA2.manualStart();

    // A2 should be active
    assertTrue(planItemA2.isActive());

    // expected state transition after manual start of A2:
    // enabled --manualStart(A2)--> active
    expectedStateTransitions.add("enabled --manualStart(A2)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item B1 /////////////////////////////////////////////////

    CaseExecutionImpl planItemB1 = (CaseExecutionImpl) instance.findCaseExecution("B1");

    // X1 should be the parent of B1
    assertEquals(planItemX1, planItemB1.getParent());

    // B1 should be enabled
    assertTrue(planItemB1.isEnabled());

    // manual start of B1
    planItemB1.manualStart();

    // B1 should be active
    assertTrue(planItemB1.isActive());

    // expected state transition after manual start of B1:
    // enabled --manualStart(B1)--> active
    expectedStateTransitions.add("enabled --manualStart(B1)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item Y ////////////////////////////////////////////////

    CaseExecutionImpl planItemY = (CaseExecutionImpl) instance.findCaseExecution("Y");

    // case instance should be the parent of Y
    assertEquals(caseInstance, planItemY.getParent());

    // Y should be enabled
    assertTrue(planItemY.isEnabled());

    // manual start of Y
    planItemY.manualStart();

    // Y should be active
    assertTrue(planItemY.isActive());

    // Y should have two children
    childPlanItems = planItemY.getCaseExecutions();
    assertEquals(2, childPlanItems.size());

    // expected state transitions after manual start of Y:
    // enabled   --manualStart(Y)--> active
    // ()        --create(C)-->      available
    // available --enable(C)-->      enabled
    // ()        --create(X2)-->      available
    // available --enable(X2)-->      enabled
    expectedStateTransitions.add("enabled --manualStart(Y)--> active");
    expectedStateTransitions.add("() --create(C)--> available");
    expectedStateTransitions.add("available --enable(C)--> enabled");
    expectedStateTransitions.add("() --create(X2)--> available");
    expectedStateTransitions.add("available --enable(X2)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item C //////////////////////////////////////////////////

    CaseExecutionImpl planItemC = (CaseExecutionImpl) instance.findCaseExecution("C");

    // Y should be the parent of C
    assertEquals(planItemY, planItemC.getParent());

    // C should be enabled
    assertTrue(planItemC.isEnabled());

    // manual start of C
    planItemC.manualStart();

    // C should be active
    assertTrue(planItemC.isActive());

    // expected state transition after manual start of C:
    // enabled --manualStart(C)--> active
    expectedStateTransitions.add("enabled --manualStart(C)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item X2 ///////////////////////////////////////////

    CaseExecutionImpl planItemX2 = (CaseExecutionImpl) instance.findCaseExecution("X2");

    // Y should be the parent of X2
    assertEquals(planItemY, planItemX2.getParent());

    // X2 should be enabled
    assertTrue(planItemX2.isEnabled());

    // manual start of X2
    planItemX2.manualStart();

    // X2 should be active
    assertTrue(planItemX2.isActive());

    // X2 should have two children
    childPlanItems = planItemX2.getCaseExecutions();
    assertEquals(2, childPlanItems.size());

    // expected state transitions after manual start of X2:
    // enabled   --manualStart(X2)--> active
    // ()        --create(A3)-->      available
    // available --enable(A3)-->      enabled
    // ()        --create(B2)-->      available
    // available --enable(B2)-->      enabled
    expectedStateTransitions.add("enabled --manualStart(X2)--> active");
    expectedStateTransitions.add("() --create(A3)--> available");
    expectedStateTransitions.add("available --enable(A3)--> enabled");
    expectedStateTransitions.add("() --create(B2)--> available");
    expectedStateTransitions.add("available --enable(B2)--> enabled");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item A3 //////////////////////////////////////////////

    CaseExecutionImpl planItemA3 = (CaseExecutionImpl) instance.findCaseExecution("A3");

    // A3 should be the parent of X2
    assertEquals(planItemX2, planItemA3.getParent());

    // A3 should be enabled
    assertTrue(planItemA3.isEnabled());

    // manual start of A3
    planItemA3.manualStart();

    // A3 should be active
    assertTrue(planItemA3.isActive());

    // expected state transition after manual start of A3:
    // enabled --manualStart(A3)--> active
    expectedStateTransitions.add("enabled --manualStart(A3)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

    // handle plan item B2 /////////////////////////////////////////////////

    CaseExecutionImpl planItemB2 = (CaseExecutionImpl) instance.findCaseExecution("B2");

    // B2 should be the parent of X2
    assertEquals(planItemX2, planItemB2.getParent());

    // B2 should be enabled
    assertTrue(planItemB2.isEnabled());

    // manual start of B2
    planItemB2.manualStart();

    // B2 should be active
    assertTrue(planItemB2.isActive());

    // expected state transition after manual start of B2:
    // enabled --manualStart(B2)--> active
    expectedStateTransitions.add("enabled --manualStart(B2)--> active");

    assertEquals(expectedStateTransitions, stateTransitionCollector.stateTransitions);

    // clear lists
    emptyCollector(stateTransitionCollector, expectedStateTransitions);

  }

}
