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
package org.camunda.bpm.engine.impl.cmmn.listener;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.Expression;

/**
 * @author Roman Smirnov
 *
 */
public class ExpressionCaseExecutionListener implements CaseExecutionListener {

  protected Expression expression;

  public ExpressionCaseExecutionListener(Expression expression) {
    this.expression = expression;
  }

  public void notify(DelegateCaseExecution caseExecution) throws Exception {
    // Return value of expression is ignored
    expression.getValue(caseExecution);
  }

  /**
   * Returns the expression text for this execution listener. Comes in handy if you want to
   * check which listeners you already have.
   */
  public String getExpressionText() {
    return expression.getExpressionText();
  }

}
