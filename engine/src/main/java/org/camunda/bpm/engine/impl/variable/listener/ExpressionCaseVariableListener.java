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

import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.delegate.Expression;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExpressionCaseVariableListener implements CaseVariableListener {

  protected Expression expression;

  public ExpressionCaseVariableListener(Expression expression) {
    this.expression = expression;
  }

  public void notify(DelegateCaseVariableInstance variableInstance) throws Exception {
    DelegateCaseVariableInstanceImpl variableInstanceImpl = (DelegateCaseVariableInstanceImpl) variableInstance;

    // Return value of expression is ignored
    expression.getValue(variableInstanceImpl.getScopeExecution());
  }

  /**
   * Returns the expression text for this execution listener. Comes in handy if you want to
   * check which listeners you already have.
   */
  public String getExpressionText() {
    return expression.getExpressionText();
  }
}
