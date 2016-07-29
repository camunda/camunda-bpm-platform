/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil.evaluateDecision;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.dmn.result.DecisionResultMapper;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implementation of a Bpmn BusinessRuleTask executing a DMN Decision.
 *
 * The decision is resolved as a {@link BaseCallableElement}.
 *
 * The decision is executed in the context of the current {@link VariableScope}.
 *
 * @author Daniel Meyer
 *
 */
public class DmnBusinessRuleTaskActivityBehavior extends AbstractBpmnActivityBehavior {

  protected final BaseCallableElement callableElement;
  protected final String resultVariable;
  protected final DecisionResultMapper decisionResultMapper;

  public DmnBusinessRuleTaskActivityBehavior(BaseCallableElement callableElement, String resultVariableName, DecisionResultMapper decisionResultMapper) {
    this.callableElement = callableElement;
    this.resultVariable = resultVariableName;
    this.decisionResultMapper = decisionResultMapper;
  }

  @Override
  public void execute(final ActivityExecution execution) throws Exception {
    executeWithErrorPropagation(execution, new Callable<Void>() {

      public Void call() throws Exception {
        evaluateDecision((AbstractVariableScope) execution, callableElement, resultVariable, decisionResultMapper);
        leave(execution);
        return null;
      }

    });
  }

}
