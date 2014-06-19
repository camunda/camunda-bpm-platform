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
package org.camunda.bpm.engine.test.cmmn.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.HumanTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CmmnHandlerContext;
import org.camunda.bpm.engine.impl.cmmn.handler.HumanTaskDiscretionaryItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.model.cmmn.instance.DiscretionaryItem;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanningTable;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskDicretionaryItemHandlerTest extends CmmnElementHandlerTest {

  protected HumanTask humanTask;
  protected PlanningTable planningTable;
  protected DiscretionaryItem discretionaryItem;
  protected HumanTaskDiscretionaryItemHandler handler = new HumanTaskDiscretionaryItemHandler();
  protected CmmnHandlerContext context;

  @Before
  public void setUp() {
    humanTask = createElement(casePlanModel, "aHumanTask", HumanTask.class);

    planningTable = createElement(humanTask, "aPlanningTable", PlanningTable.class);

    discretionaryItem = createElement(planningTable, "DI_aHumanTask", DiscretionaryItem.class);
    discretionaryItem.setDefinition(humanTask);

    context = new CmmnHandlerContext();
  }

  @Test
  public void testHumanTaskActivityName() {
    // given:
    // the humanTask has a name "A HumanTask"
    String name = "A HumanTask";
    humanTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof HumanTaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given: a humanTask with isBlocking = true (defaultValue)

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    // a humanTask with isBlocking = false
    humanTask.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
    assertFalse(isBlocking);
  }

  @Test
  public void testIsDiscretionaryProperty() {
    // given:
    // a discretionary item to handle

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    Boolean discretionary = (Boolean) activity.getProperty("discretionary");
    assertTrue(discretionary);
  }

  @Test
  public void testWithoutParent() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertNull(activity.getParent());
  }

  @Test
  public void testWithParent() {
    // given:
    // a new activity as parent
    CmmnCaseDefinition parent = new CmmnCaseDefinition("aParentActivity");
    context.setParent(parent);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

}
