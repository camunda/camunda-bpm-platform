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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.CaseRule;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;

/**
 * @author Roman Smirnov
 *
 */
public class CaseRuleImpl implements CaseRule {

  protected Expression expression;

  public CaseRuleImpl(Expression expression) {
    this.expression = expression;
  }

  public boolean evaluate(CmmnActivityExecution execution) {
    if (expression == null) {
      return false;
    }

    Object result = expression.getValue(execution);

    if (result==null) {
      throw new ProcessEngineException("rule expression returns null");
    }

    if (!(result instanceof Boolean)) {
      throw new ProcessEngineException("rule expression returns non-Boolean: "+result+" ("+result.getClass().getName()+")");
    }

    return (Boolean) result;
  }

}
