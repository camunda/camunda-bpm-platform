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

import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_DESCRIPTION;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_TYPE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_DISCRETIONARY;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REQUIRED_RULE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.MilestoneActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.MilestoneItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.DefaultControl;
import org.camunda.bpm.model.cmmn.instance.DiscretionaryItem;
import org.camunda.bpm.model.cmmn.instance.ItemControl;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.PlanItemControl;
import org.camunda.bpm.model.cmmn.instance.PlanningTable;
import org.camunda.bpm.model.cmmn.instance.RequiredRule;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class MilestoneDiscretionaryItemHandlerTest extends CmmnElementHandlerTest {

  protected Milestone milestone;
  protected PlanningTable planningTable;
  protected DiscretionaryItem discretionaryItem;
  protected MilestoneItemHandler handler = new MilestoneItemHandler();

  @Before
  public void setUp() {
    milestone = createElement(casePlanModel, "aMilestone", Milestone.class);

    planningTable = createElement(casePlanModel, "aPlanningTable", PlanningTable.class);

    discretionaryItem = createElement(planningTable, "DI_aMilestone", DiscretionaryItem.class);
    discretionaryItem.setDefinition(milestone);

  }

  @Test
  public void testMilestoneActivityName() {
    // given:
    // the Milestone has a name "A Milestone"
    String name = "A Milestone";
    milestone.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testMilestoneActivityType() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("milestone", activityType);
  }

  @Test
  public void testMilestoneDescription() {
    // given
    String description = "This is a milestone";
    milestone.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testDiscretionaryItemDescription() {
    // given
    String description = "This is a discretionaryItem";
    discretionaryItem.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof MilestoneActivityBehavior);
  }

  @Test
  public void testIsDiscretionaryProperty() {
    // given:
    // a discretionary item to handle

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    Boolean discretionary = (Boolean) activity.getProperty(PROPERTY_DISCRETIONARY);
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

  @Test
  public void testRequiredRule() {
    // given
    ItemControl itemControl = createElement(discretionaryItem, "ItemControl_1", ItemControl.class);
    RequiredRule requiredRule = createElement(itemControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(discretionaryItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(milestone, "ItemControl_1", DefaultControl.class);
    RequiredRule requiredRule = createElement(defaultControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(discretionaryItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

}
