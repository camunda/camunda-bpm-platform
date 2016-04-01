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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.TimerEventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.TimerEventListenerItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventListenerJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventListenerJobHandler;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.TimerEventListener;
import org.camunda.bpm.model.cmmn.instance.TimerExpression;
import org.junit.Before;
import org.junit.Test;

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
    //given:
    //create a timer expression;
    TimerExpression timerExprElement = createElement(timerEventListener, TimerExpression.class);
    timerExprElement.setText("${aTest}");
    Cmmn.validateModel(modelInstance);

    //when
    CmmnActivity newActivity = timerEventListenerItemHandler.handleElement(planItem, context);

    //then
   assertNotNull(newActivity.getProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION));

  }

  @Test
  public void testJobHandlerType(){
    //given:
    //create a timer expression;
    TimerExpression timerExprElement = createElement(timerEventListener, TimerExpression.class);
    timerExprElement.setText("R/10S");
    Cmmn.validateModel(modelInstance);

    //when
    CmmnActivity newActivity = timerEventListenerItemHandler.handleElement(planItem, context);

    //then
    Object jobDeclr = newActivity.getProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION);
    assertNotNull(jobDeclr);
    assertTrue(jobDeclr instanceof TimerEventListenerJobDeclaration);
    TimerEventListenerJobDeclaration timerEventListenerJobDeclaration = (TimerEventListenerJobDeclaration)jobDeclr;
    assertEquals(TimerEventListenerJobHandler.TYPE ,timerEventListenerJobDeclaration.getJobHandlerType());
  }

  @Test
  public void testCycleTimerExpression(){
    //given:
    //create a timer expression;
    TimerExpression timerExprElement = createElement(timerEventListener, TimerExpression.class);
    timerExprElement.setText("R/10S");
    Cmmn.validateModel(modelInstance);

    //when
    CmmnActivity newActivity = timerEventListenerItemHandler.handleElement(planItem, context);

    //then
    Object jobDeclr = newActivity.getProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION);
    assertNotNull(jobDeclr);
    assertTrue(jobDeclr instanceof TimerEventListenerJobDeclaration);
    TimerEventListenerJobDeclaration timerEventListenerJobDeclaration = (TimerEventListenerJobDeclaration)jobDeclr;
    assertEquals(TimerDeclarationType.CYCLE, timerEventListenerJobDeclaration.getTimerDeclarationType());
  }

  @Test
  public void testDurationTimerExpression(){
    //given:
    //create a timer expression;
    TimerExpression timerExprElement = createElement(timerEventListener, TimerExpression.class);
    timerExprElement.setText("P5S");
    Cmmn.validateModel(modelInstance);

    //when
    CmmnActivity newActivity = timerEventListenerItemHandler.handleElement(planItem, context);

    //then
    Object jobDeclr = newActivity.getProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION);
    assertNotNull(jobDeclr);
    assertTrue(jobDeclr instanceof TimerEventListenerJobDeclaration);
    TimerEventListenerJobDeclaration timerEventListenerJobDeclaration = (TimerEventListenerJobDeclaration)jobDeclr;
    assertEquals(TimerDeclarationType.DURATION, timerEventListenerJobDeclaration.getTimerDeclarationType());
  }

  @Test
  public void testDateTimerExpression(){
    //given:
    //create a timer expression;
    TimerExpression timerExprElement = createElement(timerEventListener, TimerExpression.class);
    timerExprElement.setText("2016/04/06 10:10:10");
    Cmmn.validateModel(modelInstance);

    //when
    CmmnActivity newActivity = timerEventListenerItemHandler.handleElement(planItem, context);

    //then
    Object jobDeclr = newActivity.getProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION);
    assertNotNull(jobDeclr);
    assertTrue(jobDeclr instanceof TimerEventListenerJobDeclaration);
    TimerEventListenerJobDeclaration timerEventListenerJobDeclaration = (TimerEventListenerJobDeclaration)jobDeclr;
    assertEquals(TimerDeclarationType.DATE, timerEventListenerJobDeclaration.getTimerDeclarationType());
  }

}
