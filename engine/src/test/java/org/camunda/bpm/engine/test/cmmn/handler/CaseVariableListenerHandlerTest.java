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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.cmmn.handler.CaseTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.variable.listener.ClassDelegateCaseVariableListener;
import org.camunda.bpm.engine.impl.variable.listener.DelegateExpressionCaseVariableListener;
import org.camunda.bpm.engine.impl.variable.listener.ExpressionCaseVariableListener;
import org.camunda.bpm.engine.test.cmmn.handler.specification.SpecUtil;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaVariableListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class CaseVariableListenerHandlerTest extends CmmnElementHandlerTest {

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
  public void testClassDelegateHandling() {
    ExtensionElements extensionElements = SpecUtil.createElement(modelInstance, caseTask, null, ExtensionElements.class);
    CamundaVariableListener variableListener = SpecUtil.createElement(modelInstance, extensionElements, null, CamundaVariableListener.class);
    CamundaField field = SpecUtil.createElement(modelInstance, variableListener, null, CamundaField.class);
    field.setCamundaName("fieldName");
    field.setCamundaStringValue("a string value");

    variableListener.setCamundaClass("a.class.Name");

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    List<VariableListener<?>> listeners = activity.getVariableListenersLocal(CaseVariableListener.CREATE);
    Assert.assertEquals(1, listeners.size());

    ClassDelegateCaseVariableListener listener = (ClassDelegateCaseVariableListener) listeners.get(0);
    Assert.assertEquals("a.class.Name", listener.getClassName());
    Assert.assertEquals(1, listener.getFieldDeclarations().size());
    Assert.assertEquals("fieldName", listener.getFieldDeclarations().get(0).getName());
    Object fieldValue = listener.getFieldDeclarations().get(0).getValue();
    assertTrue(fieldValue instanceof Expression);
    Expression expressionValue = (Expression) fieldValue;
    assertEquals("a string value", expressionValue.getExpressionText());

    Assert.assertEquals(listener, activity.getVariableListenersLocal(CaseVariableListener.UPDATE).get(0));
    Assert.assertEquals(listener, activity.getVariableListenersLocal(CaseVariableListener.DELETE).get(0));
  }

  @Test
  public void testDelegateExpressionDelegateHandling() {
    ExtensionElements extensionElements = SpecUtil.createElement(modelInstance, caseTask, null, ExtensionElements.class);
    CamundaVariableListener variableListener = SpecUtil.createElement(modelInstance, extensionElements, null, CamundaVariableListener.class);
    variableListener.setCamundaDelegateExpression("${expression}");
    variableListener.setCamundaEvent(CaseVariableListener.CREATE);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    List<VariableListener<?>> listeners = activity.getVariableListenersLocal(CaseVariableListener.CREATE);
    Assert.assertEquals(1, listeners.size());

    DelegateExpressionCaseVariableListener listener = (DelegateExpressionCaseVariableListener) listeners.get(0);
    Assert.assertEquals("${expression}", listener.getExpressionText());

    Assert.assertEquals(0, activity.getVariableListenersLocal(CaseVariableListener.UPDATE).size());
    Assert.assertEquals(0, activity.getVariableListenersLocal(CaseVariableListener.DELETE).size());
  }

  @Test
  public void testExpressionDelegateHandling() {
    ExtensionElements extensionElements = SpecUtil.createElement(modelInstance, caseTask, null, ExtensionElements.class);
    CamundaVariableListener variableListener = SpecUtil.createElement(modelInstance, extensionElements, null, CamundaVariableListener.class);
    variableListener.setCamundaExpression("${expression}");
    variableListener.setCamundaEvent(CaseVariableListener.CREATE);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    List<VariableListener<?>> listeners = activity.getVariableListenersLocal(CaseVariableListener.CREATE);
    Assert.assertEquals(1, listeners.size());

    ExpressionCaseVariableListener listener = (ExpressionCaseVariableListener) listeners.get(0);
    Assert.assertEquals("${expression}", listener.getExpressionText());

    Assert.assertEquals(0, activity.getVariableListenersLocal(CaseVariableListener.UPDATE).size());
    Assert.assertEquals(0, activity.getVariableListenersLocal(CaseVariableListener.DELETE).size());
  }

}
