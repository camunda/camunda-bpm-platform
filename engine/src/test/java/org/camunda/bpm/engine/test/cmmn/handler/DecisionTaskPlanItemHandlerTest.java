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
package org.camunda.bpm.engine.test.cmmn.handler;

import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_DESCRIPTION;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_TYPE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_IS_BLOCKING;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REQUIRED_RULE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.helper.CmmnProperties;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.DecisionTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.DmnDecisionTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.DecisionTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.SentryHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.dmn.result.DecisionResultMapper;
import org.camunda.bpm.engine.impl.dmn.result.ResultListDecisionTableResultMapper;
import org.camunda.bpm.engine.impl.dmn.result.SingleResultDecisionResultMapper;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.instance.Body;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.DecisionRefExpression;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;
import org.camunda.bpm.model.cmmn.instance.DefaultControl;
import org.camunda.bpm.model.cmmn.instance.Documentation;
import org.camunda.bpm.model.cmmn.instance.EntryCriterion;
import org.camunda.bpm.model.cmmn.instance.ExitCriterion;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.ItemControl;
import org.camunda.bpm.model.cmmn.instance.ManualActivationRule;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemControl;
import org.camunda.bpm.model.cmmn.instance.RepetitionRule;
import org.camunda.bpm.model.cmmn.instance.RequiredRule;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class DecisionTaskPlanItemHandlerTest extends CmmnElementHandlerTest {

  protected DecisionTask decisionTask;
  protected PlanItem planItem;
  protected DecisionTaskItemHandler handler = new DecisionTaskItemHandler();

  @Before
  public void setUp() {
    decisionTask = createElement(casePlanModel, "aHumanTask", DecisionTask.class);

    planItem = createElement(casePlanModel, "PI_aHumanTask", PlanItem.class);
    planItem.setDefinition(decisionTask);
  }

  @Test
  public void testActivityName() {
    // given:
    String name = "A DecisionTask";
    decisionTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testPlanItemActivityName() {
    // given:
    String humanTaskName = "A DecisionTask";
    decisionTask.setName(humanTaskName);

    String planItemName = "My LocalName";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNotEquals(humanTaskName, activity.getName());
    assertEquals(planItemName, activity.getName());
  }

  @Test
  public void testActivityType() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("decisionTask", activityType);
  }

  @Test
  public void testDescriptionProperty() {
    // given
    String description = "This is a decisionTask";
    decisionTask.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testPlanItemDescriptionProperty() {
    // given
    String description = "This is a planItem";
    planItem.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testDocumentation() {
    // given
    String description = "This is a documenation";
    Documentation documentation = createElement(decisionTask, Documentation.class);
    documentation.setTextContent(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testPlanItemDocumentation() {
    // given
    String description = "This is a planItem";
    Documentation documentationElem = createElement(planItem, Documentation.class);
    documentationElem.setTextContent(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof DmnDecisionTaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty(PROPERTY_IS_BLOCKING);
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    decisionTask.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty(PROPERTY_IS_BLOCKING);
    assertFalse(isBlocking);
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
    ExitCriterion criterion = createElement(planItem, ExitCriterion.class);
    criterion.setSentry(sentry);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration sentryDeclaration = new SentryHandler().handleElement(sentry, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

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
    Body body1 = createElement(conditionExpression1, null, Body.class);
    body1.setTextContent("${test}");

    // set first exitCriteria
    ExitCriterion criterion1 = createElement(planItem, ExitCriterion.class);
    criterion1.setSentry(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    Body body2 = createElement(conditionExpression2, null, Body.class);
    body2.setTextContent("${test}");

    // set second exitCriteria
    ExitCriterion criterion2 = createElement(planItem, ExitCriterion.class);
    criterion2.setSentry(sentry2);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration firstSentryDeclaration = new SentryHandler().handleElement(sentry1, context);
    CmmnSentryDeclaration secondSentryDeclaration = new SentryHandler().handleElement(sentry2, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getEntryCriteria().isEmpty());

    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(2, newActivity.getExitCriteria().size());

    assertTrue(newActivity.getExitCriteria().contains(firstSentryDeclaration));
    assertTrue(newActivity.getExitCriteria().contains(secondSentryDeclaration));
  }

  @Test
  public void testEntryCriteria() {
    // given

    // create sentry containing ifPart
    Sentry sentry = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    // set entryCriteria
    EntryCriterion criterion = createElement(planItem, EntryCriterion.class);
    criterion.setSentry(sentry);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration sentryDeclaration = new SentryHandler().handleElement(sentry, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getExitCriteria().isEmpty());

    assertFalse(newActivity.getEntryCriteria().isEmpty());
    assertEquals(1, newActivity.getEntryCriteria().size());

    assertEquals(sentryDeclaration, newActivity.getEntryCriteria().get(0));
  }

  @Test
  public void testMultipleEntryCriteria() {
    // given

    // create first sentry containing ifPart
    Sentry sentry1 = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart1 = createElement(sentry1, "abc", IfPart.class);
    ConditionExpression conditionExpression1 = createElement(ifPart1, "def", ConditionExpression.class);
    Body body1 = createElement(conditionExpression1, null, Body.class);
    body1.setTextContent("${test}");

    // set first entryCriteria
    EntryCriterion criterion1 = createElement(planItem, EntryCriterion.class);
    criterion1.setSentry(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    Body body2 = createElement(conditionExpression2, null, Body.class);
    body2.setTextContent("${test}");

    // set second entryCriteria
    EntryCriterion criterion2 = createElement(planItem, EntryCriterion.class);
    criterion2.setSentry(sentry2);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration firstSentryDeclaration = new SentryHandler().handleElement(sentry1, context);
    CmmnSentryDeclaration secondSentryDeclaration = new SentryHandler().handleElement(sentry2, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getExitCriteria().isEmpty());

    assertFalse(newActivity.getEntryCriteria().isEmpty());
    assertEquals(2, newActivity.getEntryCriteria().size());

    assertTrue(newActivity.getEntryCriteria().contains(firstSentryDeclaration));
    assertTrue(newActivity.getEntryCriteria().contains(secondSentryDeclaration));
  }

  @Test
  public void testEntryCriteriaAndExitCriteria() {
    // given

    // create sentry containing ifPart
    Sentry sentry = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    // set entry-/exitCriteria
    EntryCriterion criterion1 = createElement(planItem, EntryCriterion.class);
    criterion1.setSentry(sentry);
    ExitCriterion criterion2 = createElement(planItem, ExitCriterion.class);
    criterion2.setSentry(sentry);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration sentryDeclaration = new SentryHandler().handleElement(sentry, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(1, newActivity.getExitCriteria().size());
    assertEquals(sentryDeclaration, newActivity.getExitCriteria().get(0));

    assertFalse(newActivity.getEntryCriteria().isEmpty());
    assertEquals(1, newActivity.getEntryCriteria().size());
    assertEquals(sentryDeclaration, newActivity.getEntryCriteria().get(0));
  }

  @Test
  public void testManualActivationRule() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    ManualActivationRule manualActivationRule = createElement(itemControl, "ManualActivationRule_1", ManualActivationRule.class);
    ConditionExpression expression = createElement(manualActivationRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testManualActivationRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(decisionTask, "ItemControl_1", DefaultControl.class);
    ManualActivationRule manualActivationRule = createElement(defaultControl, "ManualActivationRule_1", ManualActivationRule.class);
    ConditionExpression expression = createElement(manualActivationRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRule() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RequiredRule requiredRule = createElement(itemControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(decisionTask, "ItemControl_1", DefaultControl.class);
    RequiredRule requiredRule = createElement(defaultControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRepetitionRuleStandardEvents() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RepetitionRule repetitionRule = createElement(itemControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(2, events.size());
    assertTrue(events.contains(CaseExecutionListener.COMPLETE));
    assertTrue(events.contains(CaseExecutionListener.TERMINATE));
  }

  @Test
  public void testRepetitionRuleStandardEventsByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(decisionTask, "DefaultControl_1", DefaultControl.class);
    RepetitionRule repetitionRule = createElement(defaultControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(2, events.size());
    assertTrue(events.contains(CaseExecutionListener.COMPLETE));
    assertTrue(events.contains(CaseExecutionListener.TERMINATE));
  }

  @Test
  public void testRepetitionRuleCustomStandardEvents() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RepetitionRule repetitionRule = createElement(itemControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    repetitionRule.setCamundaRepeatOnStandardEvent(CaseExecutionListener.DISABLE);

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertTrue(events.contains(CaseExecutionListener.DISABLE));
  }

  @Test
  public void testRepetitionRuleCustomStandardEventsByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(decisionTask, "DefaultControl_1", DefaultControl.class);
    RepetitionRule repetitionRule = createElement(defaultControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    repetitionRule.setCamundaRepeatOnStandardEvent(CaseExecutionListener.DISABLE);

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertTrue(events.contains(CaseExecutionListener.DISABLE));
  }

  @Test
  public void testWithoutParent() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNull(activity.getParent());
  }

  @Test
  public void testWithParent() {
    // given
    CmmnCaseDefinition parent = new CmmnCaseDefinition("aParentActivity");
    context.setParent(parent);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

  @Test
  public void testCallableElement() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    assertNotNull(behavior.getCallableElement());
  }

  @Test
  public void testConstantDecisionRef() {
    // given:
    String decisionRef = "aDecisionToCall";
    decisionTask.setDecision(decisionRef);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider decisionRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(decisionRefValueProvider);

    assertTrue(decisionRefValueProvider instanceof ConstantValueProvider);
    ConstantValueProvider valueProvider = (ConstantValueProvider) decisionRefValueProvider;
    assertEquals(decisionRef, valueProvider.getValue(null));
  }

  @Test
  public void testExpressionDecisionRef() {
    // given:
    String decisionRef = "${aDecisionToCall}";
    decisionTask.setDecision(decisionRef);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(caseRefValueProvider);

    assertTrue(caseRefValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) caseRefValueProvider;
    assertEquals(decisionRef, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testConstantDecisionRefExpression() {
    // given:
    String decision = "aDecisionToCall";
    DecisionRefExpression decisionRefExpression= createElement(decisionTask, DecisionRefExpression.class);
    decisionRefExpression.setText(decision);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider decisionRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(decisionRefValueProvider);

    assertTrue(decisionRefValueProvider instanceof ConstantValueProvider);
    ConstantValueProvider valueProvider = (ConstantValueProvider) decisionRefValueProvider;
    assertEquals(decision, valueProvider.getValue(null));
  }

  @Test
  public void testExpressionDecisionRefExpression() {
    // given:
    String decision = "${aDecisionToCall}";
    DecisionRefExpression decisionRefExpression= createElement(decisionTask, DecisionRefExpression.class);
    decisionRefExpression.setText(decision);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(caseRefValueProvider);

    assertTrue(caseRefValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) caseRefValueProvider;
    assertEquals(decision, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testBinding() {
    // given:
    CallableElementBinding caseBinding = CallableElementBinding.LATEST;
    decisionTask.setCamundaDecisionBinding(caseBinding.getValue());

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    CallableElementBinding binding = callableElement.getBinding();
    assertNotNull(binding);
    assertEquals(caseBinding, binding);
  }

  @Test
  public void testVersionConstant() {
    // given:
    String caseVersion = "2";
    decisionTask.setCamundaDecisionVersion(caseVersion);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseVersionValueProvider = callableElement.getVersionValueProvider();
    assertNotNull(caseVersionValueProvider);

    assertTrue(caseVersionValueProvider instanceof ConstantValueProvider);
    assertEquals(caseVersion, caseVersionValueProvider.getValue(null));
  }

  @Test
  public void testVersionExpression() {
    // given:
    String caseVersion = "${aVersion}";
    decisionTask.setCamundaDecisionVersion(caseVersion);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    BaseCallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseVersionValueProvider = callableElement.getVersionValueProvider();
    assertNotNull(caseVersionValueProvider);

    assertTrue(caseVersionValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) caseVersionValueProvider;
    assertEquals(caseVersion, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testResultVariable() {
    // given:
    decisionTask.setCamundaResultVariable("aResultVariable");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DecisionTaskActivityBehavior behavior = (DecisionTaskActivityBehavior) activity.getActivityBehavior();
    assertEquals("aResultVariable", behavior.getResultVariable());
  }

  @Test
  public void testDefaultMapDecisionResult() {
    // given:

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DmnDecisionTaskActivityBehavior behavior = (DmnDecisionTaskActivityBehavior) activity.getActivityBehavior();
    DecisionResultMapper mapper = behavior.getDecisionTableResultMapper();
    assertTrue(mapper instanceof ResultListDecisionTableResultMapper);
  }

  @Test
  public void testMapDecisionResult() {
    // given:
    decisionTask.setCamundaMapDecisionResult("singleResult");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    DmnDecisionTaskActivityBehavior behavior = (DmnDecisionTaskActivityBehavior) activity.getActivityBehavior();
    DecisionResultMapper mapper = behavior.getDecisionTableResultMapper();
    assertTrue(mapper instanceof SingleResultDecisionResultMapper);
  }

}
