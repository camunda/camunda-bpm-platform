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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.SentryHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.TaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnIfPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnOnPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.model.cmmn.PlanItemTransition;
import org.camunda.bpm.model.cmmn.instance.Body;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemOnPart;
import org.camunda.bpm.model.cmmn.instance.PlanItemTransitionStandardEvent;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class SentryHandlerTest extends CmmnElementHandlerTest {

  protected Sentry sentry;
  protected PlanItemOnPart onPart;
  protected Task task;
  protected PlanItem planItem;
  protected TaskItemHandler taskItemHandler = new TaskItemHandler();
  protected SentryHandler sentryHandler = new SentryHandler();

  @Before
  public void setUp() {
    task = createElement(casePlanModel, "aTask", Task.class);

    planItem = createElement(casePlanModel, "PI_aTask", PlanItem.class);
    planItem.setDefinition(task);

    sentry = createElement(casePlanModel, "aSentry", Sentry.class);

    onPart = createElement(sentry, "anOnPart", PlanItemOnPart.class);
    onPart.setSource(planItem);
    createElement(onPart, null, PlanItemTransitionStandardEvent.class);
    onPart.setStandardEvent(PlanItemTransition.complete);
  }

  @Test
  public void testSentry() {
    // given

    // when
    CmmnSentryDeclaration sentryDeclaration = sentryHandler.handleElement(sentry, context);

    // then
    assertNotNull(sentryDeclaration);

    assertEquals(sentry.getId(), sentryDeclaration.getId());

    assertNull(sentryDeclaration.getIfPart());
    assertTrue(sentryDeclaration.getOnParts().isEmpty());
  }

  @Test
  public void testSentryWithIfPart() {
    // given
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    String expression = "${test}";
    body.setTextContent(expression);

    // when
    CmmnSentryDeclaration sentryDeclaration = sentryHandler.handleElement(sentry, context);

    // then
    assertNotNull(sentryDeclaration);

    CmmnIfPartDeclaration ifPartDeclaration = sentryDeclaration.getIfPart();
    assertNotNull(ifPartDeclaration);

    Expression condition = ifPartDeclaration.getCondition();
    assertNotNull(condition);
    assertEquals(expression, condition.getExpressionText());

    assertTrue(sentryDeclaration.getOnParts().isEmpty());

  }

  @Test
  public void testSentryWithIfPartWithMultipleCondition() {
    // given
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);

    ConditionExpression firstConditionExpression = createElement(ifPart, "con_1", ConditionExpression.class);
    Body firstBody = createElement(firstConditionExpression, null, Body.class);
    String firstExpression = "${firstExpression}";
    firstBody.setTextContent(firstExpression);

    ConditionExpression secondConditionExpression = createElement(ifPart, "con_2", ConditionExpression.class);
    Body secondBody = createElement(secondConditionExpression, null, Body.class);
    String secondExpression = "${secondExpression}";
    secondBody.setTextContent(secondExpression);

    // when
    CmmnSentryDeclaration sentryDeclaration = sentryHandler.handleElement(sentry, context);

    // then
    assertNotNull(sentryDeclaration);

    CmmnIfPartDeclaration ifPartDeclaration = sentryDeclaration.getIfPart();
    assertNotNull(ifPartDeclaration);

    Expression condition = ifPartDeclaration.getCondition();
    assertNotNull(condition);
    assertEquals(firstExpression, condition.getExpressionText());

    // the second condition will be ignored!

    assertTrue(sentryDeclaration.getOnParts().isEmpty());

  }

  @Test
  public void testSentryWithOnPart() {
    // given
    CmmnActivity casePlanModelActivity = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(casePlanModelActivity);

    CmmnSentryDeclaration sentryDeclaration = sentryHandler.handleElement(sentry, context);
    CmmnActivity source = taskItemHandler.handleElement(planItem, context);

    // when
    sentryHandler.initializeOnParts(sentry, context);

    // then
    assertNotNull(sentryDeclaration);

    List<CmmnOnPartDeclaration> onParts = sentryDeclaration.getOnParts();
    assertNotNull(onParts);
    assertFalse(onParts.isEmpty());
    assertEquals(1, onParts.size());

    List<CmmnOnPartDeclaration> onPartsAssociatedWithSource = sentryDeclaration.getOnParts(source.getId());
    assertNotNull(onPartsAssociatedWithSource);
    assertFalse(onPartsAssociatedWithSource.isEmpty());
    assertEquals(1, onParts.size());

    CmmnOnPartDeclaration onPartDeclaration = onPartsAssociatedWithSource.get(0);
    assertNotNull(onPartDeclaration);
    // source
    assertEquals(source, onPartDeclaration.getSource());
    assertEquals(onPart.getSource().getId(), onPartDeclaration.getSource().getId());
    // standardEvent
    assertEquals(onPart.getStandardEvent().name(), onPartDeclaration.getStandardEvent());
    // sentry
    assertNull(onPartDeclaration.getSentry());

    assertNull(sentryDeclaration.getIfPart());

  }

  @Test
  public void testSentryWithOnPartReferencesSentry() {
    // given
    Sentry exitSentry = createElement(casePlanModel, "anotherSentry", Sentry.class);
    IfPart ifPart = createElement(exitSentry, "IfPart_1", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "con_1", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    onPart.setSentry(exitSentry);

    CmmnActivity casePlanModelActivity = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(casePlanModelActivity);

    CmmnSentryDeclaration sentryDeclaration = sentryHandler.handleElement(sentry, context);
    CmmnSentryDeclaration exitSentryDeclaration = sentryHandler.handleElement(exitSentry, context);
    CmmnActivity source = taskItemHandler.handleElement(planItem, context);

    // when
    sentryHandler.initializeOnParts(sentry, context);

    // then
    assertNotNull(sentryDeclaration);

    List<CmmnOnPartDeclaration> onParts = sentryDeclaration.getOnParts();
    assertNotNull(onParts);
    assertFalse(onParts.isEmpty());
    assertEquals(1, onParts.size());

    List<CmmnOnPartDeclaration> onPartsAssociatedWithSource = sentryDeclaration.getOnParts(source.getId());
    assertNotNull(onPartsAssociatedWithSource);
    assertFalse(onPartsAssociatedWithSource.isEmpty());
    assertEquals(1, onParts.size());

    CmmnOnPartDeclaration onPartDeclaration = onPartsAssociatedWithSource.get(0);
    assertNotNull(onPartDeclaration);
    // source
    assertEquals(source, onPartDeclaration.getSource());
    assertEquals(onPart.getSource().getId(), onPartDeclaration.getSource().getId());
    // standardEvent
    assertEquals(onPart.getStandardEvent().name(), onPartDeclaration.getStandardEvent());
    // sentry
    assertNotNull(onPartDeclaration.getSentry());
    assertEquals(exitSentryDeclaration, onPartDeclaration.getSentry());

    assertNull(sentryDeclaration.getIfPart());

  }

}
