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

import java.util.concurrent.Callable;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.dmn.invocation.DecisionTableInvocation;
import org.camunda.bpm.engine.impl.dmn.invocation.VariableScopeContext;
import org.camunda.bpm.engine.impl.dmn.result.DecisionTableResultMapper;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.util.CallableElementUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;

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
public class DecisionRuleTaskActivityBehavior extends AbstractBpmnActivityBehavior {

  public static final String DECISION_RESULT_VARIABLE = "decisionResult";

  protected final BaseCallableElement callableElement;
  protected final String resultVariable;
  protected final DecisionTableResultMapper decisionTableResultMapper;

  public DecisionRuleTaskActivityBehavior(BaseCallableElement callableElement, String resultVariableName, DecisionTableResultMapper decisionTableResultMapper) {
    this.callableElement = callableElement;
    this.resultVariable = resultVariableName;
    this.decisionTableResultMapper = decisionTableResultMapper;
  }

  @Override
  public void execute(final ActivityExecution execution) throws Exception {

    final DecisionDefinition decisionDefinition = resolveDecisionDefinition(execution);
    final DecisionTableInvocation invocation = createInvocation(execution, decisionDefinition);

    executeWithErrorPropagation(execution, new Callable<Void>() {

      public Void call() throws Exception {

        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(invocation);

        DmnDecisionTableResult result = invocation.getInvocationResult();
        if (result != null) {
          // sets the decision result as variable for output mapping
          ((ExecutionEntity) execution).setVariableLocalTransient(DECISION_RESULT_VARIABLE, result);

          if (resultVariable != null && decisionTableResultMapper != null) {
            Object mappedDecisionResult = decisionTableResultMapper.mapDecisionTableResult(result);
            execution.setVariable(resultVariable, mappedDecisionResult);
          }
        }

        leave(execution);
        return null;
      }

    });
  }

  protected DecisionTableInvocation createInvocation(ActivityExecution execution, DecisionDefinition decisionDefinitionToCall) {
    return new DecisionTableInvocation(decisionDefinitionToCall, VariableScopeContext.wrap(execution));
  }

  protected DecisionDefinition resolveDecisionDefinition(ActivityExecution execution) {
    return CallableElementUtil.getDecisionDefinitionToCall(execution, callableElement);
  }

}
