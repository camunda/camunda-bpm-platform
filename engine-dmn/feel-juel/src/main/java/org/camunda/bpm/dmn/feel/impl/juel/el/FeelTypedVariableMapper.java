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
package org.camunda.bpm.dmn.feel.impl.juel.el;

import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.juel.FeelLogger;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FeelTypedVariableMapper extends VariableMapper {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected ExpressionFactory expressionFactory;
  protected VariableContext variableContext;

  public FeelTypedVariableMapper(ExpressionFactory expressionFactory, VariableContext variableContext) {
    this.expressionFactory = expressionFactory;
    this.variableContext = variableContext;
  }

  public ValueExpression resolveVariable(String variable) {
    if (variableContext.containsVariable(variable)) {
      Object value = unpackVariable(variable);
      return expressionFactory.createValueExpression(value, Object.class);
    }
    else {
      throw LOG.unknownVariable(variable);
    }
  }

  public ValueExpression setVariable(String variable, ValueExpression expression) {
    throw LOG.variableMapperIsReadOnly();
  }

  public Object unpackVariable(String variable) {
    TypedValue valueTyped = variableContext.resolve(variable);
    if(valueTyped != null) {
      return valueTyped.getValue();
    }
    return null;
  }

}
