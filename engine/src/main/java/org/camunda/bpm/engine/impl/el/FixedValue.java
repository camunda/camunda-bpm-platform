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
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * Expression that always returns the same value when <code>getValue</code> is
 * called. Setting of the value is not supported.
 *
 * @author Frederik Heremans
 */
public class FixedValue implements Expression {

  private Object value;

  public FixedValue(Object value) {
    this.value = value;
  }

  public Object getValue(VariableScope variableScope) {
    return value;
  }

  public Object getValue(VariableScope variableScope, BaseDelegateExecution contextExecution) {
    return getValue(variableScope);
  }

  public void setValue(Object value, VariableScope variableScope) {
    throw new ProcessEngineException("Cannot change fixed value");
  }

  public String getExpressionText() {
    return value.toString();
  }

  @Override
  public boolean isLiteralText() {
    return true;
  }
}
