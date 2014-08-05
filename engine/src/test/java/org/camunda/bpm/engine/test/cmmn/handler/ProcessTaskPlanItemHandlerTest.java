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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElementParameter;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.ProcessTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.ProcessTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.listener.ClassDelegateCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.DelegateExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ScriptCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaExpression;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaString;
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

  @Test
  public void testCreateCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCreateCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCreateCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testCreateCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testEnableCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testEnableCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testEnableCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testEnableCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testDisableCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testDisableCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testDisableCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testDisableCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testReEnableCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testReEnableCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testReEnableCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testReEnableCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testStartCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testStartCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testStartCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testStartCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testManualStartCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testManualStartCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testManualStartCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testManualStartCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testCompleteCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCompleteCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCompleteCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testCompleteCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testTerminateCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testTerminateCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testTerminateCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testTerminateCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testExitCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testExitCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testExitCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testExitCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testSuspendCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testSuspendCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testSuspendCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testSuspendCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testResumeCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testResumeCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testResumeCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testResumeCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testAllCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));


    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

      ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
      assertEquals(className, classDelegateListener.getClassName());
      assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

      DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
      assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
      assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ExpressionCaseExecutionListener);

      ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
      assertEquals(expression, expressionListener.getExpressionText());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ScriptCaseExecutionListener);

      ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
      ExecutableScript executableScript = scriptListener.getScript();
      assertNotNull(executableScript);
      assertEquals(scriptFormat, executableScript.getLanguage());
    }

  }

  @Test
  public void testFieldInjectionExpressionOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldExpression = "${myFirstExpression}";

    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaExpression(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldExpression = "${mySecondExpression}";

    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaExpression(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionExpressionChildOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaExpression firstFieldExpressionChild = createElement(firstField, null, CamundaExpression.class);

    String firstFieldExpression = "${myFirstExpression}";
    firstFieldExpressionChild.setTextContent(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaExpression secondFieldExpressionChild = createElement(secondField, null, CamundaExpression.class);

    String secondFieldExpression = "${mySecondExpression}";
    secondFieldExpressionChild.setTextContent(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaString firstFieldStringChild = createElement(firstField, null, CamundaString.class);

    String firstFieldString = "aFirstFixedValue";
    firstFieldStringChild.setTextContent(firstFieldString);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaString secondFieldStringChild = createElement(secondField, null, CamundaString.class);

    String secondFieldString = "aSecondFixedValue";
    secondFieldStringChild.setTextContent(secondFieldString);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldString, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldString, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringValueOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldStringValue = "aFirstFixedValue";
    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaStringValue(firstFieldStringValue);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldStringValue = "aSecondFixedValue";
    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaStringValue(secondFieldStringValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldStringValue, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldStringValue, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionExpressionOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldExpression = "${myFirstExpression}";

    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaExpression(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldExpression = "${mySecondExpression}";

    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaExpression(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = delegateExpressionListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionExpressionChildOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaExpression firstFieldExpressionChild = createElement(firstField, null, CamundaExpression.class);

    String firstFieldExpression = "${myFirstExpression}";
    firstFieldExpressionChild.setTextContent(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaExpression secondFieldExpressionChild = createElement(secondField, null, CamundaExpression.class);

    String secondFieldExpression = "${mySecondExpression}";
    secondFieldExpressionChild.setTextContent(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener classDelegateListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, classDelegateListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaString firstFieldStringChild = createElement(firstField, null, CamundaString.class);

    String firstFieldString = "aFirstFixedValue";
    firstFieldStringChild.setTextContent(firstFieldString);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaString secondFieldStringChild = createElement(secondField, null, CamundaString.class);

    String secondFieldString = "aSecondFixedValue";
    secondFieldStringChild.setTextContent(secondFieldString);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener classDelegateListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, classDelegateListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldString, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldString, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringValueOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(processTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldStringValue = "aFirstFixedValue";
    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaStringValue(firstFieldStringValue);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldStringValue = "aSecondFixedValue";
    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaStringValue(secondFieldStringValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener classDelegateListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, classDelegateListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldStringValue, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldStringValue, expressionValue.getExpressionText());

  }

}
