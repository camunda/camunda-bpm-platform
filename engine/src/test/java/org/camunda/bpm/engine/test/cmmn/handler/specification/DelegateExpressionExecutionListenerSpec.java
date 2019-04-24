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
package org.camunda.bpm.engine.test.cmmn.handler.specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.listener.DelegateExpressionCaseExecutionListener;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;

public class DelegateExpressionExecutionListenerSpec extends AbstractExecutionListenerSpec {

  protected static final String DELEGATE_EXPRESSION = "${myDelegateExpression}";

  public DelegateExpressionExecutionListenerSpec(String eventName) {
    super(eventName);
  }

  protected void configureCaseExecutionListener(CmmnModelInstance modelInstance, CamundaCaseExecutionListener listener) {
    listener.setCamundaDelegateExpression(DELEGATE_EXPRESSION);
  }

  public void verifyListener(DelegateListener<? extends BaseDelegateExecution> listener) {
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(DELEGATE_EXPRESSION, delegateExpressionListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = delegateExpressionListener.getFieldDeclarations();
    assertEquals(fieldSpecs.size(), fieldDeclarations.size());

    for (int i = 0; i < fieldDeclarations.size(); i++) {
      FieldDeclaration declaration = fieldDeclarations.get(i);
      FieldSpec matchingFieldSpec = fieldSpecs.get(i);
      matchingFieldSpec.verify(declaration);
    }
  }

}
