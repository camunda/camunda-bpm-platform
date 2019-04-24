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
package org.camunda.bpm.engine.impl.delegate;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;

/**
 * Class responsible for handling Expression.setValue() invocations.
 *
 * @author Daniel Meyer
 */
public class ExpressionSetInvocation extends DelegateInvocation {

  protected final ValueExpression valueExpression;
  protected final Object value;
  protected ELContext elContext;

  public ExpressionSetInvocation(ValueExpression valueExpression, ELContext elContext, Object value, BaseDelegateExecution contextExecution) {
    super(contextExecution, null);
    this.valueExpression = valueExpression;
    this.value = value;
    this.elContext = elContext;
  }

  @Override
  protected void invoke() throws Exception {
    valueExpression.setValue(elContext, value);
  }

}
