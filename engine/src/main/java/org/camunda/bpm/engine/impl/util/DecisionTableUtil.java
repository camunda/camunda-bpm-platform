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
package org.camunda.bpm.engine.impl.util;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.dmn.invocation.DecisionTableInvocation;
import org.camunda.bpm.engine.impl.dmn.invocation.VariableScopeContext;
import org.camunda.bpm.engine.impl.dmn.result.CollectEntriesDecisionTableResultMapper;
import org.camunda.bpm.engine.impl.dmn.result.DecisionTableResultMapper;
import org.camunda.bpm.engine.impl.dmn.result.ResultListDecisionTableResultMapper;
import org.camunda.bpm.engine.impl.dmn.result.SingleEntryDecisionTableResultMapper;
import org.camunda.bpm.engine.impl.dmn.result.SingleResultDecisionTableResultMapper;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * @author Roman Smirnov
 *
 */
public class DecisionTableUtil {

  public static final String DECISION_RESULT_VARIABLE = "decisionResult";

  public static DecisionTableResultMapper getDecisionTableResultMapperForName(String mapDecisionResult) {
    if ("singleEntry".equals(mapDecisionResult)) {
      return new SingleEntryDecisionTableResultMapper();

    }
    else if ("singleResult".equals(mapDecisionResult)) {
      return new SingleResultDecisionTableResultMapper();

    }
    else if ("collectEntries".equals(mapDecisionResult)) {
      return new CollectEntriesDecisionTableResultMapper();

    }
    else if ("resultList".equals(mapDecisionResult) || mapDecisionResult == null) {
      return new ResultListDecisionTableResultMapper();

    }
    else {
      return null;
    }
  }

  public static void evaluateDecisionTable(AbstractVariableScope execution, BaseCallableElement callableElement,
      String resultVariable, DecisionTableResultMapper decisionTableResultMapper) throws Exception {

    DecisionDefinition decisionDefinition = resolveDecisionDefinition(callableElement, execution);
    DecisionTableInvocation invocation = createInvocation(decisionDefinition, execution);

    invoke(invocation);

    DmnDecisionTableResult result = invocation.getInvocationResult();
    if (result != null) {
      execution.setVariableLocalTransient(DECISION_RESULT_VARIABLE, result);

      if (resultVariable != null && decisionTableResultMapper != null) {
        Object mappedDecisionResult = decisionTableResultMapper.mapDecisionTableResult(result);
        execution.setVariable(resultVariable, mappedDecisionResult);
      }
    }
  }

  public static DmnDecisionTableResult evaluateDecisionTable(DecisionDefinition decisionDefinition, VariableMap variables) throws Exception {
    DecisionTableInvocation invocation = createInvocation(decisionDefinition, variables);
    invoke(invocation);
    return invocation.getInvocationResult();
  }

  protected static void invoke(DecisionTableInvocation invocation) throws Exception {
    Context.getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(invocation);
  }

  protected static DecisionTableInvocation createInvocation(DecisionDefinition decisionDefinition, VariableMap variables) {
    return createInvocation(decisionDefinition, variables.asVariableContext());
  }

  protected static DecisionTableInvocation createInvocation(DecisionDefinition decisionDefinition, AbstractVariableScope variableScope) {
    return createInvocation(decisionDefinition, VariableScopeContext.wrap(variableScope));
  }

  protected static DecisionTableInvocation createInvocation(DecisionDefinition decisionDefinition, VariableContext variableContext) {
    return new DecisionTableInvocation(decisionDefinition, variableContext);
  }

  protected static DecisionDefinition resolveDecisionDefinition(BaseCallableElement callableElement, AbstractVariableScope execution) {
    return CallableElementUtil.getDecisionDefinitionToCall(execution, callableElement);
  }

}
