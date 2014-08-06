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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElementParameter;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.ProcessTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.ProcessTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskPlanItemHandlerTest extends CmmnElementHandlerTest {

  protected ProcessTask processTask;
  protected PlanItem planItem;
  protected ProcessTaskItemHandler handler = new ProcessTaskItemHandler();

  @Before
  public void setUp() {
    processTask = createElement(casePlanModel, "aProcessTask", ProcessTask.class);

    planItem = createElement(casePlanModel, "PI_aProcessTask", PlanItem.class);
    planItem.setDefinition(processTask);

  }

  @Test
  public void testProcessTaskActivityName() {
    // given:
    // the processTask has a name "A ProcessTask"
    String name = "A ProcessTask";
    processTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testPlanItemActivityName() {
    // given:
    // the processTask has a name "A CaseTask"
    String processTaskName = "A ProcessTask";
    processTask.setName(processTaskName);

    // the planItem has an own name "My LocalName"
    String planItemName = "My LocalName";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNotEquals(processTaskName, activity.getName());
    assertEquals(planItemName, activity.getName());
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof ProcessTaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given: a processTask with isBlocking = true (defaultValue)

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    // a processTask with isBlocking = false
    processTask.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
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
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();

    assertNotNull(behavior.getCallableElement());
  }

  @Test
  public void testProcessRefConstant() {
    // given:
    String processRef = "aProcessToCall";
    processTask.setProcess(processRef);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider processRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(processRefValueProvider);

    assertTrue(processRefValueProvider instanceof ConstantValueProvider);
    ConstantValueProvider valueProvider = (ConstantValueProvider) processRefValueProvider;
    assertEquals(processRef, valueProvider.getValue(null));
  }

  @Test
  public void testProcessRefExpression() {
    // given:
    String processRef = "${aProcessToCall}";
    processTask.setProcess(processRef);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider processRefValueProvider = callableElement.getDefinitionKeyValueProvider();
    assertNotNull(processRefValueProvider);

    assertTrue(processRefValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) processRefValueProvider;
    assertEquals(processRef, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testBinding() {
    // given:
    CallableElementBinding processBinding = CallableElementBinding.LATEST;
    processTask.setCamundaProcessBinding(processBinding.getValue());

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    CallableElementBinding binding = callableElement.getBinding();
    assertNotNull(binding);
    assertEquals(processBinding, binding);
  }

  @Test
  public void testVersionConstant() {
    // given:
    String processVersion = "2";
    processTask.setCamundaProcessVersion(processVersion);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider processVersionValueProvider = callableElement.getVersionValueProvider();
    assertNotNull(processVersionValueProvider);

    assertTrue(processVersionValueProvider instanceof ConstantValueProvider);
    assertEquals(processVersion, processVersionValueProvider.getValue(null));
  }

  @Test
  public void testVersionExpression() {
    // given:
    String processVersion = "${aVersion}";
    processTask.setCamundaProcessVersion(processVersion);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ParameterValueProvider processVersionValueProvider = callableElement.getVersionValueProvider();
    assertNotNull(processVersionValueProvider);

    assertTrue(processVersionValueProvider instanceof ElValueProvider);
    ElValueProvider valueProvider = (ElValueProvider) processVersionValueProvider;
    assertEquals(processVersion, valueProvider.getExpression().getExpressionText());
  }

  @Test
  public void testBusinessKeyConstant() {
    // given:
    String businessKey = "myBusinessKey";
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn businessKeyElement = createElement(extensionElements, null, CamundaIn.class);
    businessKeyElement.setCamundaBusinessKey(businessKey);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
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
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn businessKeyElement = createElement(extensionElements, null, CamundaIn.class);
    businessKeyElement.setCamundaBusinessKey(businessKey);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
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
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn variablesElement = createElement(extensionElements, null, CamundaIn.class);
    variablesElement.setCamundaVariables("all");
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaSource("a");
    CamundaIn sourceExpressionElement = createElement(extensionElements, null, CamundaIn.class);
    sourceExpressionElement.setCamundaSourceExpression("${b}");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    List<CallableElementParameter> inputs = callableElement.getInputs();
    assertNotNull(inputs);
    assertFalse(inputs.isEmpty());
    assertEquals(3, inputs.size());
  }

  @Test
  public void testInputVariables() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn variablesElement = createElement(extensionElements, null, CamundaIn.class);
    variablesElement.setCamundaVariables("all");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getInputs().get(0);

    assertNotNull(parameter);
    assertTrue(parameter.isAllVariables());
  }

  @Test
  public void testInputSource() {
    // given:
    String source = "a";
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaSource(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
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
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaSourceExpression(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
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
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaIn sourceElement = createElement(extensionElements, null, CamundaIn.class);
    sourceElement.setCamundaTarget(target);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getInputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    assertEquals(target, parameter.getTarget());
  }

  @Test
  public void testOutputs() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaOut variablesElement = createElement(extensionElements, null, CamundaOut.class);
    variablesElement.setCamundaVariables("all");
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaSource("a");
    CamundaOut sourceExpressionElement = createElement(extensionElements, null, CamundaOut.class);
    sourceExpressionElement.setCamundaSourceExpression("${b}");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    List<CallableElementParameter> outputs = callableElement.getOutputs();
    assertNotNull(outputs);
    assertFalse(outputs.isEmpty());
    assertEquals(3, outputs.size());
  }

  @Test
  public void testOutputVariables() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaOut variablesElement = createElement(extensionElements, null, CamundaOut.class);
    variablesElement.setCamundaVariables("all");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getOutputs().get(0);

    assertNotNull(parameter);
    assertTrue(parameter.isAllVariables());
  }

  @Test
  public void testOutputSource() {
    // given:
    String source = "a";
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaSource(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
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
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaSourceExpression(source);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
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
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaOut sourceElement = createElement(extensionElements, null, CamundaOut.class);
    sourceElement.setCamundaTarget(target);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    ProcessTaskActivityBehavior behavior = (ProcessTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();
    CallableElementParameter parameter = callableElement.getOutputs().get(0);

    assertNotNull(parameter);
    assertFalse(parameter.isAllVariables());

    assertEquals(target, parameter.getTarget());
  }

}
