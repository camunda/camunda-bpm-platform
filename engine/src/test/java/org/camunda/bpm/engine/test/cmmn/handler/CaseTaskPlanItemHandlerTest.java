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

import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_TYPE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_DESCRIPTION;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_IS_BLOCKING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElementParameter;
import org.camunda.bpm.engine.impl.cmmn.behavior.CaseTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.CaseTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.SentryHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.model.cmmn.impl.instance.Body;
import org.camunda.bpm.model.cmmn.impl.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CaseTaskPlanItemHandlerTest extends CmmnElementHandlerTest {

  protected CaseTask caseTask;
  protected PlanItem planItem;
  protected CaseTaskItemHandler handler = new CaseTaskItemHandler();

  @Before
  public void setUp() {
    caseTask = createElement(casePlanModel, "aCaseTask", CaseTask.class);

    planItem = createElement(casePlanModel, "PI_aCaseTask", PlanItem.class);
    planItem.setDefinition(caseTask);
  }

  @Test
  public void testCaseTaskActivityName() {
    // given:
    // the caseTask has a name "A CaseTask"
    String name = "A CaseTask";
    caseTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testPlanItemActivityName() {
    // given:
    // the caseTask has a name "A CaseTask"
    String name = "A CaseTask";
    caseTask.setName(name);

    // the planItem has an own name "My LocalName"
    String planItemName = "My LocalName";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNotEquals(name, activity.getName());
    assertEquals(planItemName, activity.getName());
  }

  @Test
  public void testCaseTaskDescription() {
    // given
    String description = "This is a caseTask";
    caseTask.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, (String) activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testPlanItemDescription() {
    // given
    String description = "This is a planItem";
    planItem.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, (String) activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testCaseTaskActivityType() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("caseTask", activityType);
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof CaseTaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given: a caseTask with isBlocking = true (defaultValue)

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty(PROPERTY_IS_BLOCKING);
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    // a caseTask with isBlocking = false
    caseTask.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty(PROPERTY_IS_BLOCKING);
    assertFalse(isBlocking);
  }

  @Test
  public void testWithoutParent() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

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
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

  @Test
  public void testCallableElement() {
    // given: a plan item

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    // there exists a callableElement
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();

    assertNotNull(behavior.getCallableElement());
  }

  @Test
  public void testCaseRefConstant() {
    // given:
    String caseRef = "aCaseToCall";
    caseTask.setCase(caseRef);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(caseRefValueProvider);

    assertTrue(caseRefValueProvider instanceof ConstantValueProvider);
    ConstantValueProvider valueProvider = (ConstantValueProvider) caseRefValueProvider;
    assertEquals(caseRef, valueProvider.getValue(null));
  }

  @Test
  public void testCaseRefExpression() {
    // given:
    String caseRef = "${aCaseToCall}";
    caseTask.setCase(caseRef);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(caseRefValueProvider);

    assertTrue(caseRefValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) caseRefValueProvider;
    assertEquals(caseRef, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testBinding() {
    // given:
    CallableElementBinding caseBinding = CallableElementBinding.LATEST;
    caseTask.setCamundaCaseBinding(caseBinding.getValue());

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    CallableElementBinding binding = callableElement.getBinding();
    assertNotNull(binding);
    assertEquals(caseBinding, binding);
  }

  @Test
  public void testVersionConstant() {
    // given:
    String caseVersion = "2";
    caseTask.setCamundaCaseVersion(caseVersion);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseVersionValueProvider = callableElement.getVersionValueProvider();
    assertNotNull(caseVersionValueProvider);

    assertTrue(caseVersionValueProvider instanceof ConstantValueProvider);
    assertEquals(caseVersion, caseVersionValueProvider.getValue(null));
  }

  @Test
  public void testVersionExpression() {
    // given:
    String caseVersion = "${aVersion}";
    caseTask.setCamundaCaseVersion(caseVersion);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider caseVersionValueProvider = callableElement.getVersionValueProvider();
    assertNotNull(caseVersionValueProvider);

    assertTrue(caseVersionValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) caseVersionValueProvider;
    assertEquals(caseVersion, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testBusinessKeyConstant() {
    // given:
    String businessKey = "myBusinessKey";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn businessKeyElement = createElement(extensionElements, null, CamundaIn.class);
    businessKeyElement.setCamundaBusinessKey(businessKey);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider businessKeyValueProvider = callableElement.getBusinessKeyValueProvider();
    assertNotNull(businessKeyValueProvider);

    assertTrue(businessKeyValueProvider instanceof ConstantValueProvider);
    assertEquals(businessKey, businessKeyValueProvider.getValue(null));
  }

  @Test
  public void testBusinessKeyExpression() {
    // given:
    String businessKey = "${myBusinessKey}";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn businessKeyElement = createElement(extensionElements, null, CamundaIn.class);
    businessKeyElement.setCamundaBusinessKey(businessKey);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider businessKeyValueProvider = callableElement.getBusinessKeyValueProvider();
    assertNotNull(businessKeyValueProvider);

    assertTrue(businessKeyValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) businessKeyValueProvider;
    assertEquals(businessKey, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testInputs() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn variablesElement = createElement(extensionElements, null, CamundaIn.class);
    variablesElement.setCamundaVariables("all");
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaSource("a");
    CamundaIn sourceExpressionElement = createElement(extensionElements, null, CamundaIn.class);
    sourceExpressionElement.setCamundaSourceExpression("${b}");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    List<CallableElementParameter> inputs = callableElement.getInputs();
    assertNotNull(inputs);
    assertFalse(inputs.isEmpty());
    assertEquals(3, inputs.size());
  }

  @Test
  public void testInputVariables() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn variablesElement = createElement(extensionElements, null, CamundaIn.class);
    variablesElement.setCamundaVariables("all");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getInputs().get(0);

    assertNotNull(parameter);
    assertTrue(parameter.isAllVariables());
  }

  @Test
  public void testInputSource() {
    // given:
    String source = "a";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaSource(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getInputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    ParameterValueProvider sourceValueProvider = parameter.getSourceValueProvider();
    assertNotNull(sourceValueProvider);

    assertTrue(sourceValueProvider instanceof ConstantValueProvider);
    assertEquals(source, sourceValueProvider.getValue(null));
  }

  @Test
  public void testInputSourceExpression() {
    // given:
    String source = "${a}";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaSourceExpression(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getInputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    ParameterValueProvider sourceExpressionValueProvider = parameter.getSourceValueProvider();
    assertNotNull(sourceExpressionValueProvider);

    assertTrue(sourceExpressionValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) sourceExpressionValueProvider;
    assertEquals(source, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testInputTarget() {
    // given:
    String target = "b";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaTarget(target);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getInputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    assertEquals(target, parameter.getTarget());
  }

  @Test
  public void testOutputs() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaOut variablesElement = createElement(extensionElements, null, CamundaOut.class);
    variablesElement.setCamundaVariables("all");
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaSource("a");
    CamundaOut sourceExpressionElement = createElement(extensionElements, null, CamundaOut.class);
    sourceExpressionElement.setCamundaSourceExpression("${b}");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    List<CallableElementParameter> outputs = callableElement.getOutputs();
    assertNotNull(outputs);
    assertFalse(outputs.isEmpty());
    assertEquals(3, outputs.size());
  }

  @Test
  public void testOutputVariables() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaOut variablesElement = createElement(extensionElements, null, CamundaOut.class);
    variablesElement.setCamundaVariables("all");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getOutputs().get(0);

    assertNotNull(parameter);
    assertTrue(parameter.isAllVariables());
  }

  @Test
  public void testOutputSource() {
    // given:
    String source = "a";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaSource(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getOutputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    ParameterValueProvider sourceValueProvider = parameter.getSourceValueProvider();
    assertNotNull(sourceValueProvider);

    assertTrue(sourceValueProvider instanceof ConstantValueProvider);
    assertEquals(source, sourceValueProvider.getValue(null));
  }

  @Test
  public void testOutputSourceExpression() {
    // given:
    String source = "${a}";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaSourceExpression(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getOutputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    ParameterValueProvider sourceExpressionValueProvider = parameter.getSourceValueProvider();
    assertNotNull(sourceExpressionValueProvider);

    assertTrue(sourceExpressionValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) sourceExpressionValueProvider;
    assertEquals(source, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testOutputTarget() {
    // given:
    String target = "b";
    ExtensionElements extensionElements = addExtensionElements(caseTask);
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaTarget(target);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CaseTaskActivityBehavior behavior = (CaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getOutputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    assertEquals(target, parameter.getTarget());
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
    planItem.getExitCriterias().add(sentry);

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
    planItem.getExitCriterias().add(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    Body body2 = createElement(conditionExpression2, null, Body.class);
    body2.setTextContent("${test}");

    // set second exitCriteria
    planItem.getExitCriterias().add(sentry2);

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

    // set exitCriteria
    planItem.getEntryCriterias().add(sentry);

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
    planItem.getEntryCriterias().add(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    Body body2 = createElement(conditionExpression2, null, Body.class);
    body2.setTextContent("${test}");

    // set second entryCriteria
    planItem.getEntryCriterias().add(sentry2);

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
    planItem.getEntryCriterias().add(sentry);
    planItem.getExitCriterias().add(sentry);

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

}
