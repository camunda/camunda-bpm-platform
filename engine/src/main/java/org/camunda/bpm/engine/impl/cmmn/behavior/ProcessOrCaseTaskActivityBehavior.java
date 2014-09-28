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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 * @author Roman Smirnov
 *
 */
public abstract class ProcessOrCaseTaskActivityBehavior extends TaskActivityBehavior implements TransferVariablesActivityBehavior {

  protected CallableElement callableElement;

  protected void performStart(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;

    List<CallableElementParameter> inputs = callableElement.getInputs();
    Map<String, Object> variables = getVariables(inputs, caseExecution);

    String businessKey = callableElement.getBusinessKey(caseExecution);

    triggerCallableElement(caseExecution, variables, businessKey);

    if (caseExecution.isActive() && !isBlocking(caseExecution)) {
      caseExecution.complete();
    }

  }

  public void transferVariables(VariableScope from, VariableScope to) {
    AbstractVariableScope fromVariableScope = (AbstractVariableScope) from;

    List<CallableElementParameter> outputs = callableElement.getOutputs();
    Map<String, Object> variables = getVariables(outputs, fromVariableScope);

    to.setVariables(variables);
  }

  protected Map<String, Object> getVariables(List<CallableElementParameter> params, AbstractVariableScope variableScope) {
    Map<String, Object> result = new HashMap<String, Object>();

    for (CallableElementParameter param : params) {

      if (param.isAllVariables()) {
        Map<String, Object> allVariables = variableScope.getVariables();
        result.putAll(allVariables);

      } else {
        String targetVariableName = param.getTarget();
        Object value = param.getSource(variableScope);
        result.put(targetVariableName, value);
      }

    }

    return result;
  }

  public CallableElement getCallableElement() {
    return callableElement;
  }

  public void setCallableElement(CallableElement callableElement) {
    this.callableElement = callableElement;
  }

  protected String getDefinitionKey(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    return getCallableElement().getDefinitionKey(caseExecution);
  }

  protected CallableElementBinding getBinding() {
    return getCallableElement().getBinding();
  }

  protected Integer getVersion(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    return getCallableElement().getVersion(caseExecution);
  }

  protected String getDeploymentId(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    CmmnCaseDefinition definition = caseExecution.getCaseDefinition();
    if (definition instanceof CaseDefinitionEntity) {
      CaseDefinitionEntity caseDefinition = (CaseDefinitionEntity) definition;
      return caseDefinition.getDeploymentId();
    }
    return null;
  }

  protected boolean isLatestBinding() {
    CallableElementBinding binding = getBinding();
    return binding == null || CallableElementBinding.LATEST.equals(binding);
  }

  protected boolean isDeploymentBinding() {
    CallableElementBinding binding = getBinding();
    return CallableElementBinding.DEPLOYMENT.equals(binding);
  }

  protected boolean isVersionBinding() {
    CallableElementBinding binding = getBinding();
    return CallableElementBinding.VERSION.equals(binding);
  }

  protected abstract void triggerCallableElement(CmmnActivityExecution execution, Map<String, Object> variables, String businessKey);

}
