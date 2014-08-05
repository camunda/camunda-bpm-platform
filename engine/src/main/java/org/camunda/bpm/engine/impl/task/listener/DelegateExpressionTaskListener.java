/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.task.listener;

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.applyFieldDeclaration;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.task.delegate.TaskListenerInvocation;


/**
 * @author Joram Barrez
 */
public class DelegateExpressionTaskListener implements TaskListener {

  protected Expression expression;
  private final List<FieldDeclaration> fieldDeclarations;

  public DelegateExpressionTaskListener(Expression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }

  public void notify(DelegateTask delegateTask) {
    // Note: we can't cache the result of the expression, because the
    // execution can change: eg. delegateExpression='${mySpringBeanFactory.randomSpringBean()}'

    VariableScope variableScope = delegateTask.getExecution();
    if (variableScope == null) {
      variableScope = delegateTask.getCaseExecution();
    }

    Object delegate = expression.getValue(variableScope);
    applyFieldDeclaration(fieldDeclarations, delegate);

    if (delegate instanceof TaskListener) {
      try {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new TaskListenerInvocation((TaskListener)delegate, delegateTask));
      }catch (Exception e) {
        throw new ProcessEngineException("Exception while invoking TaskListener: "+e.getMessage(), e);
      }
    } else {
      throw new ProcessEngineException("Delegate expression " + expression
              + " did not resolve to an implementation of " + TaskListener.class );
    }
  }

  /**
   * returns the expression text for this task listener. Comes in handy if you want to
   * check which listeners you already have.
   */
  public String getExpressionText() {
    return expression.getExpressionText();
  }

  public List<FieldDeclaration> getFieldDeclarations() {
    return fieldDeclarations;
  }

}
