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
package org.camunda.bpm.engine.impl.variable.listener;

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.applyFieldDeclaration;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Thorben Lindhauer
 *
 */
public class DelegateExpressionCaseVariableListener implements CaseVariableListener {

  protected Expression expression;
  private final List<FieldDeclaration> fieldDeclarations;

  public DelegateExpressionCaseVariableListener(Expression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }

  public void notify(DelegateCaseVariableInstance variableInstance) throws Exception {
    Object delegate = expression.getValue(variableInstance.getSourceExecution());
    applyFieldDeclaration(fieldDeclarations, delegate);

    if (delegate instanceof CaseVariableListener) {
      CaseVariableListener listenerInstance = (CaseVariableListener) delegate;
      Context
        .getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new CaseVariableListenerInvocation(listenerInstance, variableInstance));
    } else {
      throw new ProcessEngineException("Delegate expression " + expression
              + " did not resolve to an implementation of " + CaseVariableListener.class);
    }
  }

  /**
   * returns the expression text for this execution listener. Comes in handy if you want to
   * check which listeners you already have.
   */
  public String getExpressionText() {
    return expression.getExpressionText();
  }

  public List<FieldDeclaration> getFieldDeclarations() {
    return fieldDeclarations;
  }

}
