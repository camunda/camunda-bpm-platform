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
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_AUTO_COMPLETE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.SentryHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.model.cmmn.instance.Body;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.ExitCriterion;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CasePlanModelHandlerTest extends CmmnElementHandlerTest {

  protected CasePlanModelHandler handler = new CasePlanModelHandler();

  @Test
  public void testCasePlanModelActivityName() {
    // given:
    // the case plan model has a name "A CasePlanModel"
    String name = "A CasePlanModel";
    casePlanModel.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testCasePlanModelActivityType() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("casePlanModel", activityType);
  }

  @Test
  public void testCasePlanModelDescription() {
    // given
    String description = "This is a casePlanModal";
    casePlanModel.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testActivityBehavior() {
    // given: a case plan model

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof StageActivityBehavior);
  }

  @Test
  public void testWithoutParent() {
    // given: a casePlanModel

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

  @Test
  public void testExitCriteria() {
    // given

    // create sentry containing ifPart
    Sentry sentry = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    // set exitCriteria
    ExitCriterion criterion = createElement(casePlanModel, ExitCriterion.class);
    criterion.setSentry(sentry);

    // transform casePlanModel
    CmmnActivity newActivity = handler.handleElement(casePlanModel, context);

    // transform Sentry
    context.setParent(newActivity);
    SentryHandler sentryHandler = new SentryHandler();
    CmmnSentryDeclaration sentryDeclaration = sentryHandler.handleElement(sentry, context);

    // when
    handler.initializeExitCriterias(casePlanModel, newActivity, context);

    // then
    assertTrue(newActivity.getEntryCriteria().isEmpty());

    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(1, newActivity.getExitCriteria().size());

    assertEquals(sentryDeclaration, newActivity.getExitCriteria().get(0));

  }

  @Test
  public void testMultipleExitCriteria() {
    // given

    // create first sentry containing ifPart
    Sentry sentry1 = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart1 = createElement(sentry1, "abc", IfPart.class);
    ConditionExpression conditionExpression1 = createElement(ifPart1, "def", ConditionExpression.class);
    conditionExpression1.setText("${test}");

    // set first exitCriteria
    ExitCriterion criterion1 = createElement(casePlanModel, ExitCriterion.class);
    criterion1.setSentry(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    conditionExpression2.setText("${test}");

    // set second exitCriteria
    ExitCriterion criterion2 = createElement(casePlanModel, ExitCriterion.class);
    criterion2.setSentry(sentry2);

    // transform casePlanModel
    CmmnActivity newActivity = handler.handleElement(casePlanModel, context);

    context.setParent(newActivity);
    SentryHandler sentryHandler = new SentryHandler();

    // transform first Sentry
    CmmnSentryDeclaration firstSentryDeclaration = sentryHandler.handleElement(sentry1, context);

    // transform second Sentry
    CmmnSentryDeclaration secondSentryDeclaration = sentryHandler.handleElement(sentry2, context);

    // when
    handler.initializeExitCriterias(casePlanModel, newActivity, context);

    // then
    assertTrue(newActivity.getEntryCriteria().isEmpty());

    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(2, newActivity.getExitCriteria().size());

    assertTrue(newActivity.getExitCriteria().contains(firstSentryDeclaration));
    assertTrue(newActivity.getExitCriteria().contains(secondSentryDeclaration));

  }

  @Test
  public void testAutoComplete() {
    // given
    casePlanModel.setAutoComplete(true);

    // when
    CmmnActivity newActivity = handler.handleElement(casePlanModel, context);

    // then
    Object autoComplete = newActivity.getProperty(PROPERTY_AUTO_COMPLETE);
    assertNotNull(autoComplete);
    assertTrue((Boolean) autoComplete);
  }

}
