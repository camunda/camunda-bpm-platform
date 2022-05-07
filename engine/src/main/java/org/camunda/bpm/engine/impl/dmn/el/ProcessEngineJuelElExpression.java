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
package org.camunda.bpm.engine.impl.dmn.el;

import org.camunda.bpm.dmn.engine.impl.spi.el.ElExpression;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessEngineJuelElExpression implements ElExpression {

  protected final JuelExpressionManager expressionManager;
  protected final ValueExpression valueExpression;

  public ProcessEngineJuelElExpression(JuelExpressionManager expressionManager, ValueExpression expression) {
    this.expressionManager = expressionManager;
    this.valueExpression = expression;
  }

  public Object getValue(VariableContext variableContext) {
    if(Context.getCommandContext() == null) {
      throw new ProcessEngineException("Expression can only be evaluated inside the context of the process engine");
    }

    ELContext context = expressionManager.createElContext(variableContext);

    return valueExpression.getValue(context);

  }

}
