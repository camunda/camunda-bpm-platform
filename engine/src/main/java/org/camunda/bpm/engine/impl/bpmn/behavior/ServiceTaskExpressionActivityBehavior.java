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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * ActivityBehavior that evaluates an expression when executed. Optionally, it
 * sets the result of the expression as a variable on the execution.
 *
 * @author Tom Baeyens
 * @author Christian Stettler
 * @author Frederik Heremans
 * @author Slawomir Wojtasiak (Patch for ACT-1159)
 * @author Falko Menge
 */
public class ServiceTaskExpressionActivityBehavior extends TaskActivityBehavior {

  protected Expression expression;
  protected String resultVariable;

  public ServiceTaskExpressionActivityBehavior(Expression expression, String resultVariable) {
    this.expression = expression;
    this.resultVariable = resultVariable;
  }

  @Override
  public void performExecution(final ActivityExecution execution) throws Exception {
    executeWithErrorPropagation(execution, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        //getValue() can have side-effects, that's why we have to call it independently from the result variable
        Object value = expression.getValue(execution);
        if (resultVariable != null) {
          execution.setVariable(resultVariable, value);
        }
        leave(execution);
        return null;
      }
    });

  }
}
