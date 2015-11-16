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

import static org.camunda.bpm.engine.impl.util.DecisionTableUtil.evaluateDecisionTable;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.dmn.result.DecisionTableResultMapper;

/**
 * @author Roman Smirnov
 *
 */
public class DmnDecisionTaskActivityBehavior extends DecisionTaskActivityBehavior {

  protected DecisionTableResultMapper decisionResultMapper;

  protected void performStart(CmmnActivityExecution execution) {
    try {
      evaluateDecisionTable((AbstractVariableScope) execution, callableElement, resultVariable, decisionResultMapper);

      if (execution.isActive()) {
        execution.complete();
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw LOG.decisionDefinitionEvaluationFailed(execution, e);
    }
  }

  public DecisionTableResultMapper getDecisionTableResultMapper() {
    return decisionResultMapper;
  }

  public void setDecisionTableResultMapper(DecisionTableResultMapper decisionResultMapper) {
    this.decisionResultMapper = decisionResultMapper;
  }

}
