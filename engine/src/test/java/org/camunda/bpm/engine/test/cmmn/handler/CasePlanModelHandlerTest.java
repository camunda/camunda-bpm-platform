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
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.listener.ClassDelegateCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.DelegateExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ScriptCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaExpression;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaString;
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
  public void testCreateCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testReActivateCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.RE_ACTIVATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testReActivateCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.RE_ACTIVATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testReActivateCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.RE_ACTIVATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testReActivateCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.RE_ACTIVATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testCloseCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CLOSE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testCloseCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CLOSE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testCloseCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.CLOSE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
  public void testCloseCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.CLOSE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(6, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reActivateListeners = activity.getListeners(CaseExecutionListener.RE_ACTIVATE);
    assertEquals(1, reActivateListeners.size());
    listeners.add(reActivateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> closeListeners = activity.getListeners(CaseExecutionListener.CLOSE);
    assertEquals(1, closeListeners.size());
    listeners.add(closeListeners.get(0));

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(6, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reActivateListeners = activity.getListeners(CaseExecutionListener.RE_ACTIVATE);
    assertEquals(1, reActivateListeners.size());
    listeners.add(reActivateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> closeListeners = activity.getListeners(CaseExecutionListener.CLOSE);
    assertEquals(1, closeListeners.size());
    listeners.add(closeListeners.get(0));

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(6, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reActivateListeners = activity.getListeners(CaseExecutionListener.RE_ACTIVATE);
    assertEquals(1, reActivateListeners.size());
    listeners.add(reActivateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> closeListeners = activity.getListeners(CaseExecutionListener.CLOSE);
    assertEquals(1, closeListeners.size());
    listeners.add(closeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ExpressionCaseExecutionListener);

      ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
      assertEquals(expression, expressionListener.getExpressionText());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

    // then
    assertEquals(6, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reActivateListeners = activity.getListeners(CaseExecutionListener.RE_ACTIVATE);
    assertEquals(1, reActivateListeners.size());
    listeners.add(reActivateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> closeListeners = activity.getListeners(CaseExecutionListener.CLOSE);
    assertEquals(1, closeListeners.size());
    listeners.add(closeListeners.get(0));

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
    ExtensionElements extensionElements = addExtensionElements(casePlanModel);
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
    CmmnActivity activity = handler.handleElement(casePlanModel, context);

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
