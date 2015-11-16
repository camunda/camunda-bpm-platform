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

package org.camunda.bpm.engine.test.history;

import java.io.Serializable;

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;

public class DecisionServiceDelegate implements JavaDelegate, CaseExecutionListener, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    DecisionService decisionService = execution.getProcessEngineServices().getDecisionService();
    evaluateDecision(decisionService, execution);
  }

  public void notify(DelegateCaseExecution caseExecution) throws Exception {
    DecisionService decisionService = caseExecution.getProcessEngineServices().getDecisionService();
    evaluateDecision(decisionService, caseExecution);
  }

  public boolean evaluate(DelegateCaseExecution caseExecution) {
    DecisionService decisionService = caseExecution.getProcessEngineServices().getDecisionService();
    DmnDecisionTableResult result = evaluateDecision(decisionService, caseExecution);
    DmnDecisionRuleResult singleResult = result.getSingleResult();
    return (Boolean) singleResult.getSingleEntry();
  }

  protected DmnDecisionTableResult evaluateDecision(DecisionService decisionService, VariableScope variableScope) {
    return decisionService.evaluateDecisionTableByKey("testDecision", variableScope.getVariables());
  }

}
