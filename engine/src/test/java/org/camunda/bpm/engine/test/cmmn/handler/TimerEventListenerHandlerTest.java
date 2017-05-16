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

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.EventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.TimerEventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.TimerEventListenerItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.PlanItemTransition;
import org.camunda.bpm.model.cmmn.instance.*;
import org.junit.Before;
import org.junit.Test;

import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_DESCRIPTION;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_TYPE;
import static org.junit.Assert.*;

/**
 *  @author Roman Smirnov
 *  @author Subhro
 */
public class TimerEventListenerHandlerTest extends CmmnElementHandlerTest {

  protected TimerEventListener timerEventListener;
  protected PlanItem planItem;
  protected TimerEventListenerItemHandler timerEventListenerItemHandler = new TimerEventListenerItemHandler();

  @Before
  public void setUp() {
    timerEventListener = createElement(casePlanModel, "aTimerEventListener", TimerEventListener.class);

    planItem = createElement(casePlanModel, "PI_aTimerEventListener", PlanItem.class);
    planItem.setDefinition(timerEventListener);

  }

  @Test
  public void testTimerEventListenerActivityName() {
    String timerName = "A TimerEventListener";
    timerEventListener.setName(timerName);

    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);
    assertEquals(timerName, activity.getName());

  }

  @Test
  public void testPlanItemActivityName() {
    String timerName = "A TimerEventListener";
    timerEventListener.setName(timerName);

    // the planItem has an own name "My LocalName"
    String planItemName = "My Local TimerEventListener";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);

    // then
    assertNotEquals(timerName, activity.getName());
    assertEquals(planItemName, activity.getName());
  }

  @Test
  public void testTimerEventListenerActivityType() {
    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("timerEventListener", activityType);
  }


  @Test
  public void testTimerEventListenerDescription() {
    String description = "This is a timer event listener";
    timerEventListener.setDescription(description);

    // when
    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testActivityBehavior() {
    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof TimerEventListenerActivityBehavior);

  }

  @Test
  public void testWithoutParent() {
    // given: a planItem

    // when
    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);

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
    CmmnActivity activity = timerEventListenerItemHandler.handleElement(planItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

  @Test
  public void testTimerExpression(){
    //create a timer expression;
    TimerExpression timerExprElement = createElement(timerEventListener, TimerExpression.class);
    timerExprElement.setText("${aTest}");
    Cmmn.validateModel(modelInstance);

    CmmnActivity newActivity = timerEventListenerItemHandler.handleElement(planItem, context);
   assertNotNull(newActivity.getProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION));

  }
}
