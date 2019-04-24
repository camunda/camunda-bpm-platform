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
package org.camunda.bpm.engine.impl.bpmn.listener;

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.applyFieldDeclaration;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.BpmnBehaviorLogger;
import org.camunda.bpm.engine.impl.bpmn.delegate.ExecutionListenerInvocation;
import org.camunda.bpm.engine.impl.bpmn.delegate.JavaDelegateInvocation;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;


/**
 * @author Joram Barrez
 */
public class DelegateExpressionExecutionListener implements ExecutionListener {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  protected Expression expression;
  private final List<FieldDeclaration> fieldDeclarations;

  public DelegateExpressionExecutionListener(Expression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }

  public void notify(DelegateExecution execution) throws Exception {
    // Note: we can't cache the result of the expression, because the
    // execution can change: eg. delegateExpression='${mySpringBeanFactory.randomSpringBean()}'
    Object delegate = expression.getValue(execution);
    applyFieldDeclaration(fieldDeclarations, delegate);

    if (delegate instanceof ExecutionListener) {
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new ExecutionListenerInvocation((ExecutionListener) delegate, execution));
    } else if (delegate instanceof JavaDelegate) {
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new JavaDelegateInvocation((JavaDelegate) delegate, execution));
    } else {
      throw LOG.resolveDelegateExpressionException(expression, ExecutionListener.class, JavaDelegate.class);
    }
  }

  /**
   * returns the expression text for this execution listener. Comes in handy if you want to
   * check which listeners you already have.
   */
  public String getExpressionText() {
    return expression.getExpressionText();
  }

}
