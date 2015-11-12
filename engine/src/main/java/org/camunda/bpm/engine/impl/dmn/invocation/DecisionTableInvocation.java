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
package org.camunda.bpm.engine.impl.dmn.invocation;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * {@link DelegateInvocation} invoking a {@link DecisionDefinition}
 * in a given {@link VariableContext}.
 *
 * The DmnEngine instance is resolved from the Context.
 *
 * The invocation result is a {@link DmnDecisionTableResult}.
 *
 * The target of the invocation is the {@link DecisionDefinition}.
 *
 * @author Daniel Meyer
 *
 */
public class DecisionTableInvocation extends DelegateInvocation {

  protected DecisionDefinition decisionDefinition;
  protected VariableContext variableContext;

  public DecisionTableInvocation(DecisionDefinition decisionDefinition, VariableContext variableContext) {
    super(null, (DecisionDefinitionEntity) decisionDefinition);
    this.decisionDefinition = decisionDefinition;
    this.variableContext = variableContext;
  }

  protected void invoke() throws Exception {
    final DmnEngine dmnEngine = Context.getProcessEngineConfiguration()
      .getDmnEngine();

    invocationResult = dmnEngine.evaluateDecisionTable((DmnDecision) decisionDefinition, variableContext);
  }

  @Override
  public DmnDecisionTableResult getInvocationResult() {
    return (DmnDecisionTableResult) super.getInvocationResult();
  }

  public DecisionDefinition getDecisionDefinition() {
    return decisionDefinition;
  }

}
