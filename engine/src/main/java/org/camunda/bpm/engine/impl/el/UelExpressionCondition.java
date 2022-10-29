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
package org.camunda.bpm.engine.impl.el;

import org.camunda.bpm.engine.ProcessEngineException;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureInstanceOf;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.javax.el.PropertyNotFoundException;


/**
 * {@link Condition} that resolves an UEL expression at runtime.
 *
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class UelExpressionCondition implements Condition {

  protected Expression expression;

  public UelExpressionCondition(Expression expression) {
    this.expression = expression;
  }

  @Override
  public boolean evaluate(DelegateExecution execution) {
    return evaluate(execution, execution);
  }

  @Override
  public boolean evaluate(VariableScope scope, DelegateExecution execution) {
    Object result = expression.getValue(scope, execution);
    ensureNotNull("condition expression returns null", "result", result);
    ensureInstanceOf("condition expression returns non-Boolean", "result", result, Boolean.class);
    return (Boolean) result;
  }

  @Override
  public boolean tryEvaluate(VariableScope scope, DelegateExecution execution) {
    boolean result = false;
    try {
      result = evaluate(scope, execution);
    } catch (ProcessEngineException pex) {
      if (!(pex.getCause() instanceof PropertyNotFoundException)) {
        throw pex;
      }
    }
    return result;
  }
}
