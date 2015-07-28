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

import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public abstract class ProcessOrCaseTaskActivityBehavior extends TaskActivityBehavior implements TransferVariablesActivityBehavior {

  protected CallableElement callableElement;

  protected void performStart(CmmnActivityExecution execution) {
    VariableMap variables = getInputVariables(execution);
    String businessKey = getBusinessKey(execution);
    triggerCallableElement(execution, variables, businessKey);

    if (execution.isActive() && !isBlocking(execution)) {
      execution.complete();
    }
  }

  public void transferVariables(VariableScope from, VariableScope to) {
    VariableMap variables = getOutputVariables(from);
    to.setVariables(variables);
  }

  public CallableElement getCallableElement() {
    return callableElement;
  }

  public void setCallableElement(CallableElement callableElement) {
    this.callableElement = callableElement;
  }

  protected String getBusinessKey(CmmnActivityExecution execution) {
    return getCallableElement().getBusinessKey(execution);
  }

  protected VariableMap getInputVariables(CmmnActivityExecution execution) {
    return getCallableElement().getInputVariables(execution);
  }

  protected VariableMap getOutputVariables(VariableScope variableScope) {
    return getCallableElement().getOutputVariables(variableScope);
  }

  protected String getDefinitionKey(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    return getCallableElement().getDefinitionKey(caseExecution);
  }

  protected Integer getVersion(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    return getCallableElement().getVersion(caseExecution);
  }

  protected String getDeploymentId(CmmnActivityExecution execution) {
    return getCallableElement().getDeploymentId();
  }

  protected CallableElementBinding getBinding() {
    return getCallableElement().getBinding();
  }

  protected boolean isLatestBinding() {
    return getCallableElement().isLatestBinding();
  }

  protected boolean isDeploymentBinding() {
    return getCallableElement().isDeploymentBinding();
  }

  protected boolean isVersionBinding() {
    return getCallableElement().isVersionBinding();
  }

  protected abstract void triggerCallableElement(CmmnActivityExecution execution, Map<String, Object> variables, String businessKey);

}
